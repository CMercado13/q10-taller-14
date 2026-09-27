package co.com.taller14.api;

import co.com.taller14.api.dto.VehiculoRequest;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.usecase.vehiculo.VehiculoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {

    private final VehiculoUseCase vehiculoUseCase;

//    @PostMapping
//    public Mono<ResponseEntity<Vehiculo>> crear(@RequestBody @Valid VehiculoRequest request) {
//        Vehiculo vehiculo = new Vehiculo(request.id(), request.placa(), request.ciudad(), request.cupoKg());
//        return vehiculoUseCase.crear(vehiculo).map(creado -> ResponseEntity.status(HttpStatus.CREATED).body(creado));
//    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<Vehiculo.ResumenCarga> bulk(@RequestBody Flux<VehiculoRequest> vehiculos) {

        Flux<Vehiculo> fluxVehiculos = vehiculos.map(v ->
                new Vehiculo(v.id(), v.placa(), v.ciudad(), v.cupoKg(), v.reservadoKg()));

        return vehiculoUseCase.cargarMasivo(fluxVehiculos);
    }
}