package com.otel.repository;

import com.otel.entity.Rezervasyon;
import com.otel.entity.KonaklamaGecmisi;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RezervasyonRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Rezervasyon> mapper = (rs, row) -> new Rezervasyon(rs.getLong("id"),
            rs.getLong("oda_id"), rs.getObject("giris_tarihi", LocalDate.class),
            rs.getObject("cikis_tarihi", LocalDate.class), rs.getInt("kisi_sayisi"), rs.getString("durum"));

    public RezervasyonRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Rezervasyon> listele(long kullaniciId) {
        return jdbc.query("""
                SELECT r.* FROM public.rezervasyon r
                JOIN public.kullanici k ON k.misafir_id = r.misafir_id
                WHERE k.id = ? AND k.rol = 'misafir' ORDER BY r.giris_tarihi DESC, r.id DESC
                """, mapper, kullaniciId);
    }

    public List<KonaklamaGecmisi> konaklamaGecmisi(long kullaniciId) {
        return jdbc.query("""
                SELECT r.id AS rezervasyon_id, o.numara AS oda_numarasi,
                       c.giris_zamani, c.cikis_zamani, COALESCE(SUM(p.tutar), 0) AS odenen_tutar
                FROM public.kullanici k
                JOIN public.rezervasyon r ON r.misafir_id = k.misafir_id
                JOIN public.konaklama c ON c.rezervasyon_id = r.id
                JOIN public.oda o ON o.id = r.oda_id
                LEFT JOIN public.odeme p ON p.konaklama_id = c.id
                WHERE k.id = ? AND k.rol = 'misafir' AND r.durum = 'tamamlandi'
                  AND c.cikis_zamani IS NOT NULL
                GROUP BY r.id, o.numara, c.giris_zamani, c.cikis_zamani
                ORDER BY c.cikis_zamani DESC, r.id DESC
                """, (rs, row) -> new KonaklamaGecmisi(rs.getLong("rezervasyon_id"),
                rs.getString("oda_numarasi"), rs.getObject("giris_zamani", OffsetDateTime.class),
                rs.getObject("cikis_zamani", OffsetDateTime.class), rs.getBigDecimal("odenen_tutar")), kullaniciId);
    }

    public List<Rezervasyon> olustur(long kullaniciId, long odaId, LocalDate giris, LocalDate cikis, int kisi) {
        return jdbc.query("""
                INSERT INTO public.rezervasyon (misafir_id, oda_id, giris_tarihi, cikis_tarihi, kisi_sayisi, durum)
                SELECT k.misafir_id, o.id, ?, ?, ?, 'onayli'
                FROM public.kullanici k CROSS JOIN public.oda o
                JOIN public.oda_tipi t ON t.id = o.oda_tipi_id
                WHERE k.id = ? AND k.rol = 'misafir' AND o.id = ?
                  AND t.kapasite >= ? AND o.durum <> 'bakimda'
                  AND (? > CURRENT_DATE OR o.durum = 'bos')
                RETURNING *
                """, mapper, giris, cikis, kisi, kullaniciId, odaId, kisi, giris);
    }

    public List<Rezervasyon> kilitle(long id, long kullaniciId) {
        return jdbc.query("""
                SELECT r.* FROM public.rezervasyon r
                JOIN public.kullanici k ON k.misafir_id = r.misafir_id
                WHERE r.id = ? AND k.id = ? AND k.rol = 'misafir' FOR UPDATE OF r
                """, mapper, id, kullaniciId);
    }

    public int iptal(long id) {
        return jdbc.update("""
                UPDATE public.rezervasyon SET durum = 'iptal'
                WHERE id = ? AND durum IN ('beklemede', 'onayli')
                  AND NOT EXISTS (SELECT 1 FROM public.konaklama WHERE rezervasyon_id = ?)
                """, id, id);
    }
}
