package com.otel.service;

import com.otel.entity.OdaTipi;
import com.otel.repository.OdaTipiYonetimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OdaTipiYonetimService {
    private final OdaTipiYonetimRepository kayitlar;

    public OdaTipiYonetimService(OdaTipiYonetimRepository kayitlar) {
        this.kayitlar = kayitlar;
    }

    public List<OdaTipi> listele() {
        return kayitlar.listele();
    }

    public long ekle(OdaTipi veri) {
        return kayitlar.ekle(veri);
    }

    public void guncelle(long id, OdaTipi veri) {
        if (kayitlar.guncelle(id, veri) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
    }

    public void sil(long id) {
        if (kayitlar.sil(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
    }
}
