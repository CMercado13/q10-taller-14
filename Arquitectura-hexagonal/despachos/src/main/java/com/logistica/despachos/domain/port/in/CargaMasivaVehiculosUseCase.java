package com.logistica.despachos.domain.port.in;

import com.logistica.despachos.domain.model.Vehiculo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CargaMasivaVehiculosUseCase {
    Mono<ResumenCarga> cargar(Flux<Vehiculo> vehiculos);

    record ResumenCarga(long procesados, long lotes) {}
}