package com.logistica.despachos.domain.exception;

public class CupoInsuficienteException extends DominioException {
    public CupoInsuficienteException(Long vehiculoId, int pesoSolicitado) {
        super("Cupo insuficiente en vehículo " + vehiculoId + " para " + pesoSolicitado + " kg");
    }

    @Override
    public String codigo() {
        return "CUPO_INSUFICIENTE";
    }
}