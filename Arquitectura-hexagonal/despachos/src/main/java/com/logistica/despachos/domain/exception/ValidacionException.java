package com.logistica.despachos.domain.exception;

public class ValidacionException extends DominioException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "VALIDACION";
    }
}