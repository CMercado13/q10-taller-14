package com.logistica.despachos.infrastructure.web.filter;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrazaIdWebFilter implements WebFilter {

    public static final String TRAZA_ID_KEY = "trazaId";
    public static final String HEADER = "X-Traza-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String trazaIdHeader = exchange.getRequest().getHeaders().getFirst(HEADER);
        String trazaId = (trazaIdHeader == null || trazaIdHeader.isBlank())
                ? UUID.randomUUID().toString()
                : trazaIdHeader;
        exchange.getResponse().getHeaders().add(HEADER, trazaId);

        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(TRAZA_ID_KEY, trazaId));
    }
}
