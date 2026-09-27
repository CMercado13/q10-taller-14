package co.com.taller14.events.config;

import co.com.taller14.model.despacho.DespachoEvento;
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