package com.logistica.despachos.infrastructure.persistence.repository;

import com.logistica.despachos.infrastructure.persistence.entity.DespachoEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DespachoDataRepository extends ReactiveCrudRepository<DespachoEntity, Long> {

    Mono<DespachoEntity> findByIdemKey(String idemKey);

    @Query("UPDATE despacho SET estado = :estado WHERE id = :id RETURNING *")
    Mono<DespachoEntity> actualizarEstado(Long id, String estado);

    @Query("SELECT * FROM despacho WHERE estado = :estado AND expira_en < :ahora")
    Flux<DespachoEntity> buscarVencidos(String estado, Instant ahora);
}