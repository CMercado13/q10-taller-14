package co.com.taller14.api.config;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Component
public class TrazaWebFilter implements WebFilter {

    private static final String TRACE_ID_HEADER = "X-Traza-Id";
    private static final String TRACE_ID_CONTEXT = "traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = Optional.ofNullable(
                exchange.getRequest().getHeaders().getFirst(TRACE_ID_HEADER)
        ).orElseGet(() -> UUID.randomUUID().toString());

        return chain.filter(exchange)
                .contextWrite(context -> context.put(TRACE_ID_CONTEXT, traceId));
    }
}