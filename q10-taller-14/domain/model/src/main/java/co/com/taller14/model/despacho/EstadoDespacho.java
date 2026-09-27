package co.com.taller14.model.despacho;

/**
 * Estados del ciclo de vida de un despacho.
 */
public enum EstadoDespacho {
    RECIBIDO,
    ASIGNADO,
    EN_RUTA,
    ENTREGADO,
    RECHAZADO,
    EXPIRADO;

    public boolean esTerminal() {
        return this == ENTREGADO || this == RECHAZADO || this == EXPIRADO;
    }
}
