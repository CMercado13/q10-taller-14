package com.logistica.despachos.domain.model;

public record Paquete(
        Long id,
        Long despachoId,
        Long vehiculoId,
        int pesoKg
) {
    public Paquete {
        if (pesoKg <= 0) {
            throw new IllegalArgumentException("El peso del paquete debe ser mayor a 0");
        }
    }

    public Paquete asignadoA(Long vehiculoId) {
        return new Paquete(id, despachoId, vehiculoId, pesoKg);
    }

    public Paquete conDespacho(Long despachoId) {
        return new Paquete(id, despachoId, vehiculoId, pesoKg);
    }
}