package co.com.taller14.consumer;

import co.com.taller14.model.transportista.ScoreRiesgo;
import co.com.taller14.model.transportista.gateway.ScoringGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ConsumerScoreAdapter implements ScoringGateway {

    private final WebClient webClient;

    @Override
    public Mono<ScoreRiesgo> consultar(String ciudad) {
        return webClient.get()
                .uri("/scoring/{ciudad}", ciudad)
                .retrieve()
                .bodyToMono(ScoringResponse.class)
                .map(r -> new ScoreRiesgo(ciudad, r.score()))
                .timeout(Duration.ofMillis(800));
    }

    private record ScoringResponse(int score) {
    }
}

