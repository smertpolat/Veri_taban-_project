package com.otel.controller;

import com.otel.entity.GunlukHareket;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import com.otel.entity.KonaklamaTutari;
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
    @PostMapping("/konaklamalar/{id}/hizmetler")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Long> hizmetEkle(@PathVariable long id,
            @Valid @RequestBody HizmetIstegi istek) {
        return Map.of("konaklamaHizmetiId", resepsiyon.hizmetEkle(id, istek.hizmetId(), istek.adet()));
    }

    @GetMapping("/konaklamalar/{id}/tutar")
    public KonaklamaTutari tutar(@PathVariable long id) {
        return resepsiyon.tutar(id);
    }

    @PostMapping("/konaklamalar/{id}/check-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void checkOut(@PathVariable long id, @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody OdemeIstegi istek) {
        resepsiyon.checkOut(id, Long.parseLong(jwt.getSubject()), istek.odemeTuru());
    }

    public record HizmetIstegi(@Positive long hizmetId,
                               @Positive int adet) {
    }

    public record OdemeIstegi(
            @NotNull
            @Pattern(regexp = "nakit|kart") String odemeTuru) {
    }

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
