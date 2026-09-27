package co.com.taller14.model.despacho.gateways;

import co.com.taller14.model.despacho.DespachoEvento;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EventPublisherGateway {

    Mono<Void> publicar(DespachoEvento evento);
    
    Flux<DespachoEvento> eventosOrden(); // hot, filtrable por ordenId
}
