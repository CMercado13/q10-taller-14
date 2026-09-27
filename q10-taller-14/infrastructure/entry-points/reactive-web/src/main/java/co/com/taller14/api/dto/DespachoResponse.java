package co.com.taller14.api.dto;

import co.com.taller14.model.despacho.Despacho;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record DespachoResponse(
        Long id,
        Long clienteId,
        String ciudad,
        String estado,
        BigDecimal tarifa,
        BigDecimal total,
        Integer scoreRiesgo,
        String trazaId,
        Instant creadoEn,
        Instant expiraEn,
        List<PaqueteResponse> paquetes
) {
    public record PaqueteResponse(Long id, Long vehiculoId, int pesoKg) {
    }

    public static DespachoResponse desde(Despacho d) {
        List<PaqueteResponse> paquetes = d.paquetes() == null ? List.of() :
                d.paquetes().stream()
                        .map(p -> new PaqueteResponse(p.id(), p.vehiculoId(), p.pesoKg()))
                        .toList();

        return new DespachoResponse(d.id(), d.clienteId(), d.ciudad(), d.estado().name(),
                d.tarifa(), d.total(), d.scoreRiesgo(), d.trazaId(), d.creadoEn(), d.expiraEn(), paquetes);
    }
}