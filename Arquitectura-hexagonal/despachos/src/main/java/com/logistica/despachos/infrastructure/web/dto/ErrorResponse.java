package com.logistica.despachos.infrastructure.web.dto;

import java.time.Instant;

public record ErrorResponse(String codigo, String mensaje, String trazaId, Instant instante) {
    public static ErrorResponse de(String codigo, String mensaje, String trazaId) {
        return new ErrorResponse(codigo, mensaje, trazaId, Instant.now());
    }
}