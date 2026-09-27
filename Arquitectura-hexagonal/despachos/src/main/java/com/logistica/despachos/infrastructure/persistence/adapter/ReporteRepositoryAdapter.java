package com.logistica.despachos.infrastructure.persistence.adapter;

import com.logistica.despachos.domain.model.ReporteCiudad;
import com.logistica.despachos.domain.port.out.ReporteRepositoryPort;
import com.logistica.despachos.infrastructure.persistence.repository.ReporteDataRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
public class ReporteRepositoryAdapter implements ReporteRepositoryPort {

    private final ReporteDataRepository reporteData;

    public ReporteRepositoryAdapter(ReporteDataRepository reporteData) {
        this.reporteData = reporteData;
    }

    @Override
    public Flux<ReporteCiudad> agregarPorCiudad() {
        return reporteData.agregarPorCiudad();
    }
}