package co.com.taller14.api.support;

import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

public final class TrazaIdSupport {

    private TrazaIdSupport() {}

    public static Mono<String> actual() {
        return Mono.deferContextual(ctx -> Mono.just(obtener(ctx)));
    }

    public static String obtener(ContextView ctx) {
        return ctx.getOrDefault("trazaId", "SIN-TRAZA");
    }
}