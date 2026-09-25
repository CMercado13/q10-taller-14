package co.com.taller14.usecase.vehiculo;

import co.com.taller14.model.exceptions.ValidacionException;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class VehiculoUseCase {

    private static final int TAMANO_LOTE_BULK = 500;

    private final VehiculoRepository vehiculoRepository;

    public VehiculoUseCase(VehiculoRepository vehiculoRepository) {
        this.vehiculoRepository = vehiculoRepository;
    }

    public Mono<Vehiculo> crear(Vehiculo vehiculo) {
        if (vehiculo.placa() == null || vehiculo.placa().isBlank()) {
            return Mono.error(new ValidacionException("La placa es obligatoria"));
        }
        return vehiculoRepository.guardar(vehiculo);
    }

    public Flux<Vehiculo> listar() {
        return vehiculoRepository.todos();
    }

    /**
     * Carga masiva NDJSON en lotes de 500 (INSERT ... ON CONFLICT (id) DO UPDATE).
     */
    public Mono<Long> cargarMasivo(Flux<Vehiculo> vehiculos) {
        return vehiculoRepository.upsertLote(vehiculos, TAMANO_LOTE_BULK);
    }
}
