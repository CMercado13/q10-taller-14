package co.com.taller14.model.exceptions;

public class DespachoNoExisteException extends DomainException {
    public DespachoNoExisteException(Long id) {
        super("El despacho " + id + " no existe");
    }

    @Override
    public String codigo() {
        return "DESPACHO_NO_EXISTE";
    }
}
