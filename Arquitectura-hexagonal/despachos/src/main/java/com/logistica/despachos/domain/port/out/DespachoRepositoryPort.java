package com.logistica.despachos.domain.port.out;

import com.logistica.despachos.domain.model.Despacho;
import com.logistica.despachos.domain.model.EstadoDespacho;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

public interface DespachoRepositoryPort {
    Mono<Despacho> guardar(Despacho despacho);

    Mono<Despacho> buscarPorId(Long id);

    Mono<Despacho> buscarPorIdemKey(String idemKey);

    Mono<Despacho> actualizarEstado(Long id, EstadoDespacho estado);

    Flux<Despacho> buscarVencidos(EstadoDespacho estado, Instant ahora);

    Flux<Despacho> buscarTodosConPaquetes();
}