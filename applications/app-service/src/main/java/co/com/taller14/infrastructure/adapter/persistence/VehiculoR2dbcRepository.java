package co.com.taller14.infrastructure.adapter.persistence;

import co.com.taller14.infrastructure.adapter.persistence.entity.VehiculoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface VehiculoR2dbcRepository extends ReactiveCrudRepository<VehiculoEntity, Long> {
}
