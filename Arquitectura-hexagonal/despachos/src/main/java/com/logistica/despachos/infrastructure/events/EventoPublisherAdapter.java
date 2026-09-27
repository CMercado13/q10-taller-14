package com.logistica.despachos.infrastructure.events;

import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.port.out.EventoPublisherPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

@Component
public class EventoPublisherAdapter implements EventoPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(EventoPublisherAdapter.class);
    private final Sinks.Many<DespachoEvento> sink;

    public EventoPublisherAdapter(Sinks.Many<DespachoEvento> sink) {
        this.sink = sink;
    }

    @Override
    public void publicar(DespachoEvento evento) {
        Sinks.EmitResult resultado = sink.tryEmitNext(evento);
        if (resultado.isFailure()) {
            log.warn("No se pudo emitir evento [trazaId={}]: {}", evento.trazaId(), resultado);
        }
    }
}