package com.logistica.despachos.infrastructure.external.simulator;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/external/simulator")
public class SimuladorController {

    private final SimuladorState state;

    public SimuladorController(SimuladorState state) {
        this.state = state;
    }

    @GetMapping
    public Mono<Map<String, Object>> obtenerEstado() {
        var c = state.config();
        return Mono.just(Map.of(
                "fallarTarifa", c.fallarTarifa.get(),
                "latenciaClimaMs", c.latenciaClimaMs.get(),
                "colgarScoring", c.colgarScoring.get(),
                "scoreFijo", c.scoreFijo.get()
        ));
    }

    @PutMapping
    public Mono<Map<String, Object>> actualizar(@RequestBody Map<String, Object> body) {
        var c = state.config();
        if (body.containsKey("fallarTarifa")) c.fallarTarifa.set((Boolean) body.get("fallarTarifa"));
        if (body.containsKey("latenciaClimaMs")) c.latenciaClimaMs.set(((Number) body.get("latenciaClimaMs")).longValue());
        if (body.containsKey("colgarScoring")) c.colgarScoring.set((Boolean) body.get("colgarScoring"));
        if (body.containsKey("scoreFijo")) c.scoreFijo.set(((Number) body.get("scoreFijo")).intValue());
        return obtenerEstado();
    }

    @DeleteMapping
    public Mono<Void> reset() {
        state.reset();
        return Mono.empty();
    }
}