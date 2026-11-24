package com.otel.entity;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record Personel(
        Long id,
        @NotBlank @Size(max = 100) String ad,
        @NotBlank @Size(max = 100) String soyad,
        @NotNull @Pattern(regexp = "resepsiyon|kat_gorevlisi|yonetici") String gorev,
        @NotNull LocalDate iseGirisTarihi
) {
}
