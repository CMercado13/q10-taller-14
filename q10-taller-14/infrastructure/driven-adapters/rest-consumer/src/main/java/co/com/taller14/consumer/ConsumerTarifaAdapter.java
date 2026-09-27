package co.com.taller14.consumer;

import co.com.taller14.model.transportista.Tarifa;
import co.com.taller14.model.transportista.gateway.TarifaGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerTarifaAdapter implements TarifaGateway {

    private final WebClient webClient;

    @Override
    public Mono<Tarifa> consultar(String ciudad) {
        return webClient.get()
                .uri("/tarifa/{ciudad}", ciudad)
                .exchangeToMono(response -> {
                    if (response.statusCode().is4xxClientError()) {
                        log.error("::consultar tarifa error: {}", response.statusCode().value());
                        return Mono.error(new Error4xx());
                    }
                    if (response.statusCode().isError()) {
                        return Mono.error(new OtherError());
                    }
                    return response.bodyToMono(TarifaResponse.class)
                            .map(r -> new Tarifa(ciudad, r.valorPorKg(), false));
                })
                .retryWhen(Retry.backoff(3, Duration.ofMillis(200))
                        // aplica el retry solo si la excepción es OtherError
                        .filter(ex -> ex instanceof OtherError)
                        // si se agotan los reintentos, retorna la excepción original OtherError
                        .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> retrySignal.failure())
                );
    }

    private record TarifaResponse(java.math.BigDecimal valorPorKg) {
    }

    private static class Error4xx extends RuntimeException {
    }

    private static class OtherError extends RuntimeException {
    }
}
