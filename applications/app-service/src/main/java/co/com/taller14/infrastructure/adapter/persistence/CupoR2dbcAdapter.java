package co.com.taller14.infrastructure.adapter.persistence;

import co.com.taller14.model.cupo.gateways.CupoGateway;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

/**
 * Reserva y libera cupo de forma atómica con UPDATE ... WHERE ... RETURNING sobre DatabaseClient.
 * No hay lectura-luego-escritura: la condición cupo_kg >= :peso se evalúa en la misma sentencia,
 * así que dos reservas concurrentes sobre el mismo vehículo nunca dejan cupo_kg negativo.
 */
@Repository
public class CupoR2dbcAdapter implements CupoGateway {

    private final DatabaseClient databaseClient;

    public CupoR2dbcAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Integer> reservar(Long vehiculoId, Integer pesoKg) {
        return databaseClient.sql(
                        "UPDATE vehiculo SET cupo_kg = cupo_kg - :peso " +
                                "WHERE id = :id AND cupo_kg >= :peso RETURNING cupo_kg")
                .bind("peso", pesoKg)
                .bind("id", vehiculoId)
                .map(row -> row.get("cupo_kg", Integer.class))
                .first();
    }

    @Override
    public Mono<Void> liberar(Long vehiculoId, Integer pesoKg) {
        return databaseClient.sql("UPDATE vehiculo SET cupo_kg = cupo_kg + :peso WHERE id = :id")
                .bind("peso", pesoKg)
                .bind("id", vehiculoId)
                .fetch()
                .rowsUpdated()
                .then();
    }
}
