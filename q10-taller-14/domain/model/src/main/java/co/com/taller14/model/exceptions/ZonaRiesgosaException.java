package co.com.taller14.model.exceptions;

public class ZonaRiesgosaException extends DomainException {

    public ZonaRiesgosaException(String ciudad, int score) {
        super("La ciudad " + ciudad + " supera el score de riesgo permitido: " + score);
    }

    @Override
    public String codigo() {
        return "ZONA_RIESGOSA";
    }
}