package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.ReporteCiudad;
import reactor.core.publisher.Flux;

public interface ReporteRepositoryPort {
    Flux<ReporteCiudad> agregarPorCiudad();
}
