package co.com.taller14.consumer;

import co.com.taller14.model.transportista.Clima;
import co.com.taller14.model.transportista.Riesgo;
import co.com.taller14.model.transportista.Tarifa;
import co.com.taller14.model.transportista.TransportistaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ConsumerAdapter implements TransportistaGateway {

    private final WebClient client;

    @Override
    public Mono<Tarifa> tarifaPorCiudad(String ciudad) {
        return null;
    }

    @Override
    public Mono<Clima> climaPorCiudad(String ciudad) {
        return null;
    }

    @Override
    public Mono<Riesgo> scoreRiesgo(String ciudad) {
        return null;
    }
}
