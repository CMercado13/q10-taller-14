package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.model.Vehiculo;
import com.logistica.despachos.domain.port.in.CargaMasivaVehiculosUseCase;
import com.logistica.despachos.domain.port.out.VehiculoRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class CargaMasivaVehiculosService implements CargaMasivaVehiculosUseCase {

    private static final int TAMANO_LOTE = 500;

    private final VehiculoRepositoryPort vehiculoRepo;

    public CargaMasivaVehiculosService(VehiculoRepositoryPort vehiculoRepo) {
        this.vehiculoRepo = vehiculoRepo;
    }

    @Override
    public Mono<ResumenCarga> cargar(Flux<Vehiculo> vehiculos) {
        return vehiculos
                .buffer(TAMANO_LOTE)
                .concatMap(this::upsertLoteConConteo)
                .reduce(new ResumenCarga(0, 0), (acc, conteoLote) ->
                        new ResumenCarga(acc.procesados() + conteoLote, acc.lotes() + 1));
    }

    // Devuelve cuántos elementos tenía el lote procesado, sin AtomicLong ni estado compartido mutable
    private Mono<Long> upsertLoteConConteo(List<Vehiculo> lote) {
        return Flux.fromIterable(lote)
                .concatMap(vehiculoRepo::upsert)
                .count();
    }
}