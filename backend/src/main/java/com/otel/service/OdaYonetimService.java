package com.otel.service;

import com.otel.entity.Oda;
import com.otel.repository.OdaYonetimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OdaYonetimService {
    private final OdaYonetimRepository kayitlar;

    public OdaYonetimService(OdaYonetimRepository kayitlar) {
        this.kayitlar = kayitlar;
    }

    public List<Oda> listele() {
        return kayitlar.listele();
    }

    public long ekle(Oda veri) {
        return kayitlar.ekle(veri);
    }

    public void guncelle(long id, Oda veri) {
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
