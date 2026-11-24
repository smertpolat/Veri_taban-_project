package com.otel.entity;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record Oda(
        Long id,
        @NotBlank @Size(max = 30) String numara,
        @NotNull Integer kat,
        @NotNull @Positive Long odaTipiId,
        @NotNull @Pattern(regexp = "bos|dolu|temizlikte|bakimda") String durum
) {
}
