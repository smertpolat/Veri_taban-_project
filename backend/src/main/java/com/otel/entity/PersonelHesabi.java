package com.otel.entity;

public record PersonelHesabi(
        long id,
        long personelId,
        String kullaniciAdi,
        String rol
) {
}
