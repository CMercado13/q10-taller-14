package co.com.taller14.model.exceptions;

public class ValidacionException extends DomainException {
    public ValidacionException(String mensaje) {
        super(mensaje);
    }

    @Override
    public String codigo() {
        return "VALIDACION";
    }
}
