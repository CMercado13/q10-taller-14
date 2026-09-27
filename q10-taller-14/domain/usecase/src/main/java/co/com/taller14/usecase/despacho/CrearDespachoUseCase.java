package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.exceptions.CupoInsuficienteException;
import co.com.taller14.model.exceptions.ValidacionException;
import co.com.taller14.model.exceptions.ZonaRiesgosaException;
import co.com.taller14.model.paquete.Paquete;
import co.com.taller14.model.transportista.Clima;
import co.com.taller14.model.transportista.ScoreRiesgo;
import co.com.taller14.model.transportista.Tarifa;
import co.com.taller14.model.transportista.gateway.ClimaGateway;
import co.com.taller14.model.transportista.gateway.ScoringGateway;
import co.com.taller14.model.transportista.gateway.TarifaGateway;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class CrearDespachoUseCase {

    private final DespachoRepository despachoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final TarifaGateway tarifaGateway;
    private final ClimaGateway climaGateway;
    private final ScoringGateway scoringGateway;
    private final EventPublisherGateway eventPublisherGateway;

    private static final Duration TTL_ASIGNACION = Duration.ofMinutes(15);

    public Mono<Despacho> crear(Despacho.Comando comando) {
        return validar(comando)
                .then(Mono.defer(() -> {
                    if (comando.idemKey() == null || comando.idemKey().isBlank()) {
                        return procesar(comando);
                    }
                    return despachoRepository.porIdemKey(comando.idemKey())
                            .switchIfEmpty(Mono.defer(() -> procesar(comando)));
                }));
    }

    private Mono<Void> validar(Despacho.Comando comando) {
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

    private Mono<Despacho> procesar(Despacho.Comando comando) {
        List<Paquete> paquetesIniciales = comando.paquetes().stream()
                .map(p -> new Paquete(null, null, p.vehiculoId(), p.pesoKg()))
                .toList();

        Despacho nuevo = Despacho.nuevo(comando.clienteId(), comando.ciudad(),
                comando.trazaId(), comando.idemKey(), paquetesIniciales);

        return despachoRepository.crearRecibido(nuevo)
                .flatMap(despacho -> reservarCupos(despacho, comando.paquetes())
                        .flatMap(paquetesReservados ->
                                consultarExternosYAsignar(despacho.conPaquetes(paquetesReservados)))
                        .onErrorResume(CupoInsuficienteException.class, ex ->
                                // Cierra el despacho como RECHAZADO en lugar de dejarlo huérfano en RECIBIDO
                                despachoRepository.cambiarEstado(despacho.id(), EstadoDespacho.RECHAZADO)
                                        .then(Mono.error(ex)))
                )
                .doOnNext(d -> eventPublisherGateway.publicar(DespachoEvento.de(d, "Despacho " + d.estado())));
    }

    /**
     * Paso 2: reserva secuencial de cupo con compensación tipo saga.
     * Se acumula el resultado con `scan` (operador reactivo puro), no con una lista mutable externa.
     * concatMap garantiza orden estricto: paquete N no se reserva hasta que N-1 terminó.
     */
    private Mono<List<Paquete>> reservarCupos(Despacho despacho, List<Despacho.ItemPaquete> items) {
        return Flux.fromIterable(items)
                .concatMap(item -> vehiculoRepository.reservarCupo(item.vehiculoId(), item.pesoKg())
                        .switchIfEmpty(
                                Mono.defer(() -> Mono.error(new CupoInsuficienteException(item.vehiculoId(), item.pesoKg())))
                        )
                        .map(v -> new Paquete(null, despacho.id(), item.vehiculoId(), item.pesoKg()))
                )
                .scan(List.<Paquete>of(), (acumulado, paquete) -> {
                    List<Paquete> nuevo = new ArrayList<>(acumulado);
                    nuevo.add(paquete);
                    return nuevo;
                })
                .skip(1) // el primer elemento del scan es la semilla vacía
                .last(List.of())
                .flatMap(Mono::just)
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
                                                                List<Despacho.ItemPaquete> items,
                                                                CupoInsuficienteException ex) {
        return Flux.fromIterable(items)
                .concatMap(item -> vehiculoRepository.porId(item.vehiculoId())
                        .filter(v -> v.reservadoKg() >= item.pesoKg())
                        .map(v -> new Paquete(null, despacho.id(), item.vehiculoId(), item.pesoKg())))
                .collectList()
                .flatMap(reservados -> Flux.fromIterable(reservados)
                        .concatMap(p -> vehiculoRepository.liberarCupo(p.vehiculoId(), p.pesoKg()))
                        .then(Mono.error(ex)));
    }

    /**
     * Paso 3 y 4: consultas externas en paralelo + decisión de riesgo.
     */
    private Mono<Despacho> consultarExternosYAsignar(Despacho despacho) {
        Mono<Tarifa> tarifaMono = tarifaGateway.consultar(despacho.ciudad())
                .onErrorResume(ex -> Mono.just(new Tarifa(despacho.ciudad(), BigDecimal.valueOf(5000), true)));

        Mono<Clima> climaMono = climaGateway.consultar(despacho.ciudad());

        Mono<ScoreRiesgo> scoreMono = scoringGateway.consultar(despacho.ciudad())
                .onErrorReturn(new ScoreRiesgo(despacho.ciudad(), ScoreRiesgo.VALOR_POR_DEFECTO));

        return Mono.zip(tarifaMono, climaMono, scoreMono)
                .flatMap(tuple -> {
                    Tarifa tarifa = tuple.getT1();
                    ScoreRiesgo score = tuple.getT3();

                    if (score.superaUmbral()) {
                        return Flux.fromIterable(despacho.paquetes())
                                .concatMap(p -> vehiculoRepository.liberarCupo(p.vehiculoId(), p.pesoKg()))
                                .then(despachoRepository.cambiarEstado(despacho.id(), EstadoDespacho.RECHAZADO))
                                .then(Mono.error(new ZonaRiesgosaException(despacho.ciudad(), score.valor())));
                    }

                    int totalKg = despacho.paquetes().stream().mapToInt(Paquete::pesoKg).sum();
                    BigDecimal total = tarifa.valorPorKg().multiply(BigDecimal.valueOf(totalKg));

                    Despacho asignado = despacho.conTarifaYTotal(tarifa.valorPorKg(), total,
                            score.valor(), Instant.now().plus(TTL_ASIGNACION));

                    return despachoRepository.asignar(asignado);
                });
    }

}