package co.com.taller14.events;

import co.com.taller14.events.config.EventoBusConfig;
import co.com.taller14.model.despacho.DespachoEvento;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventoPublisherAdapterTest {

    @Test
    void dosSuscriptoresRecibenElMismoEvento() {
        Sinks.Many<DespachoEvento> sink = new EventoBusConfig().despachoEventoSink();
        EventoPublisherAdapter adapter = new EventoPublisherAdapter(sink);
        DespachoEvento evento = DespachoEvento.heartbeat(3L);
        List<DespachoEvento> primero = new CopyOnWriteArrayList<>();
        List<DespachoEvento> segundo = new CopyOnWriteArrayList<>();

        adapter.eventosOrden().subscribe(primero::add);
        adapter.eventosOrden().subscribe(segundo::add);
        adapter.publicar(evento);

        assertEquals(List.of(evento), primero);
        assertEquals(List.of(evento), segundo);
    }
}
