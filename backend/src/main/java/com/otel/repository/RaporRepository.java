package com.otel.repository;

import com.otel.entity.AylikRapor;
import com.otel.entity.OdaDurumu;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class RaporRepository {
    private final JdbcTemplate jdbc;

    public RaporRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AylikRapor aylik(LocalDate tarih) {
        return jdbc.queryForObject("CALL public.aylik_doluluk_gelir_raporu(?, NULL, NULL, NULL, NULL)",
                (rs, row) -> new AylikRapor(
                        rs.getLong("dolu_oda_gecesi"),
                        rs.getLong("toplam_oda_gecesi"),
                        rs.getBigDecimal("doluluk_yuzdesi"),
                        rs.getBigDecimal("gelir")), tarih);
    }

    public List<OdaDurumu> odaDurumlari() {
        return jdbc.query("SELECT * FROM public.oda_guncel_durum ORDER BY numara, id",
                (rs, row) -> new OdaDurumu(
                        rs.getLong("id"),
                        rs.getString("numara"),
                        rs.getInt("kat"),
                        rs.getString("oda_tipi"),
                        rs.getString("durum"),
                        rs.getObject("son_temizlik_zamani", OffsetDateTime.class)));
    }
}
