package com.logistica.despachos.domain.exception;

public class VehiculoNoExisteException extends DominioException {
    public VehiculoNoExisteException(Long id) {
        super("El vehículo con id " + id + " no existe");
    }

    @Override
    public String codigo() {
        return "VEHICULO_NO_EXISTE";
    }
}