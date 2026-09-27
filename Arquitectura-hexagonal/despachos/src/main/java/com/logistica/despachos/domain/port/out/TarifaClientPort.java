package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.Tarifa;
import reactor.core.publisher.Mono;

public interface TarifaClientPort {
    Mono<Tarifa> consultar(String ciudad);
}