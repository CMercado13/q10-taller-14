package co.com.taller14.api.external.config;

import org.springframework.stereotype.Component;

@Component
public class SimuladorState {
    private final SimuladorConfig config = new SimuladorConfig();

    public SimuladorConfig config() {
        return config;
    }

    public void reset() {
        config.fallarTarifa.set(false);
        config.latenciaClimaMs.set(0);
        config.colgarScoring.set(false);
        config.scoreFijo.set(-1);
    }
}