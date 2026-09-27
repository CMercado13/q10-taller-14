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

    public Despacho conEstado(EstadoDespacho nuevoEstado) {
        return new Despacho(id, clienteId, ciudad, nuevoEstado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conId(Long id) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conTarifaYTotal(BigDecimal nuevaTarifa, BigDecimal nuevoTotal, Integer nuevoScore) {
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
}
