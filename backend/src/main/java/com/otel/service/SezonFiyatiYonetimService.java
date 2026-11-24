package com.otel.service;

import com.otel.entity.SezonFiyati;
import com.otel.repository.SezonFiyatiYonetimRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SezonFiyatiYonetimService {
    private final SezonFiyatiYonetimRepository kayitlar;

    public SezonFiyatiYonetimService(SezonFiyatiYonetimRepository kayitlar) {
        this.kayitlar = kayitlar;
    }

    public List<SezonFiyati> listele() {
        return kayitlar.listele();
    }

    public long ekle(SezonFiyati veri) {
        tarihKontrol(veri);
        return kayitlar.ekle(veri);
    }

    public void guncelle(long id, SezonFiyati veri) {
        tarihKontrol(veri);
        if (kayitlar.guncelle(id, veri) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
    }

    private void tarihKontrol(SezonFiyati veri) {
        if (!veri.baslangicTarihi().isBefore(veri.bitisTarihi())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Bitiş tarihi başlangıç tarihinden sonra olmalıdır.");
        }
    }

    public void sil(long id) {
        if (kayitlar.sil(id) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Kayıt bulunamadı.");
        }
    }
}
