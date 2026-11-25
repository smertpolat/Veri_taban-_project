package com.otel.controller;

import com.otel.entity.Rezervasyon;
import com.otel.entity.KonaklamaGecmisi;
import com.otel.service.RezervasyonService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rezervasyonlar")
public class RezervasyonController {
    private final RezervasyonService rezervasyonlar;

    public RezervasyonController(RezervasyonService rezervasyonlar) {
        this.rezervasyonlar = rezervasyonlar;
    }

    @GetMapping
    public List<Rezervasyon> listele(@AuthenticationPrincipal Jwt jwt) {
        return rezervasyonlar.listele(Long.parseLong(jwt.getSubject()));
    }

    @GetMapping("/konaklama-gecmisi")
    public List<KonaklamaGecmisi> konaklamaGecmisi(@AuthenticationPrincipal Jwt jwt) {
        return rezervasyonlar.konaklamaGecmisi(Long.parseLong(jwt.getSubject()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rezervasyon olustur(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody YeniRezervasyon istek) {
        return rezervasyonlar.olustur(Long.parseLong(jwt.getSubject()), istek.odaId(),
                istek.girisTarihi(), istek.cikisTarihi(), istek.kisiSayisi());
    }

    @PutMapping("/{id}/iptal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void iptal(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        rezervasyonlar.iptal(id, Long.parseLong(jwt.getSubject()));
    }

    public record YeniRezervasyon(@Positive long odaId, @NotNull @FutureOrPresent LocalDate girisTarihi,
                                   @NotNull LocalDate cikisTarihi, @Positive int kisiSayisi) {
    }
}
