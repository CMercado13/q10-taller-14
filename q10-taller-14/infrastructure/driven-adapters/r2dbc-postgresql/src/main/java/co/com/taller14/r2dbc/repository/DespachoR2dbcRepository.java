package co.com.taller14.r2dbc.repository;

import co.com.taller14.r2dbc.entity.DespachoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DespachoR2dbcRepository extends ReactiveCrudRepository<DespachoEntity, Long> {

    Flux<DespachoEntity> findByEstadoAndExpiraEnBefore(String estado, Instant instante);

    Mono<DespachoEntity> findByIdemKey(String idemKey);
}
