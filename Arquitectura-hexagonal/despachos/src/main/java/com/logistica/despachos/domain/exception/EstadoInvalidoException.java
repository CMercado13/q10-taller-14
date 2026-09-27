package com.logistica.despachos.domain.exception;

public class EstadoInvalidoException extends DominioException {
    public EstadoInvalidoException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "ESTADO_INVALIDO";
    }
}