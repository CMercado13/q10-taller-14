package co.com.taller14.model.transportista;

public record ScoreRiesgo(String ciudad, int valor) {
    public static final int UMBRAL_RECHAZO = 80;
    public static final int VALOR_POR_DEFECTO = 50;

    public boolean superaUmbral() {
        return valor > UMBRAL_RECHAZO;
    }
}