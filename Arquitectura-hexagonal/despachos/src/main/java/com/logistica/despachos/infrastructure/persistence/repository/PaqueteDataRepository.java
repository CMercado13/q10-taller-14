package com.logistica.despachos.infrastructure.persistence.repository;

import com.logistica.despachos.infrastructure.persistence.entity.PaqueteEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PaqueteDataRepository extends ReactiveCrudRepository<PaqueteEntity, Long> {
    Flux<PaqueteEntity> findByDespachoId(Long despachoId);
}