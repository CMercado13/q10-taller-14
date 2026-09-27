package co.com.taller14.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VehiculoRequest(Long id,
                              @NotBlank String placa,
                              @NotBlank String ciudad,
                              @NotNull @PositiveOrZero Integer cupoKg) {
}
