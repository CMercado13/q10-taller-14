package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.port.in.SuscribirEventosDespachoUseCase;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Service
public class EventosDespachoService implements SuscribirEventosDespachoUseCase {

    private final Flux<DespachoEvento> eventosCompartidos;

    public EventosDespachoService(Sinks.Many<DespachoEvento> sink) {
        // hot + refCount: los suscriptores comparten la misma fuente sin duplicar trabajo
        this.eventosCompartidos = sink.asFlux()
                .publish()
                .refCount(1);
    }

    @Override
    public Flux<DespachoEvento> suscribir(Long despachoId) {
        return eventosCompartidos
                .filter(evento -> evento.despachoId().equals(despachoId))
                .takeUntil(DespachoEvento::esEstadoTerminal);
    }

    @Override
    public Flux<DespachoEvento> suscribirGlobal() {
        return eventosCompartidos;
    }
}