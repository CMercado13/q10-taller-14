package com.logistica.despachos.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("despacho")
public class DespachoEntity {

    @Id
    private Long id;
    private Long clienteId;
    private String ciudad;
    private String estado;
    private BigDecimal tarifa;
    private BigDecimal total;
    private Integer scoreRiesgo;
    private String trazaId;
    private String idemKey;
    private Instant creadoEn;
    private Instant expiraEn;

    public DespachoEntity() {}

    public DespachoEntity(Long id, Long clienteId, String ciudad, String estado, BigDecimal tarifa,
                          BigDecimal total, Integer scoreRiesgo, String trazaId, String idemKey,
                          Instant creadoEn, Instant expiraEn) {
        this.id = id;
        this.clienteId = clienteId;
        this.ciudad = ciudad;
        this.estado = estado;
        this.tarifa = tarifa;
        this.total = total;
        this.scoreRiesgo = scoreRiesgo;
        this.trazaId = trazaId;
        this.idemKey = idemKey;
        this.creadoEn = creadoEn;
        this.expiraEn = expiraEn;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClienteId() { return clienteId; }
    public void setClienteId(Long clienteId) { this.clienteId = clienteId; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getTarifa() { return tarifa; }
    public void setTarifa(BigDecimal tarifa) { this.tarifa = tarifa; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public Integer getScoreRiesgo() { return scoreRiesgo; }
    public void setScoreRiesgo(Integer scoreRiesgo) { this.scoreRiesgo = scoreRiesgo; }
    public String getTrazaId() { return trazaId; }
    public void setTrazaId(String trazaId) { this.trazaId = trazaId; }
    public String getIdemKey() { return idemKey; }
    public void setIdemKey(String idemKey) { this.idemKey = idemKey; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant creadoEn) { this.creadoEn = creadoEn; }
    public Instant getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Instant expiraEn) { this.expiraEn = expiraEn; }
}