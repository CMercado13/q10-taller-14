package co.com.taller14.api;

import co.com.taller14.model.despacho.DespachoEvento;
import co.com.taller14.model.reporte.ReporteCiudad;
import co.com.taller14.model.vehiculo.Vehiculo;
import co.com.taller14.usecase.ciudades.ReportarCiudadesUseCase;
import co.com.taller14.usecase.despacho.EventosDespachoUseCase;
import co.com.taller14.usecase.vehiculo.VehiculoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ReporteController.class, VehiculoController.class, TableroController.class})
@WebFluxTest
class ReportesYVehiculosControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private ReportarCiudadesUseCase reportarCiudadesUseCase;
    @MockitoBean
    private VehiculoUseCase vehiculoUseCase;
    @MockitoBean
    private EventosDespachoUseCase eventosDespachoUseCase;

    @Test
    void exponeReporteJsonNdjsonBulkYTablero() {
        when(reportarCiudadesUseCase.reporteTotal())
                .thenReturn(Flux.just(new ReporteCiudad("BOG", 10, BigDecimal.TEN, 1)));
        when(reportarCiudadesUseCase.reporteEnVivo())
                .thenReturn(Flux.just(new ReporteCiudad("MDE", 4, BigDecimal.ONE, 1)));
        when(vehiculoUseCase.cargarMasivo(any())).thenReturn(Mono.just(new Vehiculo.ResumenCarga(1, 500)));
        when(eventosDespachoUseCase.suscribirGlobal()).thenReturn(Flux.just(DespachoEvento.heartbeat(1L)));

        client.get().uri("/api/reports/ciudades")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].ciudad").isEqualTo("BOG");

        client.get().uri("/api/reports/ciudades/stream")
                .accept(MediaType.APPLICATION_NDJSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
                .expectBody(String.class)
                .value(body -> org.junit.jupiter.api.Assertions.assertTrue(body.contains("MDE")));

        client.post().uri("/api/vehiculos/bulk")
                .contentType(MediaType.APPLICATION_NDJSON)
                .bodyValue("{\"id\":1,\"placa\":\"ABC123\",\"ciudad\":\"BOG\",\"cupoKg\":10,\"reservadoKg\":0}\n")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.procesados").isEqualTo(1);

        client.get().uri("/api/ops/tablero")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM);
    }
}
