package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
public class ExpirarDespachosUseCase {

    private final DespachoRepository despachoRepository;
    private final VehiculoRepository vehiculoRepository;
    private final EventPublisherGateway eventPublisherGateway;


    public Mono<Long> expirarVencidos() {
        return despachoRepository.vencidosAntesDe(Instant.now())
                .flatMap(despacho ->
                        Flux.fromIterable(despacho.paquetes())
                                .concatMap(p -> vehiculoRepository.liberarCupo(p.vehiculoId(), p.pesoKg()))
                                .then(despachoRepository.cambiarEstado(despacho.id(), EstadoDespacho.EXPIRADO))
                                .doOnNext(d -> eventPublisherGateway.publicar(DespachoEvento.de(d, "Despacho expirado, cupo liberado")))
                )
                .count();
    }
}
