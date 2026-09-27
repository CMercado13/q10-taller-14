package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.Clima;
import reactor.core.publisher.Mono;

public interface ClimaClientPort {
    Mono<Clima> consultar(String ciudad);
}