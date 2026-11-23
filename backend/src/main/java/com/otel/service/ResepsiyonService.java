package com.otel.service;

import com.otel.entity.GunlukHareket;
import com.otel.entity.KonaklamaTutari;
import com.otel.repository.ResepsiyonRepository;
import java.sql.SQLException;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResepsiyonService {
    @Transactional
    public long hizmetEkle(long id, long hizmetId, int adet) {
        konaklamaKontrol(id, true);
        List<Long> sonuc = resepsiyon.hizmetEkle(id, hizmetId, adet);
        if (sonuc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ek hizmet bulunamadı.");
        }
        return sonuc.getFirst();
    }

    @Transactional
    public KonaklamaTutari tutar(long id) {
        konaklamaKontrol(id, false);
        return resepsiyon.tutar(id);
    }

    @Transactional
    public void checkOut(long id, long kullaniciId, String odemeTuru) {
        konaklamaKontrol(id, true);
        List<Long> personel = resepsiyon.personelBul(kullaniciId);
        if (personel.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Resepsiyon hesabı gerekir.");
        }
        resepsiyon.checkOut(id, personel.getFirst(), odemeTuru);
    }

    private void konaklamaKontrol(long id, boolean acikOlmali) {
        List<Boolean> sonuc = resepsiyon.konaklamayiKilitle(id);
        if (sonuc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Konaklama bulunamadı.");
        }
        if (acikOlmali && !sonuc.getFirst()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Konaklama zaten kapatılmış.");
        }
    }

    private final ResepsiyonRepository resepsiyon;

    public ResepsiyonService(ResepsiyonRepository resepsiyon) {
        this.resepsiyon = resepsiyon;
    }

    public List<GunlukHareket> gunlukListe() {
        return resepsiyon.gunlukListe();
    }

    @Transactional
    public long checkIn(long rezervasyonId, long kullaniciId) {
        if (!resepsiyon.rezervasyonuKilitle(rezervasyonId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı.");
        }
        try {
            List<Long> sonuc = resepsiyon.checkIn(rezervasyonId, kullaniciId);
            if (sonuc.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Check-in için onaylı rezervasyon, uygun tarih, boş oda ve resepsiyon hesabı gerekir.");
            }
            return sonuc.getFirst();
        } catch (DataAccessException e) {
            if (e.getMostSpecificCause() instanceof SQLException sql
                    && ("P0001".equals(sql.getSQLState()) || "23505".equals(sql.getSQLState()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Rezervasyon veya oda check-in için uygun değil.");
            }
            throw e;
        }
    }
}
