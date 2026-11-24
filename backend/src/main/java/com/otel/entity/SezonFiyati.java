package com.otel.entity;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record SezonFiyati(
        Long id,
        @NotNull @Positive Long odaTipiId,
        @NotNull LocalDate baslangicTarihi,
        @NotNull LocalDate bitisTarihi,
        @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal gecelikFiyat
) {
}
