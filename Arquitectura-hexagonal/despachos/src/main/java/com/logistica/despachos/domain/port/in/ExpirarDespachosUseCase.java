package com.logistica.despachos.domain.port.in;

import reactor.core.publisher.Mono;

public interface ExpirarDespachosUseCase {
    Mono<Long> expirarVencidos();
}