package co.com.taller14.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PaqueteRequest(
        @NotNull(message = "El vehículo destino es obligatorio") Long vehiculoId,
        @Min(value = 1, message = "El peso debe ser mayor a 0") int pesoKg
) {}