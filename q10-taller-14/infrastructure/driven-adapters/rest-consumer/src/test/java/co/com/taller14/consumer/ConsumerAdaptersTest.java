package co.com.taller14.consumer;

import co.com.taller14.model.transportista.Clima;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsumerAdaptersTest {

    private MockWebServer server;
    private WebClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = WebClient.builder().baseUrl(server.url("/").toString()).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.close();
    }

    @Test
    void reintentaTarifaTransitoriaYNoReintenta4xx() {
        server.enqueue(error(500));
        server.enqueue(json(200, "{\"valorPorKg\":8.50}"));
        ConsumerTarifaAdapter adapter = new ConsumerTarifaAdapter(client);

        StepVerifier.create(adapter.consultar("BOG"))
                .assertNext(tarifa -> assertEquals(0, new java.math.BigDecimal("8.50").compareTo(tarifa.valorPorKg())))
                .verifyComplete();
        assertEquals(2, server.getRequestCount());

        server.enqueue(error(400));
        StepVerifier.create(adapter.consultar("MDE"))
                .expectError()
                .verify();
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void cacheaLaMismaConsultaDeClima() {
        server.enqueue(json(200, "{\"ventana\":\"AM\",\"factorDemora\":1.5}"));
        ConsumerClimaAdapter adapter = new ConsumerClimaAdapter(client);
        Mono<Clima> consulta = adapter.consultar("BOG");

        StepVerifier.create(consulta).expectNextCount(1).verifyComplete();
        StepVerifier.create(consulta).expectNextCount(1).verifyComplete();

        assertEquals(1, server.getRequestCount());
    }

    @Test
    void elScoreHaceTimeoutSiElExternoSeCuelga() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setBody("{\"score\":10}")
                .setHeadersDelay(2, TimeUnit.SECONDS));

        StepVerifier.create(new ConsumerScoreAdapter(client).consultar("CLO"))
                .expectError()
                .verify(java.time.Duration.ofSeconds(2));
    }

    private MockResponse json(int code, String body) {
        return new MockResponse()
                .setResponseCode(code)
                .addHeader("Content-Type", "application/json")
                .setBody(body);
    }

    private MockResponse error(int code) {
        return new MockResponse().setResponseCode(code).setBody("error");
    }
}
