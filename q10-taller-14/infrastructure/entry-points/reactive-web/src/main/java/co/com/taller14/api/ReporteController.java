package co.com.taller14.api;

import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.usecase.ciudades.ReportarCiudadesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Flux<ReporteCiudad>> ciudades() {
        Flux<ReporteCiudad> stream = reportarUseCase.reporteTotal();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON) // Convierte el Flux en un arreglo JSON tradicional [...]
                .body(stream);
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public ResponseEntity<Flux<ReporteCiudad>> stream() {
        Flux<ReporteCiudad> stream = reportarUseCase.reporteEnVivo();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_NDJSON) // Mantiene la conexión abierta emitiendo JSON por líneas
                .body(stream);
    }
}
