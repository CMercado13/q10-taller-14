package co.com.taller14.infrastructure.adapter.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("paquete")
public class PaqueteEntity {

    @Id
    private Long id;

    @Column("despacho_id")
    private Long despachoId;

    @Column("vehiculo_id")
    private Long vehiculoId;

    @Column("peso_kg")
    private Integer pesoKg;

    public PaqueteEntity() {
    }

    public PaqueteEntity(Long id, Long despachoId, Long vehiculoId, Integer pesoKg) {
        this.id = id;
        this.despachoId = despachoId;
        this.vehiculoId = vehiculoId;
        this.pesoKg = pesoKg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDespachoId() {
        return despachoId;
    }

    public void setDespachoId(Long despachoId) {
        this.despachoId = despachoId;
    }

    public Long getVehiculoId() {
        return vehiculoId;
    }

    public void setVehiculoId(Long vehiculoId) {
        this.vehiculoId = vehiculoId;
    }

    public Integer getPesoKg() {
        return pesoKg;
    }

    public void setPesoKg(Integer pesoKg) {
        this.pesoKg = pesoKg;
    }
}
