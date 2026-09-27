package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.ScoreRiesgo;
import reactor.core.publisher.Mono;

public interface ScoringClientPort {
    Mono<ScoreRiesgo> consultar(String ciudad);
}