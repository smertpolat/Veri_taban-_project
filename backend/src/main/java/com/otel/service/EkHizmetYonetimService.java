package com.otel.service;

import com.otel.entity.EkHizmet;
import com.otel.repository.EkHizmetYonetimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EkHizmetYonetimService {
    private final EkHizmetYonetimRepository kayitlar;

    public EkHizmetYonetimService(EkHizmetYonetimRepository kayitlar) {
        this.kayitlar = kayitlar;
    }

    public List<EkHizmet> listele() {
        return kayitlar.listele();
    }

    public long ekle(EkHizmet veri) {
        return kayitlar.ekle(veri);
    }

    public void guncelle(long id, EkHizmet veri) {
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
