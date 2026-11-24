package com.otel.entity;

import java.time.LocalDate;

public record GunlukHareket(
        long rezervasyonId,
        String ad,
        String soyad,
        String odaNumarasi,
        String tur,
        LocalDate tarih,
        Long konaklamaId
) {
}
