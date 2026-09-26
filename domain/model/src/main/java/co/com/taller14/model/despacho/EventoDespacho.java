package co.com.taller14.model.despacho;

import java.time.Instant;

public record EventoDespacho(Long ordenId, EstadoDespacho estado, String mensaje, Instant timestamp) {

    public static EventoDespacho de(Despacho despacho, String mensaje) {
        return new EventoDespacho(despacho.id(), despacho.estado(), mensaje, Instant.now());
    }

    /**
     * estado == null identifica un heartbeat.
     */
    public static EventoDespacho heartbeat(Long ordenId) {
        return new EventoDespacho(ordenId, null, "heartbeat", Instant.now());
    }
}