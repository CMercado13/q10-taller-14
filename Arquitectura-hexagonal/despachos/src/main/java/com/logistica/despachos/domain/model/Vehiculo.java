package com.logistica.despachos.domain.model;

public record Vehiculo(
        Long id,
        String placa,
        String ciudad,
        int cupoKg,
        int reservadoKg
) {
    public boolean tieneCupoPara(int pesoKg) {
        return cupoKg >= pesoKg;
    }
}