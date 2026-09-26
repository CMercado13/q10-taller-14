package co.com.taller14.model.exceptions;

public class VehiculoNoExisteException extends DomainException {
    public VehiculoNoExisteException(Long id) {
        super("VEHICULO_NO_EXISTE", "El vehiculo " + id + " no existe");
    }
}
