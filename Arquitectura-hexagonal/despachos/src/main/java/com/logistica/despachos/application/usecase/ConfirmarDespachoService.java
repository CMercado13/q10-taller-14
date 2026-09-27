package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.exception.DespachoNoExisteException;
import com.logistica.despachos.domain.exception.EstadoInvalidoException;
import com.logistica.despachos.domain.model.Despacho;
import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.model.EstadoDespacho;
import com.logistica.despachos.domain.port.in.ConfirmarDespachoUseCase;
import com.logistica.despachos.domain.port.out.DespachoRepositoryPort;
import com.logistica.despachos.domain.port.out.EventoPublisherPort;
import com.logistica.despachos.domain.port.out.VehiculoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ConfirmarDespachoService implements ConfirmarDespachoUseCase {

    private final DespachoRepositoryPort despachoRepo;
    private final VehiculoRepositoryPort vehiculoRepo;
    private final EventoPublisherPort eventoPublisher;
    private final TransactionalOperator transactionalOperator;

    public ConfirmarDespachoService(DespachoRepositoryPort despachoRepo,
                                    VehiculoRepositoryPort vehiculoRepo,
                                    EventoPublisherPort eventoPublisher,
                                    TransactionalOperator transactionalOperator) {
        this.despachoRepo = despachoRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.eventoPublisher = eventoPublisher;
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public Mono<Despacho> confirmar(Long id) {
        Mono<Despacho> flujo = despachoRepo.buscarPorId(id)
                .switchIfEmpty(Mono.error(new DespachoNoExisteException(id)))
                .flatMap(d -> {
                    if (d.estado() != EstadoDespacho.ASIGNADO) {
                        return Mono.error(new EstadoInvalidoException(
                                "El despacho " + id + " no está en estado ASIGNADO"));
                    }
                    return Flux.fromIterable(d.paquetes())
                            .concatMap(p -> vehiculoRepo.consumirReservado(p.vehiculoId(), p.pesoKg()))
                            .then(despachoRepo.actualizarEstado(id, EstadoDespacho.EN_RUTA));
                });

        return flujo.as(transactionalOperator::transactional)
                .doOnNext(d -> eventoPublisher.publicar(DespachoEvento.de(d, "Despacho confirmado, EN_RUTA")));
    }
}
