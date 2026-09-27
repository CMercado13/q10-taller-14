package com.logistica.despachos.infrastructure.web.controller;

import com.logistica.despachos.domain.model.Vehiculo;
import com.logistica.despachos.domain.port.in.CargaMasivaVehiculosUseCase;
import com.logistica.despachos.infrastructure.web.dto.VehiculoRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final CargaMasivaVehiculosUseCase cargaMasivaUseCase;

    public VehiculoController(CargaMasivaVehiculosUseCase cargaMasivaUseCase) {
        this.cargaMasivaUseCase = cargaMasivaUseCase;
    }

    @PostMapping(value = "/bulk", consumes = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<CargaMasivaVehiculosUseCase.ResumenCarga> bulk(
            @RequestBody Flux<VehiculoRequest> vehiculos) {

        Flux<Vehiculo> dominio = vehiculos.map(v ->
                new Vehiculo(v.id(), v.placa(), v.ciudad(), v.cupoKg(), v.reservadoKg()));

        return cargaMasivaUseCase.cargar(dominio);
    }
}