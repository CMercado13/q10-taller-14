package co.com.taller14.model.exceptions;

import co.com.taller14.model.despacho.EstadoDespacho;

public class EstadoInvalidoException extends DomainException {

    public EstadoInvalidoException(EstadoDespacho estadoActual, EstadoDespacho transicionEsperada) {
        super("No se puede pasar de " + estadoActual.name() + " a " + transicionEsperada.name());
    }

    @Override
    public String codigo() {
        return "ESTADO_INVALIDO";
    }
}
