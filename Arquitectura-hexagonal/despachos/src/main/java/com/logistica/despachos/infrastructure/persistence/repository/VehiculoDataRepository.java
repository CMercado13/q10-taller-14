package com.logistica.despachos.infrastructure.persistence.repository;

import com.logistica.despachos.infrastructure.persistence.entity.VehiculoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface VehiculoDataRepository extends ReactiveCrudRepository<VehiculoEntity, Long> {

    // Reserva atómica de cupo: la carrera se resuelve a nivel de fila en Postgres
    @Query("UPDATE vehiculo SET cupo_kg = cupo_kg - :peso, reservado_kg = reservado_kg + :peso " +
            "WHERE id = :id AND cupo_kg >= :peso RETURNING *")
    Mono<VehiculoEntity> reservarCupo(Long id, int peso);

    @Query("UPDATE vehiculo SET cupo_kg = cupo_kg + :peso, reservado_kg = reservado_kg - :peso " +
            "WHERE id = :id RETURNING *")
    Mono<VehiculoEntity> liberarCupo(Long id, int peso);

    @Query("UPDATE vehiculo SET reservado_kg = reservado_kg - :peso WHERE id = :id RETURNING *")
    Mono<VehiculoEntity> consumirReservado(Long id, int peso);
}