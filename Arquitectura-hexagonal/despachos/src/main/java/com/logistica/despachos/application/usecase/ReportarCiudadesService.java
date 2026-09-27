package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.model.ReporteCiudad;
import com.logistica.despachos.domain.port.in.ReportarCiudadesUseCase;
import com.logistica.despachos.domain.port.out.ReporteRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;


import java.util.concurrent.ConcurrentHashMap;

@Service
public class ReportarCiudadesService implements ReportarCiudadesUseCase {

    private final ReporteRepositoryPort reporteRepo;
    private static final int RATE = 256;

    public ReportarCiudadesService(ReporteRepositoryPort reporteRepo) {
        this.reporteRepo = reporteRepo;
    }

    @Override
    public Flux<ReporteCiudad> reporteTotal() {
        return reporteRepo.agregarPorCiudad()
                .limitRate(RATE);
    }

    /** Acumulado en vivo: usa scan para emitir totales parciales a medida que llegan filas. */
    @Override
    public Flux<ReporteCiudad> reporteEnVivo() {
        ConcurrentHashMap<String, ReporteCiudad> acumulado = new ConcurrentHashMap<>();

        return reporteRepo.agregarPorCiudad()
                .limitRate(RATE)
                .scan((acc, actual) -> actual) // placeholder de composición; el acumulado real ocurre abajo
                .map(r -> acumulado.merge(r.ciudad(), r,
                        (viejo, nuevo) -> viejo.acumular(nuevo.totalKg(), nuevo.totalValor())))
                .map(r -> acumulado.getOrDefault(r.ciudad(), ReporteCiudad.vacio(r.ciudad())));
    }
}