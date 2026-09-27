package com.logistica.despachos.infrastructure.events;

import com.logistica.despachos.domain.model.DespachoEvento;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class EventoBusConfig {

    @Bean
    public Sinks.Many<DespachoEvento> despachoEventoSink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }
}