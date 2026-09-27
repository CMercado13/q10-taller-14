package co.com.taller14.model.despacho;

import co.com.taller14.model.paquete.Paquete;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Agregado central del dominio: una solicitud de despacho con sus paquetes.
 */
public record Despacho(
        Long id,
        Long clienteId,
        String ciudad,
        EstadoDespacho estado,
        BigDecimal tarifa,
        BigDecimal total,
        Integer scoreRiesgo,
        String trazaId,
        String idemKey,
        Instant creadoEn,
        Instant expiraEn,
        List<Paquete> paquetes
) {

    public static Despacho nuevo(Long clienteId, String ciudad, String trazaId,
                                 String idemKey, List<Paquete> paquetes) {
        return new Despacho(null, clienteId, ciudad, EstadoDespacho.RECIBIDO,
                null, null, null, trazaId, idemKey, Instant.now(), null, paquetes);
    }

    public Despacho conEstado(EstadoDespacho nuevoEstado) {
        return new Despacho(id, clienteId, ciudad, nuevoEstado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conId(Long id) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conTarifaYTotal(BigDecimal nuevaTarifa, BigDecimal nuevoTotal, Integer nuevoScore, Instant expiraEn) {
        return new Despacho(id, clienteId, ciudad, estado, nuevaTarifa, nuevoTotal, nuevoScore,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conExpiracion(Instant nuevaExpiracion) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, nuevaExpiracion, paquetes);
    }

    public Despacho conPaquetes(List<Paquete> nuevosPaquetes) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, nuevosPaquetes);
    }

    public record ItemPaquete(Long vehiculoId, int pesoKg) {
    }

    public record Comando(Long clienteId, String ciudad, List<ItemPaquete> paquetes,
                          String trazaId, String idemKey) {
    }

}
