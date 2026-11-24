package com.otel.repository;

import com.otel.entity.EkHizmet;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class EkHizmetYonetimRepository {
    private final JdbcTemplate jdbc;

    public EkHizmetYonetimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<EkHizmet> listele() {
        return jdbc.query("SELECT * FROM public.ek_hizmet ORDER BY id",
                (rs, row) -> new EkHizmet(
                        rs.getLong("id"), rs.getString("ad"), rs.getBigDecimal("birim_fiyat")));
    }

    public long ekle(EkHizmet veri) {
        return jdbc.queryForObject("INSERT INTO public.ek_hizmet (ad, birim_fiyat) VALUES (?, ?) RETURNING id",
                Long.class, veri.ad(), veri.birimFiyat());
    }

    public int guncelle(long id, EkHizmet veri) {
        return jdbc.update("UPDATE public.ek_hizmet SET ad = ?, birim_fiyat = ? WHERE id = ?",
                veri.ad(), veri.birimFiyat(), id);
    }

    public int sil(long id) {
        return jdbc.update("DELETE FROM public.ek_hizmet WHERE id = ?", id);
    }
}
