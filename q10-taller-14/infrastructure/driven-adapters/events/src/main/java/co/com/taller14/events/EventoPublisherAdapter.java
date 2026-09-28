package co.com.taller14.events;

import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.despacho.gateways.EventPublisherGateway;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
@Log4j2
public class EventoPublisherAdapter implements EventPublisherGateway {

    private final Sinks.Many<DespachoEvento> sink;

    public EventoPublisherAdapter(Sinks.Many<DespachoEvento> sink) {
        this.sink = sink;
    }

    @Override
    public Flux<DespachoEvento> eventosOrden() {
        return sink.asFlux();
    }

    @Override
    public void publicar(DespachoEvento evento) {
        Sinks.EmitResult resultado = sink.tryEmitNext(evento);
        if (resultado.isFailure()) {
            log.warn("::publicar no se pudo emitir evento [trazaId={}]: {}", evento.trazaId(), resultado);
        } else {
            log.info("::publicar evento publicado: [trazaId={}]: {}", evento.trazaId(), resultado);
        }
    }
}