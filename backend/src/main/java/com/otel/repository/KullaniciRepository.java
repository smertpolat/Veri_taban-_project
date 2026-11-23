package com.otel.repository;

import com.otel.entity.Kullanici;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class KullaniciRepository {
    private final JdbcTemplate jdbc;

    public KullaniciRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void misafirOlustur(String ad, String soyad, String kimlikNo, String telefon, String eposta,
                               LocalDate dogumTarihi, String kullaniciAdi, String parolaHash) {
        Long misafirId = jdbc.queryForObject("""
                INSERT INTO public.misafir (ad, soyad, kimlik_no, telefon, eposta, dogum_tarihi)
                VALUES (?, ?, ?, ?, ?, ?) RETURNING id
                """, Long.class, ad, soyad, kimlikNo, telefon, eposta, dogumTarihi);
        jdbc.update("""
                INSERT INTO public.kullanici (kullanici_adi, parola_hash, rol, misafir_id)
                VALUES (?, ?, 'misafir', ?)
                """, kullaniciAdi, parolaHash, misafirId);
    }

    public Optional<Kullanici> kullaniciBul(String kullaniciAdi) {
        return jdbc.query("""
                SELECT id, kullanici_adi, parola_hash, rol FROM public.kullanici WHERE kullanici_adi = ?
                """, (rs, i) -> new Kullanici(rs.getLong("id"), rs.getString("kullanici_adi"),
                rs.getString("parola_hash"), rs.getString("rol")), kullaniciAdi).stream().findFirst();
    }
}
