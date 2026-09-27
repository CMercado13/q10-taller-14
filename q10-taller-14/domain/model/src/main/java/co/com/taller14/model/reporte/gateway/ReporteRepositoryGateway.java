package co.com.taller14.model.reporte.gateway;

import co.com.taller14.model.reporte.ReporteCiudad;
import reactor.core.publisher.Flux;

public interface ReporteRepositoryGateway {

    Flux<ReporteCiudad> agregarPorCiudad();
}
