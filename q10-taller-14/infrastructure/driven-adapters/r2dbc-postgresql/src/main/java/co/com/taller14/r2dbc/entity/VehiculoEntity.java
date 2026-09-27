package co.com.taller14.r2dbc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("vehiculo")
@Getter
@Setter
@AllArgsConstructor
public class VehiculoEntity {

    @Id
    private Long id;
    private String placa;
    private String ciudad;

    @Column("cupo_kg")
    private Integer cupoKg;

    @Column("reservado_kg")
    private Integer reservadoKg;


    public VehiculoEntity() {
    }

}
