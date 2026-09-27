package com.logistica.despachos.domain.exception;

public abstract class DominioException extends RuntimeException {
    protected DominioException(String mensaje) {
        super(mensaje);
    }

    public abstract String codigo();
}