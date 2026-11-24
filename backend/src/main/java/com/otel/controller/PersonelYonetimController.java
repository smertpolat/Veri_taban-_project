package com.otel.controller;

import com.otel.entity.Personel;
import com.otel.service.PersonelYonetimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yonetici/personeller")
public class PersonelYonetimController {
    @GetMapping("/hesaplar")
    public List<com.otel.entity.PersonelHesabi> hesaplar() {
        return kayitlar.hesaplar();
    }

    @PutMapping("/{id}/hesap")
    public Map<String, Long> hesapKaydet(@PathVariable long id, @Valid @RequestBody HesapIstegi veri) {
        return Map.of("id", kayitlar.hesapKaydet(id, veri.kullaniciAdi(), veri.parola()));
    }

    @DeleteMapping("/{id}/hesap")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hesapSil(@PathVariable long id) {
        kayitlar.hesapSil(id);
    }

    public record HesapIstegi(
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Pattern(regexp = "[a-zA-Z0-9._-]{3,50}") String kullaniciAdi,
            @jakarta.validation.constraints.NotBlank
            @jakarta.validation.constraints.Size(min = 8, max = 72) String parola
    ) {
    }
    private final PersonelYonetimService kayitlar;

    public PersonelYonetimController(PersonelYonetimService kayitlar) {
        this.kayitlar = kayitlar;
    }

    @GetMapping
    public List<Personel> listele() {
        return kayitlar.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ekle(@Valid @RequestBody Personel veri) {
        return Map.of("id", kayitlar.ekle(veri));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guncelle(@PathVariable long id, @Valid @RequestBody Personel veri) {
        kayitlar.guncelle(id, veri);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable long id) {
        kayitlar.sil(id);
    }
}
