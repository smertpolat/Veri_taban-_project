package com.otel.entity;

import java.time.OffsetDateTime;

public record OdaDurumu(
        long id,
        String numara,
        int kat,
        String odaTipi,
        String durum,
        OffsetDateTime sonTemizlikZamani
) {
}
