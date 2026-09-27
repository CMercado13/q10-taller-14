package co.com.taller14.model.exceptions;

public class EstadoInvalidoException extends DomainException {
    public EstadoInvalidoException(String estadoActual, String transicionEsperada) {
        super("ESTADO_INVALIDO", "No se puede pasar de " + estadoActual + " a " + transicionEsperada);
    }
}
