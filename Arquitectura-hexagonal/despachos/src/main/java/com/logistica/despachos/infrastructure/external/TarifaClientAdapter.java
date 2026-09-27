package com.logistica.despachos.infrastructure.external;

import com.logistica.despachos.domain.model.Tarifa;
import com.logistica.despachos.domain.port.out.TarifaClientPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class TarifaClientAdapter implements TarifaClientPort {

    private final WebClient webClient;

    public TarifaClientAdapter(WebClient.Builder builder,@Value("${external.base-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public Mono<Tarifa> consultar(String ciudad) {
        return webClient.get()
                .uri("/tarifa/{ciudad}", ciudad)
                .retrieve()
                .bodyToMono(TarifaResponse.class)
                .map(r -> new Tarifa(ciudad, r.valorPorKg(), false));
    }

    private record TarifaResponse(java.math.BigDecimal valorPorKg) {}
}