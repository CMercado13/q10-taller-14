package com.logistica.despachos.infrastructure.external;

import com.logistica.despachos.domain.model.Clima;
import com.logistica.despachos.domain.port.out.ClimaClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class ClimaClientAdapter implements ClimaClientPort {

    private final WebClient webClient;

    public ClimaClientAdapter(WebClient.Builder builder,@Value("${external.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Clima> consultar(String ciudad) {
        return webClient.get()
                .uri("/clima/{ciudad}", ciudad)
                .retrieve()
                .bodyToMono(ClimaResponse.class)
                .map(r -> new Clima(ciudad, r.ventana(), r.factorDemora()));
    }

    private record ClimaResponse(String ventana, double factorDemora) {}
}
