package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.DespachoEvento;

public interface EventoPublisherPort {
    void publicar(DespachoEvento evento);
}