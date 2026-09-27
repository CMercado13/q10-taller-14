package co.com.taller14.model.despacho.gateways;

import co.com.taller14.model.despacho.Despacho;
import reactor.core.publisher.Mono;

public interface DespachoTransactionalGateway {

    Mono<Despacho> executeOperationTransactional(Mono<Despacho> operation);
}
