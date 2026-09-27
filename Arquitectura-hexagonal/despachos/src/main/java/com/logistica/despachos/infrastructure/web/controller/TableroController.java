package com.logistica.despachos.infrastructure.web.controller;

import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.port.in.SuscribirEventosDespachoUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class TableroController {

    private final SuscribirEventosDespachoUseCase eventosUseCase;

    public TableroController(SuscribirEventosDespachoUseCase eventosUseCase) {
        this.eventosUseCase = eventosUseCase;
    }

    @GetMapping(value = "/api/ops/tablero", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<DespachoEvento> tablero() {
        return eventosUseCase.suscribirGlobal();
    }
}