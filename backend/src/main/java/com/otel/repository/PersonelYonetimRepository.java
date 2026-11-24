package com.otel.repository;

import com.otel.entity.Personel;
import com.otel.entity.PersonelHesabi;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PersonelYonetimRepository {
    private final JdbcTemplate jdbc;

    public PersonelYonetimRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Personel> listele() {
        return jdbc.query("SELECT * FROM public.personel ORDER BY id",
                (rs, row) -> new Personel(rs.getLong("id"), rs.getString("ad"), rs.getString("soyad"),
                        rs.getString("gorev"), rs.getObject("ise_giris_tarihi", LocalDate.class)));
    }

    public long ekle(Personel veri) {
        return jdbc.queryForObject("""
                INSERT INTO public.personel(ad, soyad, gorev, ise_giris_tarihi) VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, veri.ad(), veri.soyad(), veri.gorev(), veri.iseGirisTarihi());
    }

    public int guncelle(long id, Personel veri) {
        return jdbc.update("""
                UPDATE public.personel SET ad = ?, soyad = ?, gorev = ?, ise_giris_tarihi = ? WHERE id = ?
                """, veri.ad(), veri.soyad(), veri.gorev(), veri.iseGirisTarihi(), id);
    }

    public List<String> goreviKilitle(long id) {
        return jdbc.query("SELECT gorev FROM public.personel WHERE id = ? FOR UPDATE",
                (rs, row) -> rs.getString("gorev"), id);
    }

    public void rolGuncelle(long id, String rol) {
        jdbc.update("UPDATE public.kullanici SET rol = ? WHERE personel_id = ?", rol, id);
    }

    public int sil(long id) {
        return jdbc.update("DELETE FROM public.personel WHERE id = ?", id);
    }

    public List<PersonelHesabi> hesaplar() {
        return jdbc.query("SELECT id, personel_id, kullanici_adi, rol FROM public.kullanici WHERE personel_id IS NOT NULL ORDER BY id",
                (rs, row) -> new PersonelHesabi(rs.getLong("id"), rs.getLong("personel_id"),
                        rs.getString("kullanici_adi"), rs.getString("rol")));
    }

    public long hesapKaydet(long personelId, String ad, String hash, String rol) {
        return jdbc.queryForObject("""
                INSERT INTO public.kullanici(personel_id, kullanici_adi, parola_hash, rol)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (personel_id) DO UPDATE
                SET kullanici_adi = EXCLUDED.kullanici_adi, parola_hash = EXCLUDED.parola_hash, rol = EXCLUDED.rol
                RETURNING id
                """, Long.class, personelId, ad, hash, rol);
    }

    public int hesapSil(long personelId) {
        return jdbc.update("DELETE FROM public.kullanici WHERE personel_id = ?", personelId);
    }
}
