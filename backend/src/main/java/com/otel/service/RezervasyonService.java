package com.otel.service;

import com.otel.entity.Rezervasyon;
import com.otel.entity.KonaklamaGecmisi;
import com.otel.repository.RezervasyonRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RezervasyonService {
    private final RezervasyonRepository rezervasyonlar;

    public RezervasyonService(RezervasyonRepository rezervasyonlar) {
        this.rezervasyonlar = rezervasyonlar;
    }

    public List<Rezervasyon> listele(long kullaniciId) {
        return rezervasyonlar.listele(kullaniciId);
    }

    public List<KonaklamaGecmisi> konaklamaGecmisi(long kullaniciId) {
        return rezervasyonlar.konaklamaGecmisi(kullaniciId);
    }

    @Transactional
    public Rezervasyon olustur(long kullaniciId, long odaId, LocalDate giris, LocalDate cikis, int kisi) {
        if (!giris.isBefore(cikis)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Çıkış tarihi girişten sonra olmalıdır.");
        }
        try {
            List<Rezervasyon> sonuc = rezervasyonlar.olustur(kullaniciId, odaId, giris, cikis, kisi);
            if (sonuc.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Oda, kapasite veya kullanıcı uygun değil.");
            }
            return sonuc.getFirst();
        } catch (DataAccessException e) {
            if (e.getMostSpecificCause() instanceof SQLException sql && "23P01".equals(sql.getSQLState())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Oda seçilen tarihlerde rezerve edilmiş.");
            }
            throw e;
        }
    }

    @Transactional
    public void iptal(long id, long kullaniciId) {
        List<Rezervasyon> sonuc = rezervasyonlar.kilitle(id, kullaniciId);
        if (sonuc.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Rezervasyon bulunamadı.");
        }
        if ("iptal".equals(sonuc.getFirst().durum())) {
            return;
        }
        if (rezervasyonlar.iptal(id) == 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Başlamış veya tamamlanmış konaklama iptal edilemez.");
        }
    }
}
