package com.otel.repository;

import com.otel.entity.GunlukHareket;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ResepsiyonRepository {
    private final JdbcTemplate jdbc;

    public ResepsiyonRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<GunlukHareket> gunlukListe() {
        return jdbc.query("""
                SELECT * FROM public.bugunku_giris_cikis ORDER BY tur, numara, rezervasyon_id
                """, (rs, row) -> new GunlukHareket(rs.getLong("rezervasyon_id"),
                rs.getString("ad"), rs.getString("soyad"), rs.getString("numara"),
                rs.getString("tur"), rs.getObject("tarih", LocalDate.class),
                rs.getObject("konaklama_id", Long.class)));
    }

    public boolean rezervasyonuKilitle(long id) {
        return !jdbc.query("SELECT id FROM public.rezervasyon WHERE id = ? FOR UPDATE",
                (rs, row) -> rs.getLong("id"), id).isEmpty();
    }

    public List<Long> checkIn(long rezervasyonId, long kullaniciId) {
        return jdbc.query("""
                INSERT INTO public.konaklama (rezervasyon_id, giris_personel_id)
                SELECT r.id, k.personel_id
                FROM public.rezervasyon r
                JOIN public.oda o ON o.id = r.oda_id
                CROSS JOIN public.kullanici k
                WHERE r.id = ? AND k.id = ? AND k.rol = 'resepsiyon'
                  AND r.durum = 'onayli' AND o.durum = 'bos'
                  AND r.giris_tarihi <= CURRENT_DATE AND CURRENT_DATE < r.cikis_tarihi
                  AND NOT EXISTS (SELECT 1 FROM public.konaklama WHERE rezervasyon_id = r.id)
                RETURNING id
                """, (rs, row) -> rs.getLong("id"), rezervasyonId, kullaniciId);
    }
}
