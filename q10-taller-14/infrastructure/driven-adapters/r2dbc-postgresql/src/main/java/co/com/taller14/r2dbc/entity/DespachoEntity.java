package co.com.taller14.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("despacho")
public class DespachoEntity {

    @Id
    private Long id;

    @Column("cliente_id")
    private Long clienteId;

    private String ciudad;
    private String estado;
    private BigDecimal tarifa;
    private BigDecimal total;

    @Column("score_riesgo")
    private Integer scoreRiesgo;

    @Column("traza_id")
    private String trazaId;

    @Column("idem_key")
    private String idemKey;

    @Column("creado_en")
    private Instant creadoEn;

    @Column("expira_en")
    private Instant expiraEn;

    public DespachoEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTarifa() {
        return tarifa;
    }

    public void setTarifa(BigDecimal tarifa) {
        this.tarifa = tarifa;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getScoreRiesgo() {
        return scoreRiesgo;
    }

    public void setScoreRiesgo(Integer scoreRiesgo) {
        this.scoreRiesgo = scoreRiesgo;
    }

    public String getTrazaId() {
        return trazaId;
    }

    public void setTrazaId(String trazaId) {
        this.trazaId = trazaId;
    }

    public String getIdemKey() {
        return idemKey;
    }

    public void setIdemKey(String idemKey) {
        this.idemKey = idemKey;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(Instant expiraEn) {
        this.expiraEn = expiraEn;
    }
}
