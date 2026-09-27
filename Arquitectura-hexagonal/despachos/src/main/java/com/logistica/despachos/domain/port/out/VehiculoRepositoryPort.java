package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.Vehiculo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface VehiculoRepositoryPort {

    Mono<Vehiculo> reservarCupo(Long vehiculoId, int pesoKg);

    Mono<Vehiculo> liberarCupo(Long vehiculoId, int pesoKg);

    Mono<Vehiculo> consumirReservado(Long vehiculoId, int pesoKg);

    Mono<Vehiculo> buscarPorId(Long id);

    Mono<Vehiculo> upsert(Vehiculo vehiculo);
}