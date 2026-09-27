package com.logistica.despachos.domain.exception;

public class DespachoNoExisteException extends DominioException {
    public DespachoNoExisteException(Long id) {
        super("El despacho con id " + id + " no existe");
    }

    @Override
    public String codigo() {
        return "DESPACHO_NO_EXISTE";
    }
}
