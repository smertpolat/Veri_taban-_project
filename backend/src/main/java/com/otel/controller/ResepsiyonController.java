package com.otel.controller;

import com.otel.entity.GunlukHareket;
import com.otel.service.ResepsiyonService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resepsiyon")
public class ResepsiyonController {
    private final ResepsiyonService resepsiyon;

    public ResepsiyonController(ResepsiyonService resepsiyon) {
        this.resepsiyon = resepsiyon;
    }

    @GetMapping("/gunluk-giris-cikis")
    public List<GunlukHareket> gunlukListe() {
        return resepsiyon.gunlukListe();
    }

    @PostMapping("/rezervasyonlar/{id}/check-in")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> checkIn(@PathVariable long id, @AuthenticationPrincipal Jwt jwt) {
        return Map.of("konaklamaId", resepsiyon.checkIn(id, Long.parseLong(jwt.getSubject())));
    }
}
