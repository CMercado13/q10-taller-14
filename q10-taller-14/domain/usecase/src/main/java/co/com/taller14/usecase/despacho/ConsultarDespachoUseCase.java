package co.com.taller14.usecase.despacho;

import co.com.taller14.model.despacho.Despacho;
import co.com.taller14.model.despacho.gateways.DespachoRepository;
import co.com.taller14.model.exceptions.DespachoNoExisteException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ConsultarDespachoUseCase {

    private final DespachoRepository despachoRepository;

    public Mono<Despacho> obtener(Long id) {
        return despachoRepository.porId(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)));
    }
}