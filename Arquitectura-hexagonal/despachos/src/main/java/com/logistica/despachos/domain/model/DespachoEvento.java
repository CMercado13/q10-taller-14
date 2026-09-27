package com.logistica.despachos.domain.model;

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

    public boolean esEstadoTerminal() {
        return estado == EstadoDespacho.ENTREGADO
                || estado == EstadoDespacho.RECHAZADO
                || estado == EstadoDespacho.EXPIRADO;
    }
}