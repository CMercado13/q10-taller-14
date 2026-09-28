package co.com.taller14.model.despacho.gateways;

import co.com.taller14.model.despacho.DespachoEvento;
import reactor.core.publisher.Flux;

public interface EventPublisherGateway {

    void publicar(DespachoEvento evento);
    
    Flux<DespachoEvento> eventosOrden(); // hot, filtrable por ordenId
}
