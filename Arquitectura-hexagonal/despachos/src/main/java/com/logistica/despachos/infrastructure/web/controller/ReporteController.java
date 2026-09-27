package com.logistica.despachos.infrastructure.web.controller;

import com.logistica.despachos.domain.model.ReporteCiudad;
import com.logistica.despachos.domain.port.in.ReportarCiudadesUseCase;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reports/ciudades")
public class ReporteController {

    private final ReportarCiudadesUseCase reportarUseCase;

    public ReporteController(ReportarCiudadesUseCase reportarUseCase) {
        this.reportarUseCase = reportarUseCase;
    }

    @GetMapping
    public Flux<ReporteCiudad> ciudades() {
        return reportarUseCase.reporteTotal();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ReporteCiudad> stream() {
        return reportarUseCase.reporteEnVivo();
    }
}
