package co.com.taller14.model.exceptions;

public class CupoInsuficienteException extends DomainException {
    public CupoInsuficienteException(Long vehiculoId, Integer pesoKg) {
        super("El vehiculo " + vehiculoId + " no tiene cupo para " + pesoKg + " kg");
    }

    @Override
    public String codigo() {
        return "CUPO_INSUFICIENTE";
    }
}
