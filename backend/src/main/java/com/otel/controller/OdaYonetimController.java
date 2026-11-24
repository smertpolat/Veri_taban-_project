package com.otel.controller;

import com.otel.entity.Oda;
import com.otel.service.OdaYonetimService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/yonetici/odalar")
public class OdaYonetimController {
    private final OdaYonetimService kayitlar;

    public OdaYonetimController(OdaYonetimService kayitlar) {
        this.kayitlar = kayitlar;
    }

    @GetMapping
    public List<Oda> listele() {
        return kayitlar.listele();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> ekle(@Valid @RequestBody Oda veri) {
        return Map.of("id", kayitlar.ekle(veri));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void guncelle(@PathVariable long id, @Valid @RequestBody Oda veri) {
        kayitlar.guncelle(id, veri);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sil(@PathVariable long id) {
        kayitlar.sil(id);
    }
}
