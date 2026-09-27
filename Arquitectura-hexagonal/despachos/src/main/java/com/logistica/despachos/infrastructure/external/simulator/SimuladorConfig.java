package com.logistica.despachos.infrastructure.external.simulator;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class SimuladorConfig {
    public final AtomicBoolean fallarTarifa = new AtomicBoolean(false);
    public final AtomicLong latenciaClimaMs = new AtomicLong(0);
    public final AtomicBoolean colgarScoring = new AtomicBoolean(false);
    public final AtomicInteger scoreFijo = new AtomicInteger(-1); // -1 = aleatorio
}