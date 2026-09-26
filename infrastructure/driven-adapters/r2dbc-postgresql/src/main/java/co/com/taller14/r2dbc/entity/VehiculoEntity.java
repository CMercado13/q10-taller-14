package co.com.taller14.r2dbc.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehiculo")
public class VehiculoEntity {

    @Id
    private Long id;
    private String placa;
    private String ciudad;

    @Column("cupo_kg")
    private Integer cupoKg;

    public VehiculoEntity() {
    }

    public VehiculoEntity(Long id, String placa, String ciudad, Integer cupoKg) {
        this.id = id;
        this.placa = placa;
        this.ciudad = ciudad;
        this.cupoKg = cupoKg;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public Integer getCupoKg() {
        return cupoKg;
    }

    public void setCupoKg(Integer cupoKg) {
        this.cupoKg = cupoKg;
    }
}
