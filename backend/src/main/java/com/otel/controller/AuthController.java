package com.otel.controller;

import com.otel.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    private static final String KULLANICI_ADI = "[a-zA-Z0-9._-]{3,50}";
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/api/auth/kayit")
    @ResponseStatus(HttpStatus.CREATED)
    public void kayit(@Valid @RequestBody Kayit istegi) {
        auth.kayit(istegi.ad(), istegi.soyad(), istegi.kimlikNo(), istegi.telefon(),
                istegi.eposta(), istegi.dogumTarihi(), istegi.kullaniciAdi(), istegi.parola());
    }

    @PostMapping("/api/auth/giris")
    public Map<String, String> giris(@Valid @RequestBody Giris istegi) {
        return Map.of("token", auth.giris(istegi.kullaniciAdi(), istegi.parola()), "tokenTuru", "Bearer");
    }

    @GetMapping("/api/auth/ben")
    public Map<String, String> ben(@AuthenticationPrincipal Jwt jwt) {
        return Map.of("kullaniciAdi", jwt.getClaimAsString("kullaniciAdi"), "rol", jwt.getClaimAsString("rol"));
    }

    public record Kayit(
            @NotBlank @Size(max = 100) String ad,
            @NotBlank @Size(max = 100) String soyad,
            @NotBlank @Size(max = 40) String kimlikNo,
            @NotBlank @Size(max = 40) String telefon,
            @NotBlank @Email @Size(max = 254) String eposta,
            @NotNull @Past LocalDate dogumTarihi,
            @NotBlank @Pattern(regexp = KULLANICI_ADI) String kullaniciAdi,
            @NotBlank @Size(min = 8, max = 72) String parola) {
    }

    public record Giris(
            @NotBlank @Pattern(regexp = KULLANICI_ADI) String kullaniciAdi,
            @NotBlank @Size(max = 72) String parola) {
    }
}
