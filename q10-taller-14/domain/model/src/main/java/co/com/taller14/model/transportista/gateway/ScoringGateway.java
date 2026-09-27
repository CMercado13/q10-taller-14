package co.com.taller14.model.transportista.gateway;

import co.com.taller14.model.transportista.ScoreRiesgo;
import reactor.core.publisher.Mono;

public interface ScoringGateway {

    Mono<ScoreRiesgo> consultar(String ciudad);
}