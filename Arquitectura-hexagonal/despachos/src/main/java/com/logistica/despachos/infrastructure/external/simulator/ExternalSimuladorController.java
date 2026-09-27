package com.logistica.despachos.infrastructure.external.simulator;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/external")
public class ExternalSimuladorController {

    private final SimuladorState state;
    private final Random random = new Random();
    private final AtomicInteger intentosTarifa = new AtomicInteger(0);

    public ExternalSimuladorController(SimuladorState state) {
        this.state = state;
    }

    @GetMapping("/tarifa/{ciudad}")
    public Mono<Map<String, Object>> tarifa(@PathVariable String ciudad) {
        boolean falla = state.config().fallarTarifa.get() && intentosTarifa.getAndIncrement() % 3 != 2;
        if (falla) {
            return Mono.error(new RuntimeException("Fallo intermitente simulado en tarifa"));
        }
        return Mono.just(Map.of("valorPorKg", BigDecimal.valueOf(4500 + random.nextInt(1500))));
    }

    @GetMapping("/clima/{ciudad}")
    public Mono<Map<String, Object>> clima(@PathVariable String ciudad) {
        long latencia = state.config().latenciaClimaMs.get();
        Mono<Map<String, Object>> resultado = Mono.just(Map.of(
                "ventana", "08:00-18:00",
                "factorDemora", 1.0 + random.nextDouble()
        ));
        return latencia > 0 ? resultado.delayElement(Duration.ofMillis(latencia)) : resultado;
    }

    @GetMapping("/scoring/{ciudad}")
    public Mono<Map<String, Object>> scoring(@PathVariable String ciudad) {
        if (state.config().colgarScoring.get()) {
            return Mono.<Map<String, Object>>just(Map.of("score", 0)).delayElement(Duration.ofSeconds(5)); // simula cuelgue > timeout
        }
        int fijo = state.config().scoreFijo.get();
        int score = fijo >= 0 ? fijo : random.nextInt(100);
        return Mono.just(Map.of("score", score));
    }
}