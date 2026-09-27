package co.com.taller14.model.despacho.gateways;

import co.com.taller14.model.despacho.EventoDespacho;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventPublisherGateway {
    Mono<Void> publicar(EventoDespacho evento);
    Flux<EventoDespacho> eventosOrden(); // hot, filtrable por ordenId
}
