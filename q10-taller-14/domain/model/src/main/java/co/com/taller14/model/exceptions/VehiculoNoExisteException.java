package co.com.taller14.model.exceptions;

public class VehiculoNoExisteException extends DomainException {
    public VehiculoNoExisteException(Long id) {
        super("El vehiculo " + id + " no existe");
    }

    @Override
    public String codigo() {
        return "VEHICULO_NO_EXISTE";
    }
}
