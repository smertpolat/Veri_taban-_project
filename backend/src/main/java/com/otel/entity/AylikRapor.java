package com.otel.entity;

import java.math.BigDecimal;

public record AylikRapor(
        long doluOdaGecesi,
        long toplamOdaGecesi,
        BigDecimal dolulukYuzdesi,
        BigDecimal gelir
) {
}
