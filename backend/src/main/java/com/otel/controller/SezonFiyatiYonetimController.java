package com.otel.controller;

import com.otel.entity.SezonFiyati;
import com.otel.service.SezonFiyatiYonetimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yonetici/sezon-fiyatlari")
public class SezonFiyatiYonetimController {
    private final SezonFiyatiYonetimService kayitlar;

    public SezonFiyatiYonetimController(SezonFiyatiYonetimService kayitlar) {
        this.kayitlar = kayitlar;
    }

    @GetMapping
    public List<SezonFiyati> listele() {
        return kayitlar.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ekle(@Valid @RequestBody SezonFiyati veri) {
        return Map.of("id", kayitlar.ekle(veri));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guncelle(@PathVariable long id, @Valid @RequestBody SezonFiyati veri) {
        kayitlar.guncelle(id, veri);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable long id) {
        kayitlar.sil(id);
    }
}
