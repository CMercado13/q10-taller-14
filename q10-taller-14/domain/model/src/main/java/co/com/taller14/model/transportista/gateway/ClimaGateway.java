package co.com.taller14.model.transportista.gateway;

import co.com.taller14.model.transportista.Clima;
import reactor.core.publisher.Mono;

public interface ClimaGateway {

    Mono<Clima> consultar(String ciudad);
}