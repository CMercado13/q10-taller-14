package co.com.taller14.infrastructure.adapter.persistence;

import co.com.taller14.infrastructure.adapter.persistence.entity.PaqueteEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface PaqueteR2dbcRepository extends ReactiveCrudRepository<PaqueteEntity, Long> {

    Flux<PaqueteEntity> findByDespachoId(Long despachoId);
}
