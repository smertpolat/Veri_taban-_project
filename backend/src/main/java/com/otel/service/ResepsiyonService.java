package com.otel.service;

import com.otel.entity.GunlukHareket;
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
