package com.otel.controller;

import com.otel.entity.EkHizmet;
import com.otel.service.EkHizmetYonetimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yonetici/ek-hizmetler")
public class EkHizmetYonetimController {
    private final EkHizmetYonetimService kayitlar;

    public EkHizmetYonetimController(EkHizmetYonetimService kayitlar) {
        this.kayitlar = kayitlar;
    }

    @GetMapping
    public List<EkHizmet> listele() {
        return kayitlar.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ekle(@Valid @RequestBody EkHizmet veri) {
        return Map.of("id", kayitlar.ekle(veri));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guncelle(@PathVariable long id, @Valid @RequestBody EkHizmet veri) {
        kayitlar.guncelle(id, veri);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable long id) {
        kayitlar.sil(id);
    }
}
