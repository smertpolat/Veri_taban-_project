package com.otel.entity;

import java.math.BigDecimal;
import jakarta.validation.constraints.*;

public record OdaTipi(Long id, @NotBlank @Size(max = 100) String ad, @NotNull @Positive Integer kapasite, @Size(max = 2000) String aciklama, @NotNull @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal temelFiyat) {
}
