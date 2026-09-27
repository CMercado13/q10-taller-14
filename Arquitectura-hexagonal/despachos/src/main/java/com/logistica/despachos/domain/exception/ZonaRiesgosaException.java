package com.logistica.despachos.domain.exception;

public class ZonaRiesgosaException extends DominioException {
    public ZonaRiesgosaException(String ciudad, int score) {
        super("La ciudad " + ciudad + " supera el score de riesgo permitido: " + score);
    }

    @Override
    public String codigo() {
        return "ZONA_RIESGOSA";
    }
}