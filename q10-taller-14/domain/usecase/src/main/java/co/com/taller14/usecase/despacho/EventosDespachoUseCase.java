package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Log
public class EventosDespachoUseCase {

    private final DespachoRepository despachoRepository;
    private final EventPublisherGateway eventPublisherGateway;

    public Flux<DespachoEvento> eventos(Long id) {
        Flux<DespachoEvento> heartbeat = Flux.interval(Duration.ofSeconds(15)).map(t -> DespachoEvento.heartbeat(id));
        return despachoRepository.porId(id).flatMapMany(despacho -> {
            Mono<DespachoEvento> actual = Mono.just(DespachoEvento.de(despacho, "estado actual"));
            Flux<DespachoEvento> vivo = eventPublisherGateway.eventosOrden().filter(e -> id.equals(e.despachoId()));
            return Flux.merge(actual, vivo, heartbeat)
                    .takeUntil(e -> e.estado() != null && e.estado().esTerminal())
                    .doOnCancel(() -> log.info("Cliente cerró stream de orden: " + id));
        });
    }

    public Flux<DespachoEvento> suscribirGlobal() {
        return eventPublisherGateway.eventosOrden();
    }
}