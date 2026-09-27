package co.com.taller14.model.transportista;

import java.math.BigDecimal;

public record Tarifa(String ciudad, BigDecimal valorPorKg, boolean esFallback) {
}