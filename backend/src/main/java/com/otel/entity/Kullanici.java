package com.otel.entity;

public record Kullanici(
        long id,
        String kullaniciAdi,
        String parolaHash,
        String rol
) {
}
