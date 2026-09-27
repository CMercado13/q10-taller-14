package com.logistica.despachos.infrastructure.persistence.adapter;

import com.logistica.despachos.domain.model.Despacho;
import com.logistica.despachos.domain.model.EstadoDespacho;
import com.logistica.despachos.domain.model.Paquete;
import com.logistica.despachos.domain.port.out.DespachoRepositoryPort;
import com.logistica.despachos.infrastructure.persistence.entity.DespachoEntity;
import com.logistica.despachos.infrastructure.persistence.mapper.EntityMapper;
import com.logistica.despachos.infrastructure.persistence.repository.DespachoDataRepository;
import com.logistica.despachos.infrastructure.persistence.repository.PaqueteDataRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class DespachoRepositoryAdapter implements DespachoRepositoryPort {

    private final DespachoDataRepository despachoData;
    private final PaqueteDataRepository paqueteData;

    public DespachoRepositoryAdapter(DespachoDataRepository despachoData, PaqueteDataRepository paqueteData) {
        this.despachoData = despachoData;
        this.paqueteData = paqueteData;
    }

    @Override
    public Mono<Despacho> guardar(Despacho despacho) {
        boolean esNuevo = despacho.id() == null;
        DespachoEntity entity = EntityMapper.toEntity(despacho);
        return despachoData.save(entity)
                .flatMap(guardado -> {
                    if (!esNuevo) {
                        // El despacho ya existía: los paquetes se insertaron en la creación inicial.
                        // Solo actualizamos campos del despacho (tarifa, total, estado, etc.), sin duplicar paquetes.
                        return Mono.just(EntityMapper.toDomain(guardado, despacho.paquetes()));
                    }
                    return guardarPaquetes(guardado.getId(), despacho.paquetes())
                            .thenReturn(EntityMapper.toDomain(guardado, despacho.paquetes()));
                });
    }

    private Mono<Void> guardarPaquetes(Long despachoId, java.util.List<Paquete> paquetes) {
        if (paquetes == null || paquetes.isEmpty()) {
            return Mono.empty();
        }
        return Flux.fromIterable(paquetes)
                .map(p -> p.conDespacho(despachoId))
                .map(EntityMapper::toEntity)
                .concatMap(paqueteData::save)
                .then();
    }

    @Override
    public Mono<Despacho> buscarPorId(Long id) {
        return despachoData.findById(id)
                .flatMap(this::conPaquetes);
    }

    @Override
    public Mono<Despacho> buscarPorIdemKey(String idemKey) {
        return despachoData.findByIdemKey(idemKey)
                .flatMap(this::conPaquetes);
    }

    @Override
    public Mono<Despacho> actualizarEstado(Long id, EstadoDespacho estado) {
        return despachoData.actualizarEstado(id, estado.name())
                .flatMap(this::conPaquetes);
    }

    @Override
    public Flux<Despacho> buscarVencidos(EstadoDespacho estado, Instant ahora) {
        return despachoData.buscarVencidos(estado.name(), ahora)
                .flatMap(this::conPaquetes);
    }

    @Override
    public Flux<Despacho> buscarTodosConPaquetes() {
        return despachoData.findAll().flatMap(this::conPaquetes);
    }

    private Mono<Despacho> conPaquetes(DespachoEntity entity) {
        return paqueteData.findByDespachoId(entity.getId())
                .map(EntityMapper::toDomain)
                .collectList()
                .map(paquetes -> EntityMapper.toDomain(entity, paquetes));
    }
}