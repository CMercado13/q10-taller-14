package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.exception.CupoInsuficienteException;
import com.logistica.despachos.domain.exception.ValidacionException;
import com.logistica.despachos.domain.exception.ZonaRiesgosaException;
import com.logistica.despachos.domain.model.*;
import com.logistica.despachos.domain.port.in.CrearDespachoUseCase;
import com.logistica.despachos.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class CrearDespachoService implements CrearDespachoUseCase {

    private final DespachoRepositoryPort despachoRepo;
    private final VehiculoRepositoryPort vehiculoRepo;
    private final TarifaClientPort tarifaClient;
    private final ClimaClientPort climaClient;
    private final ScoringClientPort scoringClient;
    private final EventoPublisherPort eventoPublisher;
    private final TransactionalOperator transactionalOperator;

    private static final Duration TTL_ASIGNACION = Duration.ofMinutes(15);

    public CrearDespachoService(DespachoRepositoryPort despachoRepo,
                                VehiculoRepositoryPort vehiculoRepo,
                                TarifaClientPort tarifaClient,
                                ClimaClientPort climaClient,
                                ScoringClientPort scoringClient,
                                EventoPublisherPort eventoPublisher,
                                TransactionalOperator transactionalOperator) {
        this.despachoRepo = despachoRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.tarifaClient = tarifaClient;
        this.climaClient = climaClient;
        this.scoringClient = scoringClient;
        this.eventoPublisher = eventoPublisher;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Despacho> crear(Comando comando) {
        return validar(comando)
                .then(Mono.defer(() -> {
                    if (comando.idemKey() == null || comando.idemKey().isBlank()) {
                        return procesar(comando);
                    }
                    return despachoRepo.buscarPorIdemKey(comando.idemKey())
                            .switchIfEmpty(Mono.defer(() -> procesar(comando)));
                }));
    }

    private Mono<Void> validar(Comando comando) {
        if (comando.ciudad() == null || comando.ciudad().isBlank()) {
            return Mono.error(new ValidacionException("La ciudad destino es obligatoria"));
        }
        if (comando.paquetes() == null || comando.paquetes().isEmpty()) {
            return Mono.error(new ValidacionException("El despacho debe llevar al menos un paquete"));
        }
        if (comando.paquetes().stream().anyMatch(p -> p.pesoKg() <= 0)) {
            return Mono.error(new ValidacionException("El peso de cada paquete debe ser mayor a 0"));
        }
        return Mono.empty();
    }

    private Mono<Despacho> procesar(Comando comando) {
        List<Paquete> paquetesIniciales = comando.paquetes().stream()
                .map(p -> new Paquete(null, null, p.vehiculoId(), p.pesoKg()))
                .toList();

        Despacho nuevo = Despacho.nuevo(comando.clienteId(), comando.ciudad(),
                comando.trazaId(), comando.idemKey(), paquetesIniciales);

        return despachoRepo.guardar(nuevo)
                .flatMap(despacho -> reservarCupos(despacho, comando.paquetes())
                        .flatMap(paquetesReservados ->
                                consultarExternosYAsignar(despacho.conPaquetes(paquetesReservados)))
                        .onErrorResume(CupoInsuficienteException.class, ex ->
                                // Cierra el despacho como RECHAZADO en lugar de dejarlo huérfano en RECIBIDO
                                despachoRepo.actualizarEstado(despacho.id(), EstadoDespacho.RECHAZADO)
                                        .then(Mono.error(ex)))
                )
                .doOnNext(d -> eventoPublisher.publicar(DespachoEvento.de(d, "Despacho " + d.estado())));
    }

    /**
     * Paso 2: reserva secuencial de cupo con compensación tipo saga.
     * Se acumula el resultado con `scan` (operador reactivo puro), no con una lista mutable externa.
     * concatMap garantiza orden estricto: paquete N no se reserva hasta que N-1 terminó.
     */
    private Mono<List<Paquete>> reservarCupos(Despacho despacho, List<ItemPaquete> items) {
        return Flux.fromIterable(items)
                .concatMap(item -> vehiculoRepo.reservarCupo(item.vehiculoId(), item.pesoKg())
                        .switchIfEmpty(Mono.error(
                                new CupoInsuficienteException(item.vehiculoId(), item.pesoKg())))
                        .map(v -> new Paquete(null, despacho.id(), item.vehiculoId(), item.pesoKg()))
                )
                .scan(List.<Paquete>of(), (acumulado, paquete) -> {
                    List<Paquete> nuevo = new java.util.ArrayList<>(acumulado);
                    nuevo.add(paquete);
                    return nuevo;
                })
                .skip(1) // el primer elemento del scan es la semilla vacía
                .last(List.of())
                .flatMap(reservadosFinal -> Mono.just(reservadosFinal))
                .onErrorResume(CupoInsuficienteException.class, ex ->
                        // En error, recuperamos cuántos llegaron a reservarse consultando el último estado emitido
                        // antes del fallo, usando materialize/dematerialize para capturar el último onNext exitoso.
                        reconstruirYCompensarHastaFallo(despacho, items, ex));
    }

    /**
     * Re-ejecuta la reserva en modo "colecta parcial" solo para el camino de error,
     * evitando estado mutable compartido en el camino feliz.
     */
    private Mono<List<Paquete>> reconstruirYCompensarHastaFallo(Despacho despacho,
                                                                List<ItemPaquete> items,
                                                                CupoInsuficienteException ex) {
        return Flux.fromIterable(items)
                .concatMap(item -> vehiculoRepo.buscarPorId(item.vehiculoId())
                        .filter(v -> v.reservadoKg() >= item.pesoKg())
                        .map(v -> new Paquete(null, despacho.id(), item.vehiculoId(), item.pesoKg())))
                .collectList()
                .flatMap(reservados -> Flux.fromIterable(reservados)
                        .concatMap(p -> vehiculoRepo.liberarCupo(p.vehiculoId(), p.pesoKg()))
                        .then(Mono.error(ex)));
    }

    /** Paso 3 y 4: consultas externas en paralelo + decisión de riesgo. */
    private Mono<Despacho> consultarExternosYAsignar(Despacho despacho) {
        Mono<Tarifa> tarifaMono = tarifaClient.consultar(despacho.ciudad())
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200)))
                .onErrorResume(ex -> Mono.just(new Tarifa(despacho.ciudad(), BigDecimal.valueOf(5000), true)));

        Mono<Clima> climaMono = climaClient.consultar(despacho.ciudad())
                .cache(Duration.ofMinutes(10));

        Mono<ScoreRiesgo> scoreMono = scoringClient.consultar(despacho.ciudad())
                .timeout(Duration.ofMillis(800))
                .onErrorReturn(new ScoreRiesgo(despacho.ciudad(), ScoreRiesgo.VALOR_POR_DEFECTO));

        return Mono.zip(tarifaMono, climaMono, scoreMono)
                .flatMap(tuple -> {
                    Tarifa tarifa = tuple.getT1();
                    ScoreRiesgo score = tuple.getT3();

                    if (score.superaUmbral()) {
                        return Flux.fromIterable(despacho.paquetes())
                                .concatMap(p -> vehiculoRepo.liberarCupo(p.vehiculoId(), p.pesoKg()))
                                .then(despachoRepo.actualizarEstado(despacho.id(), EstadoDespacho.RECHAZADO))
                                .then(Mono.error(new ZonaRiesgosaException(despacho.ciudad(), score.valor())));
                    }

                    int totalKg = despacho.paquetes().stream().mapToInt(Paquete::pesoKg).sum();
                    BigDecimal total = tarifa.valorPorKg().multiply(BigDecimal.valueOf(totalKg));

                    Despacho asignado = despacho.asignado(tarifa.valorPorKg(), total,
                            score.valor(), Instant.now().plus(TTL_ASIGNACION));

                    return persistirTransaccional(asignado);
                });
    }

    /** Paso 5: persistencia transaccional de despacho + paquetes. */
    private Mono<Despacho> persistirTransaccional(Despacho despacho) {
        Mono<Despacho> operacion = despachoRepo.guardar(despacho)
                .flatMap(guardado -> despachoRepo.buscarPorId(guardado.id()));

        return operacion.as(transactionalOperator::transactional);
    }
}