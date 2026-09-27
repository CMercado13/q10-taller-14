package com.logistica.despachos.domain.port.in;

import com.logistica.despachos.domain.model.Despacho;
import reactor.core.publisher.Mono;

public interface ConfirmarDespachoUseCase {
    Mono<Despacho> confirmar(Long id);
}
