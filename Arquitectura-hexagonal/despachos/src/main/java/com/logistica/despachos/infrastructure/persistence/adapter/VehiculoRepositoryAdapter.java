package com.logistica.despachos.infrastructure.persistence.adapter;

import com.logistica.despachos.domain.model.Vehiculo;
import com.logistica.despachos.domain.port.out.VehiculoRepositoryPort;
import com.logistica.despachos.infrastructure.persistence.entity.VehiculoEntity;
import com.logistica.despachos.infrastructure.persistence.mapper.EntityMapper;
import com.logistica.despachos.infrastructure.persistence.repository.VehiculoDataRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class VehiculoRepositoryAdapter implements VehiculoRepositoryPort {

    private final VehiculoDataRepository vehiculoData;

    public VehiculoRepositoryAdapter(VehiculoDataRepository vehiculoData) {
        this.vehiculoData = vehiculoData;
    }

    @Override
    public Mono<Vehiculo> reservarCupo(Long vehiculoId, int pesoKg) {
        return vehiculoData.reservarCupo(vehiculoId, pesoKg)
                .map(EntityMapper::toDomain);
        // Empty Mono si no hay cupo suficiente -> el caso de uso lo traduce a CupoInsuficienteException
    }

    @Override
    public Mono<Vehiculo> liberarCupo(Long vehiculoId, int pesoKg) {
        return vehiculoData.liberarCupo(vehiculoId, pesoKg)
                .map(EntityMapper::toDomain);
    }

    @Override
    public Mono<Vehiculo> consumirReservado(Long vehiculoId, int pesoKg) {
        return vehiculoData.consumirReservado(vehiculoId, pesoKg)
                .map(EntityMapper::toDomain);
    }

    @Override
    public Mono<Vehiculo> buscarPorId(Long id) {
        return vehiculoData.findById(id).map(EntityMapper::toDomain);
    }

    @Override
    public Mono<Vehiculo> upsert(Vehiculo vehiculo) {
        VehiculoEntity entity = EntityMapper.toEntity(vehiculo);
        return vehiculoData.save(entity)
                .map(EntityMapper::toDomain)
                .onErrorResume(DataIntegrityViolationException.class,
                        ex -> vehiculoData.findById(vehiculo.id()).map(EntityMapper::toDomain));
    }
}