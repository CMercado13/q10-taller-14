package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.exception.DespachoNoExisteException;
import com.logistica.despachos.domain.model.Despacho;
import com.logistica.despachos.domain.port.in.ConsultarDespachoUseCase;
import com.logistica.despachos.domain.port.out.DespachoRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class ConsultarDespachoService implements ConsultarDespachoUseCase {

    private final DespachoRepositoryPort despachoRepo;

    public ConsultarDespachoService(DespachoRepositoryPort despachoRepo) {
        this.despachoRepo = despachoRepo;
    }

    @Override
    public Mono<Despacho> obtener(Long id) {
        return despachoRepo.buscarPorId(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)));
    }
}