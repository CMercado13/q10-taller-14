package co.com.taller14.entrypoints;

import co.com.taller14.entrypoints.dto.VehiculoRequest;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.usecase.vehiculo.VehiculoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final VehiculoUseCase vehiculoUseCase;

    public VehiculoController(VehiculoUseCase vehiculoUseCase) {
        this.vehiculoUseCase = vehiculoUseCase;
    }

    @GetMapping
    public Flux<Vehiculo> listar() {
        return vehiculoUseCase.listar();
    }

    @PostMapping
    public Mono<ResponseEntity<Vehiculo>> crear(@RequestBody @Valid VehiculoRequest request) {
        Vehiculo vehiculo = new Vehiculo(request.id(), request.placa(), request.ciudad(), request.cupoKg());
        return vehiculoUseCase.crear(vehiculo).map(creado -> ResponseEntity.status(HttpStatus.CREATED).body(creado));
    }

    /**
     * Carga masiva por NDJSON (application/x-ndjson): un objeto Vehiculo por línea, en lotes
     * de 500, con upsert (INSERT ... ON CONFLICT DO UPDATE).
     */
    @PostMapping(value = "/bulk", consumes = "application/x-ndjson")
    public Mono<ResponseEntity<Map<String, Object>>> bulk(@RequestBody Flux<VehiculoRequest> vehiculos) {
        Flux<Vehiculo> dominio = vehiculos
                .map(v -> new Vehiculo(v.id(), v.placa(), v.ciudad(), v.cupoKg()));
        return vehiculoUseCase.cargarMasivo(dominio)
                .map(total -> ResponseEntity.ok(Map.<String, Object>of("procesados", total)));
    }
}
