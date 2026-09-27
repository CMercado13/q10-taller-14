package com.logistica.despachos.infrastructure.persistence.mapper;

import com.logistica.despachos.domain.model.*;
import com.logistica.despachos.infrastructure.persistence.entity.DespachoEntity;
import com.logistica.despachos.infrastructure.persistence.entity.PaqueteEntity;
import com.logistica.despachos.infrastructure.persistence.entity.VehiculoEntity;

import java.util.List;

public final class EntityMapper {

    private EntityMapper() {}

    public static DespachoEntity toEntity(Despacho d) {
        return new DespachoEntity(d.id(), d.clienteId(), d.ciudad(), d.estado().name(),
                d.tarifa(), d.total(), d.scoreRiesgo(), d.trazaId(), d.idemKey(),
                d.creadoEn(), d.expiraEn());
    }

    public static Despacho toDomain(DespachoEntity e, List<Paquete> paquetes) {
        return new Despacho(e.getId(), e.getClienteId(), e.getCiudad(),
                EstadoDespacho.valueOf(e.getEstado()), e.getTarifa(), e.getTotal(),
                e.getScoreRiesgo(), e.getTrazaId(), e.getIdemKey(), e.getCreadoEn(),
                e.getExpiraEn(), paquetes);
    }

    public static PaqueteEntity toEntity(Paquete p) {
        return new PaqueteEntity(p.id(), p.despachoId(), p.vehiculoId(), p.pesoKg());
    }

    public static Paquete toDomain(PaqueteEntity e) {
        return new Paquete(e.getId(), e.getDespachoId(), e.getVehiculoId(), e.getPesoKg());
    }

    public static VehiculoEntity toEntity(Vehiculo v) {
        return new VehiculoEntity(v.id(), v.placa(), v.ciudad(), v.cupoKg(), v.reservadoKg());
    }

    public static Vehiculo toDomain(VehiculoEntity e) {
        return new Vehiculo(e.getId(), e.getPlaca(), e.getCiudad(), e.getCupoKg(), e.getReservadoKg());
    }
}