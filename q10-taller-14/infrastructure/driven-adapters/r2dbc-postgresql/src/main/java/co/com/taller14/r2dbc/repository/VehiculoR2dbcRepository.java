package co.com.taller14.r2dbc.repository;

import co.com.taller14.r2dbc.entity.VehiculoEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface VehiculoR2dbcRepository extends ReactiveCrudRepository<VehiculoEntity, Long> {
}
