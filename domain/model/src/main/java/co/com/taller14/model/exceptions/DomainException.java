package co.com.taller14.model.exceptions;

/**
 * Excepción base de dominio. El código de negocio (no HTTP) viaja en {@link #getCodigo()};
 * el mapeo a status HTTP ocurre en la capa de entrypoints (GlobalErrorHandler).
 */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
