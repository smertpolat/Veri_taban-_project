package com.otel.service;

import com.otel.entity.Personel;
import com.otel.entity.PersonelHesabi;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import com.otel.repository.PersonelYonetimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PersonelYonetimService {
    private final PersonelYonetimRepository kayitlar;
    private final PasswordEncoder parolalar;

    public PersonelYonetimService(PersonelYonetimRepository kayitlar, PasswordEncoder parolalar) {
        this.kayitlar = kayitlar;
        this.parolalar = parolalar;
    }

    public List<Personel> listele() {
        return kayitlar.listele();
    }

    public long ekle(Personel veri) {
        return kayitlar.ekle(veri);
    }

    @Transactional
    public void guncelle(long id, Personel veri) {
        personelKontrol(id);
        if (kayitlar.guncelle(id, veri) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
        kayitlar.rolGuncelle(id, veri.gorev());
    }

    public List<PersonelHesabi> hesaplar() {
        return kayitlar.hesaplar();
    }

    @Transactional
    public long hesapKaydet(long id, String ad, String parola) {
        String gorev = personelKontrol(id);
        if ("kat_gorevlisi".equals(gorev)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Kat görevlisi için giriş rolü tanımlı değil.");
        }
        if (parola.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parola en fazla 72 UTF-8 baytı olabilir.");
        }
        return kayitlar.hesapKaydet(id, ad, parolalar.encode(parola), gorev);
    }

    @Transactional
    public void hesapSil(long id) {
        personelKontrol(id);
        if (kayitlar.hesapSil(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Hesap bulunamadı.");
        }
    }

    private String personelKontrol(long id) {
        List<String> gorevler = kayitlar.goreviKilitle(id);
        if (gorevler.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Personel bulunamadı.");
        }
        return gorevler.getFirst();
    }

    public void sil(long id) {
        if (kayitlar.sil(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
    }
}
