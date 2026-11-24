package com.otel.repository;

import com.otel.entity.SezonFiyati;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SezonFiyatiYonetimRepository {
    private final JdbcTemplate jdbc;

    public SezonFiyatiYonetimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<SezonFiyati> listele() {
        return jdbc.query("SELECT * FROM public.sezon_fiyati ORDER BY oda_tipi_id, baslangic_tarihi, id",
                (rs, row) -> new SezonFiyati(
                        rs.getLong("id"),
                        rs.getLong("oda_tipi_id"),
                        rs.getObject("baslangic_tarihi", LocalDate.class),
                        rs.getObject("bitis_tarihi", LocalDate.class),
                        rs.getBigDecimal("gecelik_fiyat")));
    }

    public long ekle(SezonFiyati veri) {
        return jdbc.queryForObject("""
                INSERT INTO public.sezon_fiyati (oda_tipi_id, baslangic_tarihi, bitis_tarihi, gecelik_fiyat)
                VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, veri.odaTipiId(), veri.baslangicTarihi(), veri.bitisTarihi(), veri.gecelikFiyat());
    }

    public int guncelle(long id, SezonFiyati veri) {
        return jdbc.update("""
                UPDATE public.sezon_fiyati
                SET oda_tipi_id = ?, baslangic_tarihi = ?, bitis_tarihi = ?, gecelik_fiyat = ?
                WHERE id = ?
                """, veri.odaTipiId(), veri.baslangicTarihi(), veri.bitisTarihi(), veri.gecelikFiyat(), id);
    }

    public int sil(long id) {
        return jdbc.update("DELETE FROM public.sezon_fiyati WHERE id = ?", id);
    }
}
