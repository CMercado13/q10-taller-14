package com.logistica.despachos.domain.port.in;

import com.logistica.despachos.domain.model.Despacho;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CrearDespachoUseCase {

    Mono<Despacho> crear(Comando comando);

    record ItemPaquete(Long vehiculoId, int pesoKg) {}

    record Comando(Long clienteId, String ciudad, List<ItemPaquete> paquetes,
                   String trazaId, String idemKey) {}
}