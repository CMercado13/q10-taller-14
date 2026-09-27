package com.logistica.despachos.domain.port.in;

import com.logistica.despachos.domain.model.ReporteCiudad;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReportarCiudadesUseCase {
    Flux<ReporteCiudad> reporteTotal();

    Flux<ReporteCiudad> reporteEnVivo();
}