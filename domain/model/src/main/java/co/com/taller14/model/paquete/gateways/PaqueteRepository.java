package co.com.taller14.model.paquete.gateways;

import co.com.taller14.model.paquete.Paquete;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaqueteRepository {

    Mono<Paquete> guardar(Paquete paquete);

    Flux<Paquete> guardarTodos(Flux<Paquete> paquetes);

    Flux<Paquete> porDespacho(Long despachoId);
}
