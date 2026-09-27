package co.com.taller14.model.vehiculo;

/**
 * Vehículo candidato para transportar paquetes. El cupo disponible se controla
 * de forma atómica en la capa de persistencia (UPDATE ... WHERE cupo_kg >= :peso RETURNING *).
 */
public record Vehiculo(Long id, String placa, String ciudad, Integer cupoKg) {

    public Vehiculo {
        if (cupoKg != null && cupoKg < 0) {
            throw new IllegalArgumentException("cupoKg no puede ser negativo");
        }
    }
}
