package com.otel.controller;

import com.otel.entity.OdaTipi;
import com.otel.service.OdaTipiYonetimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yonetici/oda-tipleri")
public class OdaTipiYonetimController {
    private final OdaTipiYonetimService kayitlar;

    public OdaTipiYonetimController(OdaTipiYonetimService kayitlar) {
        this.kayitlar = kayitlar;
    }

    @GetMapping
    public List<OdaTipi> listele() {
        return kayitlar.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ekle(@Valid @RequestBody OdaTipi veri) {
        return Map.of("id", kayitlar.ekle(veri));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guncelle(@PathVariable long id, @Valid @RequestBody OdaTipi veri) {
        kayitlar.guncelle(id, veri);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable long id) {
        kayitlar.sil(id);
    }
}
