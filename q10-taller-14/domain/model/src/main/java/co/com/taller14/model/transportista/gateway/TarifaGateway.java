package co.com.taller14.model.transportista.gateway;


import co.com.taller14.model.transportista.Tarifa;
import reactor.core.publisher.Mono;

public interface TarifaGateway {

    Mono<Tarifa> consultar(String ciudad);
}