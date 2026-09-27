package com.logistica.despachos.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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

    public Despacho conId(Long id) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho asignado(BigDecimal tarifa, BigDecimal total, int scoreRiesgo, Instant expiraEn) {
        return new Despacho(id, clienteId, ciudad, EstadoDespacho.ASIGNADO, tarifa, total,
                scoreRiesgo, trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conEstado(EstadoDespacho nuevoEstado) {
        return new Despacho(id, clienteId, ciudad, nuevoEstado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public Despacho conPaquetes(List<Paquete> paquetes) {
        return new Despacho(id, clienteId, ciudad, estado, tarifa, total, scoreRiesgo,
                trazaId, idemKey, creadoEn, expiraEn, paquetes);
    }

    public boolean esEstadoTerminal() {
        return estado == EstadoDespacho.ENTREGADO
                || estado == EstadoDespacho.RECHAZADO
                || estado == EstadoDespacho.EXPIRADO;
    }

    public boolean estaVencido(Instant ahora) {
        return estado == EstadoDespacho.ASIGNADO && expiraEn != null && expiraEn.isBefore(ahora);
    }
}