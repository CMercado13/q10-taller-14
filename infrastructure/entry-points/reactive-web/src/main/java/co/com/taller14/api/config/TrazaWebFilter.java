package co.com.taller14.api.config;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TrazaWebFilter implements WebFilter {

    private static final String TRACE_ID_HEADER = "X-Traza-Id";
    private static final String TRACE_ID_CONTEXT = "trazaId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER)
        ).orElseGet(() -> UUID.randomUUID().toString());

        return chain.filter(exchange)
                .contextWrite(context -> context.put(TRACE_ID_CONTEXT, traceId));
    }
}