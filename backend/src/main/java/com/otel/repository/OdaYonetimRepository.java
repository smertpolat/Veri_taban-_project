package com.otel.repository;

import com.otel.entity.Oda;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OdaYonetimRepository {
    private final JdbcTemplate jdbc;

    public OdaYonetimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Oda> listele() {
        return jdbc.query("SELECT * FROM public.oda ORDER BY id",
                (rs, row) -> new Oda(rs.getLong("id"), rs.getString("numara"), rs.getInt("kat"), rs.getLong("oda_tipi_id"), rs.getString("durum")));
    }

    public long ekle(Oda v) {
        return jdbc.queryForObject("INSERT INTO public.oda (numara, kat, oda_tipi_id, durum) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, v.numara(), v.kat(), v.odaTipiId(), v.durum());
    }

    public int guncelle(long id, Oda v) {
        return jdbc.update("UPDATE public.oda SET numara = ?, kat = ?, oda_tipi_id = ?, durum = ? WHERE id = ?",
                v.numara(), v.kat(), v.odaTipiId(), v.durum(), id);
    }

    public int sil(long id) {
        return jdbc.update("DELETE FROM public.oda WHERE id = ?", id);
    }
}
