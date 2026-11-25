package com.otel.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record KonaklamaGecmisi(
        long rezervasyonId,
        String odaNumarasi,
        OffsetDateTime girisZamani,
        OffsetDateTime cikisZamani,
        BigDecimal odenenTutar
) {
}
