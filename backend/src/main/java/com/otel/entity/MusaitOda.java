package com.otel.entity;

import java.math.BigDecimal;

public record MusaitOda(
        long id,
        String numara,
        String odaTipi,
        int kapasite,
        BigDecimal temelFiyat
) {
}
