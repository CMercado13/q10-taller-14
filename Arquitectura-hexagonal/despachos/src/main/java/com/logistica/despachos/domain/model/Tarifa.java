package com.logistica.despachos.domain.model;

import java.math.BigDecimal;

public record Tarifa(String ciudad, BigDecimal valorPorKg, boolean esFallback) {
}