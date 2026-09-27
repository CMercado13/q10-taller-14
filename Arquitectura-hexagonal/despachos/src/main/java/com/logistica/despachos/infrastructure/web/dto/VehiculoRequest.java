package com.logistica.despachos.infrastructure.web.dto;

public record VehiculoRequest(Long id, String placa, String ciudad, int cupoKg, int reservadoKg) {}
