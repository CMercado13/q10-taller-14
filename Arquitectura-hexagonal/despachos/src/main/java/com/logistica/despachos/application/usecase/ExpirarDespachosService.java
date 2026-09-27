package com.logistica.despachos.application.usecase;

import com.logistica.despachos.domain.model.DespachoEvento;
import com.logistica.despachos.domain.model.EstadoDespacho;
import com.logistica.despachos.domain.model.Paquete;
import com.logistica.despachos.domain.port.in.ExpirarDespachosUseCase;
import com.logistica.despachos.domain.port.out.DespachoRepositoryPort;
import com.logistica.despachos.domain.port.out.EventoPublisherPort;
import com.logistica.despachos.domain.port.out.VehiculoRepositoryPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class ExpirarDespachosService implements ExpirarDespachosUseCase {

    private final DespachoRepositoryPort despachoRepo;
    private final VehiculoRepositoryPort vehiculoRepo;
    private final EventoPublisherPort eventoPublisher;

    public ExpirarDespachosService(DespachoRepositoryPort despachoRepo,
                                   VehiculoRepositoryPort vehiculoRepo,
                                   EventoPublisherPort eventoPublisher) {
        this.despachoRepo = despachoRepo;
        this.vehiculoRepo = vehiculoRepo;
        this.eventoPublisher = eventoPublisher;
    }

    @Override
    public Mono<Long> expirarVencidos() {
        return despachoRepo.buscarVencidos(EstadoDespacho.ASIGNADO, Instant.now())
                .flatMap(despacho ->
                        Flux.fromIterable(despacho.paquetes())
                                .concatMap(p -> vehiculoRepo.liberarCupo(p.vehiculoId(), p.pesoKg()))
                                .then(despachoRepo.actualizarEstado(despacho.id(), EstadoDespacho.EXPIRADO))
                                .doOnNext(d -> eventoPublisher.publicar(
                                        DespachoEvento.de(d, "Despacho expirado, cupo liberado")))
                )
                .count();
    }
}
