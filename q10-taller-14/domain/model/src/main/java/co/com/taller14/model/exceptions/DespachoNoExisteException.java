package co.com.taller14.model.exceptions;

public class DespachoNoExisteException extends DomainException {
    public DespachoNoExisteException(Long id) {
        super("DESPACHO_NO_EXISTE", "El despacho " + id + " no existe");
    }
}
