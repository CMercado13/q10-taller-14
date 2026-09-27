package com.logistica.despachos.infrastructure.external;

import com.logistica.despachos.domain.model.ScoreRiesgo;
import com.logistica.despachos.domain.port.out.ScoringClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ScoringClientAdapter implements ScoringClientPort {

    private final WebClient webClient;

    public ScoringClientAdapter(WebClient.Builder builder,@Value("${external.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<ScoreRiesgo> consultar(String ciudad) {
        return webClient.get()
                .uri("/scoring/{ciudad}", ciudad)
                .retrieve()
                .bodyToMono(ScoringResponse.class)
                .map(r -> new ScoreRiesgo(ciudad, r.score()));
    }

    private record ScoringResponse(int score) {}
}