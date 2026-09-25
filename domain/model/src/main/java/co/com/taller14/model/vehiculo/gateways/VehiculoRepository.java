package co.com.taller14.model.vehiculo.gateways;

import co.com.taller14.model.vehiculo.Vehiculo;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Puerto de persistencia para vehículos. Implementado por el adaptador R2DBC.
 */
public interface VehiculoRepository {

    Mono<Vehiculo> guardar(Vehiculo vehiculo);

    Mono<Vehiculo> porId(Long id);

    Flux<Vehiculo> todos();

    /**
     * Carga masiva tipo upsert (INSERT ... ON CONFLICT (id) DO UPDATE) en lotes.
     */
    Mono<Long> upsertLote(Flux<Vehiculo> vehiculos, int tamanoLote);
}
