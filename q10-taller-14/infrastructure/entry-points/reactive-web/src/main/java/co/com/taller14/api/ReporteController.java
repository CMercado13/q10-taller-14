package co.com.taller14.api;

import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.usecase.ciudades.ReportarCiudadesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reports/ciudades")
@RequiredArgsConstructor
public class ReporteController {

    private final ReportarCiudadesUseCase reportarUseCase;

    @GetMapping
    public Flux<ReporteCiudad> ciudades() {
        return reportarUseCase.reporteTotal();
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<ReporteCiudad> stream() {
        return reportarUseCase.reporteEnVivo();
    }
}
