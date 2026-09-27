package co.com.taller14.model.transportista;

import reactor.core.publisher.Mono;

public interface TransportistaGateway {

    Mono<Tarifa> tarifaPorCiudad(String ciudad);

    Mono<Clima> climaPorCiudad(String ciudad);

    Mono<Riesgo> scoreRiesgo(String ciudad);
}
