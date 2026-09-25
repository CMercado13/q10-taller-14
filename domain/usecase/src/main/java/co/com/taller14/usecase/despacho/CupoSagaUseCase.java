package co.com.taller14.usecase.despacho;

import co.com.taller14.model.cupo.gateways.CupoGateway;
import co.com.taller14.model.exceptions.CupoInsuficienteException;
import co.com.taller14.model.paquete.Paquete;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Saga de asignación de cupo: reserva paquete por paquete y, si uno falla a mitad de camino,
 * compensa devolviendo el cupo ya tomado por los paquetes anteriores.
 *
 * <p>Se usa {@code concatMap} (no {@code flatMap}) a propósito: el orden importa porque la
 * compensación necesita saber exactamente qué se reservó y en qué secuencia. Con {@code flatMap}
 * la concurrencia entrelazaría los updates y la saga perdería el registro exacto de lo reservado
 * (ver DECISIONES.md, sección 1).</p>
 */
public class CupoSagaUseCase {

    private static final Logger log = Loggers.getLogger(CupoSagaUseCase.class);

    private final CupoGateway cupoGateway;

    public CupoSagaUseCase(CupoGateway cupoGateway) {
        this.cupoGateway = cupoGateway;
    }

    /**
     * Reserva cupo para cada paquete, en orden. Si alguno falla, libera lo ya reservado
     * y propaga {@link CupoInsuficienteException}.
     */
    public Mono<List<Paquete>> reservarTodo(Flux<Paquete> paquetes) {
        List<Paquete> reservados = new ArrayList<>();
        AtomicReference<Paquete> fallido = new AtomicReference<>();

        return paquetes
                .concatMap(paquete -> Mono.defer(() -> cupoGateway.reservar(paquete.vehiculoId(), paquete.pesoKg()))
                        .map(cupoRestante -> paquete)
                        .doOnNext(reservados::add)
                        .switchIfEmpty(Mono.fromRunnable(() -> fallido.set(paquete))))
                .collectList()
                .flatMap(exitosos -> {
                    Paquete quefallo = fallido.get();
                    if (quefallo != null) {
                        return compensar(reservados)
                                .then(Mono.error(new CupoInsuficienteException(quefallo.vehiculoId(), quefallo.pesoKg())));
                    }
                    return Mono.just(exitosos);
                });
    }

    /**
     * Libera el cupo reservado por la lista dada. Se usa tanto en la compensación de la saga
     * como en el rechazo por zona riesgosa y en el job de expiración.
     */
    public Mono<Void> compensar(List<Paquete> reservados) {
        return Flux.fromIterable(reservados)
                .concatMap(p -> cupoGateway.liberar(p.vehiculoId(), p.pesoKg())
                        .doOnSuccess(v -> log.info("Cupo liberado por compensacion: vehiculo={} peso={}",
                                p.vehiculoId(), p.pesoKg())))
                .then();
    }
}
