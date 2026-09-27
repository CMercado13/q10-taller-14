package co.com.taller14.model.reporte;

import java.math.BigDecimal;

public record ReporteCiudad(String ciudad, long totalKg, BigDecimal totalValor, long cantidadDespachos) {

    public ReporteCiudad acumular(long kg, BigDecimal valor) {
        return new ReporteCiudad(ciudad, totalKg + kg, totalValor.add(valor), cantidadDespachos + 1);
    }

    public static ReporteCiudad vacio(String ciudad) {
        return new ReporteCiudad(ciudad, 0L, BigDecimal.ZERO, 0L);
    }
}