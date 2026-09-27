package com.logistica.despachos.domain.port.in;

import com.logistica.despachos.domain.model.DespachoEvento;
import reactor.core.publisher.Flux;

public interface SuscribirEventosDespachoUseCase {
    Flux<DespachoEvento> suscribir(Long despachoId);

    Flux<DespachoEvento> suscribirGlobal();
}