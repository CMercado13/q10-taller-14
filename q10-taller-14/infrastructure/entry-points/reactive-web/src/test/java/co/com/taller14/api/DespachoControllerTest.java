package co.com.taller14.api;

import co.com.taller14.api.config.TrazaIdWebFilter;
import co.com.taller14.api.exception.GlobalExceptionHandler;
import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.EstadoDespacho;
import co.com.taller14.model.exceptions.ZonaRiesgosaException;
import co.com.taller14.usecase.despacho.ConfirmarDespachoUseCase;
import co.com.taller14.usecase.despacho.ConsultarDespachoUseCase;
import co.com.taller14.usecase.despacho.CrearDespachoUseCase;
import co.com.taller14.usecase.despacho.EventosDespachoUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {DespachoController.class, GlobalExceptionHandler.class, TrazaIdWebFilter.class})
@WebFluxTest
@Import({GlobalExceptionHandler.class, TrazaIdWebFilter.class})
class DespachoControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private CrearDespachoUseCase crearDespachoUseCase;
    @MockitoBean
    private ConsultarDespachoUseCase consultarDespachoUseCase;
    @MockitoBean
    private ConfirmarDespachoUseCase confirmarDespachoUseCase;
    @MockitoBean
    private EventosDespachoUseCase eventosDespachoUseCase;

    @Test
    void creaDespachoYPropagaElTrazaIdEnElError() {
        Despacho creado = Despacho.nuevo(1L, "BOG", "taller-1", "K1", List.of()).conId(5L)
                .conEstado(EstadoDespacho.ASIGNADO);
        when(crearDespachoUseCase.crear(any())).thenReturn(Mono.just(creado));

        client.post().uri("/api/despachos")
                .header("X-Traza-Id", "taller-1")
                .header("Idempotency-Key", "K1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"clienteId\":1,\"ciudad\":\"BOG\",\"paquetes\":[{\"vehiculoId\":1,\"pesoKg\":10}]}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(5)
                .jsonPath("$.estado").isEqualTo("ASIGNADO");

        when(crearDespachoUseCase.crear(any())).thenReturn(Mono.error(new ZonaRiesgosaException("BOG", 95)));
        client.post().uri("/api/despachos")
                .header("X-Traza-Id", "taller-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"clienteId\":1,\"ciudad\":\"BOG\",\"paquetes\":[{\"vehiculoId\":1,\"pesoKg\":10}]}")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("ZONA_RIESGOSA")
                .jsonPath("$.trazaId").isEqualTo("taller-1");
    }

    @Test
    void validaElCuerpoAntesDeCrear() {
        client.post().uri("/api/despachos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"clienteId\":1,\"ciudad\":\"\",\"paquetes\":[]}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.codigo").isEqualTo("VALIDACION");
    }
}
