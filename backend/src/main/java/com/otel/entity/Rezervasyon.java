package com.otel.entity;

import java.time.LocalDate;

public record Rezervasyon(long id, long odaId, LocalDate girisTarihi, LocalDate cikisTarihi,
                          int kisiSayisi, String durum) {
}
