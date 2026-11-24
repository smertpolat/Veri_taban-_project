package com.otel.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record EkHizmet(
        Long id,
        @NotBlank @Size(max = 100) String ad,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal birimFiyat
) {
}
