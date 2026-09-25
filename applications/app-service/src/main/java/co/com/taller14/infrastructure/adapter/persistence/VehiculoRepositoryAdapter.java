package co.com.taller14.infrastructure.adapter.persistence;

import co.com.taller14.infrastructure.adapter.persistence.entity.VehiculoEntity;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.model.vehiculo.gateways.VehiculoRepository;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public class VehiculoRepositoryAdapter implements VehiculoRepository {

    private final VehiculoR2dbcRepository repository;
    private final DatabaseClient databaseClient;

    public VehiculoRepositoryAdapter(VehiculoR2dbcRepository repository, DatabaseClient databaseClient) {
        this.repository = repository;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Vehiculo> guardar(Vehiculo vehiculo) {
        // El id de vehiculo se asigna manualmente (no es BIGSERIAL), por lo que save() de
        // Spring Data intentaria un UPDATE en vez de un INSERT. Se inserta explicitamente.
        return databaseClient.sql("INSERT INTO vehiculo (id, placa, ciudad, cupo_kg) VALUES (:id, :placa, :ciudad, :cupo)")
                .bind("id", vehiculo.id())
                .bind("placa", vehiculo.placa())
                .bind("ciudad", vehiculo.ciudad())
                .bind("cupo", vehiculo.cupoKg())
                .then()
                .then(Mono.just(vehiculo));
    }

    @Override
    public Mono<Vehiculo> porId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Flux<Vehiculo> todos() {
        return repository.findAll().map(this::toDomain);
    }

    @Override
    public Mono<Long> upsertLote(Flux<Vehiculo> vehiculos, int tamanoLote) {
        return vehiculos
                .buffer(tamanoLote)
                .concatMap(this::upsertBatch)
                .reduce(0L, Long::sum);
    }

    private Mono<Long> upsertBatch(List<Vehiculo> lote) {
        if (lote.isEmpty()) {
            return Mono.just(0L);
        }
        StringBuilder sql = new StringBuilder("INSERT INTO vehiculo (id, placa, ciudad, cupo_kg) VALUES ");
        for (int i = 0; i < lote.size(); i++) {
            if (i > 0) {
                sql.append(", ");
            }
            sql.append("(:id").append(i).append(", :placa").append(i)
                    .append(", :ciudad").append(i).append(", :cupo").append(i).append(")");
        }
        sql.append(" ON CONFLICT (id) DO UPDATE SET placa = EXCLUDED.placa, ciudad = EXCLUDED.ciudad, cupo_kg = EXCLUDED.cupo_kg");

        DatabaseClient.GenericExecuteSpec spec = databaseClient.sql(sql.toString());
        for (int i = 0; i < lote.size(); i++) {
            Vehiculo v = lote.get(i);
            spec = spec.bind("id" + i, v.id())
                    .bind("placa" + i, v.placa())
                    .bind("ciudad" + i, v.ciudad())
                    .bind("cupo" + i, v.cupoKg());
        }
        return spec.fetch().rowsUpdated();
    }

    private Vehiculo toDomain(VehiculoEntity entity) {
        return new Vehiculo(entity.getId(), entity.getPlaca(), entity.getCiudad(), entity.getCupoKg());
    }
}
