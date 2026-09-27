package co.com.taller14.r2dbc.adapter;

import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.model.reporte.gateway.ReporteRepositoryGateway;
import io.r2dbc.spi.Readable;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;

@Repository
public class ReporteRepositoryAdapter implements ReporteRepositoryGateway {

    private final DatabaseClient databaseClient;

    public ReporteRepositoryAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    private static final String SQL = """
            SELECT d.ciudad AS ciudad,
                   COALESCE(SUM(p.peso_kg), 0) AS total_kg,
                   COALESCE(SUM(d.total), 0) AS total_valor,
                   COUNT(DISTINCT d.id) AS cantidad
            FROM despacho d
            JOIN paquete p ON p.despacho_id = d.id
            GROUP BY d.ciudad
            """;

    // 100% no bloqueante: DatabaseClient.sql().map().all() devuelve Flux frío,
    // el driver r2dbc-postgresql ejecuta sobre event loop de Netty, no sobre hilos bloqueantes
    @Override
    public Flux<ReporteCiudad> agregarPorCiudad() {
        return databaseClient.sql(SQL)
                .map(this::mapRow)
                .all();
    }

    @SuppressWarnings({"DataFlowIssue"})
    private ReporteCiudad mapRow(Readable row) {
        return new ReporteCiudad(
                row.get("ciudad", String.class),
                row.get("total_kg", Long.class),
                row.get("total_valor", BigDecimal.class),
                row.get("cantidad", Long.class)
        );
    }
}