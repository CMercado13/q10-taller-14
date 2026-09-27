package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.DespachoTransactionalGateway;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.exceptions.DespachoNoExisteException;
import co.com.taller14.model.exceptions.EstadoInvalidoException;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConfirmarDespachoUseCase {

    private final DespachoRepository despachoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final DespachoTransactionalGateway despachoTransactionalGateway;
    private final EventPublisherGateway eventPublisherGateway;

    public Mono<Despacho> confirmar(Long id) {
        Mono<Despacho> flujo = despachoRepository.porId(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)))
                .flatMap(d -> {
                    if (d.estado() != EstadoDespacho.ASIGNADO) {
                        return Mono.error(new EstadoInvalidoException(d.estado(), EstadoDespacho.ASIGNADO));
                    }
                    return Flux.fromIterable(d.paquetes())
                            .concatMap(p -> vehiculoRepository.consumirReservado(p.vehiculoId(), p.pesoKg()))
                            .then(despachoRepository.cambiarEstado(id, EstadoDespacho.EN_RUTA));
                });

        return despachoTransactionalGateway.executeOperationTransactional(flujo)
                .doOnNext(d -> eventPublisherGateway.publicar(DespachoEvento.de(d, "Despacho confirmado, EN_RUTA")));
    }
}
