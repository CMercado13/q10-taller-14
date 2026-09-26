package co.com.taller14.usecase.despacho;

import co.com.taller14.model.cupo.gateways.CupoGateway;
import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.eventoDespachoEvento;
import co.com.taller14.model.evento.gateways.EventPublisher;
import co.com.taller14.model.exceptions.DespachoNoExisteException;
import co.com.taller14.model.exceptions.EstadoInvalidoException;
import co.com.taller14.model.exceptions.ZonaRiesgosaException;
import co.com.taller14.model.externo.Tarifa;
import co.com.taller14.model.externo.gateways.TransportistaGateway;
import co.com.taller14.model.paquete.Paquete;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Orquesta el flujo completo de un despacho: reserva de cupo (saga), consulta paralela de
 * servicios externos (Mono.zip), decisión de riesgo, persistencia transaccional y emisión de
 * eventos al bus interno.
 */
public class DespachoUseCase {

    private static final int SCORE_RIESGO_MAXIMO = 80;
    private static final Duration VIGENCIA_ASIGNACION = Duration.ofMinutes(15);

    private final DespachoRepository despachoRepository;
    private final CupoGateway cupoGateway;
    private final CupoSagaUseCase cupoSagaUseCase;
    private final TransportistaGateway transportistaGateway;
    private final EventPublisher eventPublisher;

    public DespachoUseCase(DespachoRepository despachoRepository,
                            CupoGateway cupoGateway,
                            CupoSagaUseCase cupoSagaUseCase,
                            TransportistaGateway transportistaGateway,
                            EventPublisher eventPublisher) {
        this.despachoRepository = despachoRepository;
        this.cupoGateway = cupoGateway;
        this.cupoSagaUseCase = cupoSagaUseCase;
        this.transportistaGateway = transportistaGateway;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Crea un despacho: persiste en RECIBIDO, reserva cupo paquete por paquete (saga),
     * consulta en paralelo tarifa/clima/riesgo y, si procede, asigna en una transacción.
     */
    public Mono<Despacho> crear(Despacho solicitud) {
        Mono<Despacho> existente = solicitud.idemKey() == null
                ? Mono.empty()
                : despachoRepository.porIdemKey(solicitud.idemKey());

        return existente.switchIfEmpty(Mono.defer(() -> crearNuevo(solicitud)));
    }

    private Mono<Despacho> crearNuevo(Despacho solicitud) {
        return despachoRepository.crearRecibido(solicitud.conEstado(EstadoDespacho.RECIBIDO))
                .flatMap(recibido -> {
                    Flux<Paquete> pendientes = Flux.fromIterable(solicitud.paquetes())
                            .map(p -> new Paquete(null, recibido.id(), p.vehiculoId(), p.pesoKg()));
                    return cupoSagaUseCase.reservarTodo(pendientes)
                            .flatMap(reservados -> consultarExternosYDecidir(recibido, reservados));
                });
    }

    private Mono<Despacho> consultarExternosYDecidir(Despacho recibido, List<Paquete> reservados) {
        // Las tres llamadas son independientes entre sí: se ejecutan realmente en paralelo con
        // Mono.zip. Cada gateway resuelve su propia resiliencia (retry/timeout/cache/fallback).
        return Mono.zip(
                        transportistaGateway.tarifaPorCiudad(recibido.ciudad()),
                        transportistaGateway.climaPorCiudad(recibido.ciudad()),
                        transportistaGateway.scoreRiesgo(recibido.ciudad()))
                .flatMap(tuple -> {
                    Tarifa tarifa = tuple.getT1();
                    // tuple.getT2() es la ventana de clima: no se persiste en el agregado,
                    // pero se consultó en paralelo y quedó cacheada por 10 min en el adaptador.
                    Integer score = tuple.getT3();
                    if (score > SCORE_RIESGO_MAXIMO) {
                        return cupoSagaUseCase.compensar(reservados)
                                .then(despachoRepository.cambiarEstado(recibido.id(), EstadoDespacho.RECHAZADO))
                                .flatMap(rechazado -> emitir(rechazado))
                                .then(Mono.error(new ZonaRiesgosaException(score)));
                    }
                    return asignar(recibido, reservados, tarifa, score);
                });
    }

    private Mono<Despacho> asignar(Despacho recibido, List<Paquete> reservados, Tarifa tarifa, Integer score) {
        int pesoTotal = reservados.stream().mapToInt(Paquete::pesoKg).sum();
        BigDecimal total = tarifa.valor().multiply(BigDecimal.valueOf(pesoTotal));
        Despacho paraAsignar = recibido
                .conTarifaYTotal(tarifa.valor(), total, score)
                .conExpiracion(Instant.now().plus(VIGENCIA_ASIGNACION))
                .conPaquetes(reservados)
                .conEstado(EstadoDespacho.ASIGNADO);
        return despachoRepository.asignar(paraAsignar)
                .flatMap(asignado -> emitir(asignado).thenReturn(asignado));
    }

    public Mono<Despacho> obtener(Long id) {
        return despachoRepository.porId(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)));
    }

    public Mono<Despacho> confirmar(Long id) {
        return obtener(id)
                .flatMap(despacho -> {
                    if (despacho.estado() != EstadoDespacho.ASIGNADO) {
                        return Mono.error(new EstadoInvalidoException(despacho.estado().name(), "EN_RUTA"));
                    }
                    return despachoRepository.cambiarEstado(id, EstadoDespacho.EN_RUTA)
                            .flatMap(enRuta -> emitir(enRuta).thenReturn(enRuta));
                });
    }

    private Mono<Void> emitir(Despacho despacho) {
        return eventPublisher.publicar(new DespachoEvento(despacho.id(), despacho.estado().name(),
                despacho.ciudad(), despacho.trazaId(), Instant.now()));
    }
}
