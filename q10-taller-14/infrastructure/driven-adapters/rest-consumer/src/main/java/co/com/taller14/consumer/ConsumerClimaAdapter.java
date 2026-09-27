package co.com.taller14.consumer;

import co.com.taller14.model.transportista.Clima;
import co.com.taller14.model.transportista.gateway.ClimaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ConsumerClimaAdapter implements ClimaGateway {

    private final WebClient webClient;

    @Override
    public Mono<Clima> consultar(String ciudad) {
        return webClient.get()
                .uri("/clima/{ciudad}", ciudad)
                .retrieve()
                .bodyToMono(ClimaResponse.class)
                .map(r -> new Clima(ciudad, r.ventana(), r.factorDemora()))
                .cache(Duration.ofMinutes(10));
    }

    private record ClimaResponse(String ventana, double factorDemora) {
    }
}
