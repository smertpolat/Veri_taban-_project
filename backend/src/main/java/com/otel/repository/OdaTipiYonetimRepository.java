package com.otel.repository;

import com.otel.entity.OdaTipi;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OdaTipiYonetimRepository {
    private final JdbcTemplate jdbc;

    public OdaTipiYonetimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<OdaTipi> listele() {
        return jdbc.query("SELECT * FROM public.oda_tipi ORDER BY id",
                (rs, row) -> new OdaTipi(rs.getLong("id"), rs.getString("ad"), rs.getInt("kapasite"), rs.getString("aciklama"), rs.getBigDecimal("temel_fiyat")));
    }

    public long ekle(OdaTipi v) {
        return jdbc.queryForObject("INSERT INTO public.oda_tipi (ad, kapasite, aciklama, temel_fiyat) VALUES (?, ?, ?, ?) RETURNING id",
                Long.class, v.ad(), v.kapasite(), v.aciklama(), v.temelFiyat());
    }

    public int guncelle(long id, OdaTipi v) {
        return jdbc.update("UPDATE public.oda_tipi SET ad = ?, kapasite = ?, aciklama = ?, temel_fiyat = ? WHERE id = ?",
                v.ad(), v.kapasite(), v.aciklama(), v.temelFiyat(), id);
    }

    public int sil(long id) {
        return jdbc.update("DELETE FROM public.oda_tipi WHERE id = ?", id);
    }
}
