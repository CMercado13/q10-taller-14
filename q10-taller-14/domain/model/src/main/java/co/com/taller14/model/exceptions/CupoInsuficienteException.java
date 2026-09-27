package co.com.taller14.model.exceptions;

public class CupoInsuficienteException extends DomainException {
    public CupoInsuficienteException(Long vehiculoId, Integer pesoKg) {
        super("CUPO_INSUFICIENTE", "El vehiculo " + vehiculoId + " no tiene cupo para " + pesoKg + " kg");
    }
}
