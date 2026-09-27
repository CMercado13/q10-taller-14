package com.logistica.despachos.infrastructure.persistence.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehiculo")
public class VehiculoEntity {

    @Id
    private Long id;
    private String placa;
    private String ciudad;
    private Integer cupoKg;
    private Integer reservadoKg;

    public VehiculoEntity() {}

    public VehiculoEntity(Long id, String placa, String ciudad, Integer cupoKg, Integer reservadoKg) {
        this.id = id;
        this.placa = placa;
        this.ciudad = ciudad;
        this.cupoKg = cupoKg;
        this.reservadoKg = reservadoKg;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public Integer getCupoKg() { return cupoKg; }
    public void setCupoKg(Integer cupoKg) { this.cupoKg = cupoKg; }
    public Integer getReservadoKg() { return reservadoKg; }
    public void setReservadoKg(Integer reservadoKg) { this.reservadoKg = reservadoKg; }
}