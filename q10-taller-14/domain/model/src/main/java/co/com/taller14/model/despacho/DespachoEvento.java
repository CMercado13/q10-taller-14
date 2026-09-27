package co.com.taller14.model.despacho;

import java.time.Instant;

public record DespachoEvento(
        Long despachoId,
        EstadoDespacho estado,
        String mensaje,
        String trazaId,
        Instant instante
) {
    public static DespachoEvento de(Despacho d, String mensaje) {
        return new DespachoEvento(d.id(), d.estado(), mensaje, d.trazaId(), Instant.now());
    }


    /**
     * estado == null identifica un heartbeat.
     */
    public static DespachoEvento heartbeat(Long ordenId) {
        return new DespachoEvento(ordenId, null, "heartbeat", "No trace", Instant.now());
    }

    public boolean esEstadoTerminal() {
        return estado == EstadoDespacho.ENTREGADO
                || estado == EstadoDespacho.RECHAZADO
                || estado == EstadoDespacho.EXPIRADO;
    }
}