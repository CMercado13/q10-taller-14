package co.com.taller14.usecase.ciudades;

import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.model.reporte.gateway.ReporteRepositoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class ReportarCiudadesUseCase {

    private final ReporteRepositoryGateway reporteRepositoryGateway;
    private static final int RATE = 256;

    public Flux<ReporteCiudad> reporteTotal() {
        return reporteRepositoryGateway.agregarPorCiudad()
                .limitRate(RATE);
    }

    /**
     * Acumulado en vivo: usa scan para emitir totales parciales a medida que llegan filas.
     */
    public Flux<ReporteCiudad> reporteEnVivo() {
        ConcurrentHashMap<String, ReporteCiudad> acumulado = new ConcurrentHashMap<>();
        return reporteRepositoryGateway.agregarPorCiudad()
                .limitRate(RATE)
                .scan((acc, actual) -> actual) // placeholder de composición; el acumulado real ocurre abajo
                .map(r -> acumulado.merge(r.ciudad(), r,
                        (viejo, nuevo) -> viejo.acumular(nuevo.totalKg(), nuevo.totalValor())))
                .map(r -> acumulado.getOrDefault(r.ciudad(), ReporteCiudad.vacio(r.ciudad())));
    }
}