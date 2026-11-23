package com.otel.service;

import com.otel.entity.Kullanici;
import com.otel.repository.KullaniciRepository;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final KullaniciRepository kullanicilar;
    private final PasswordEncoder parolalar;
    private final JwtEncoder jwt;
    private final String bosHash;

    public AuthService(KullaniciRepository kullanicilar, PasswordEncoder parolalar, JwtEncoder jwt) {
        this.kullanicilar = kullanicilar;
        this.parolalar = parolalar;
        this.jwt = jwt;
        this.bosHash = parolalar.encode(java.util.UUID.randomUUID().toString());
    }

    @Transactional
    public void kayit(String ad, String soyad, String kimlikNo, String telefon, String eposta,
                      LocalDate dogumTarihi, String kullaniciAdi, String parola) {
        if (parola.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parola en fazla 72 UTF-8 baytı olabilir.");
        }
        try {
            kullanicilar.misafirOlustur(ad.strip(), soyad.strip(), kimlikNo.strip(), telefon.strip(),
                    eposta.strip(), dogumTarihi, kullaniciAdi, parolalar.encode(parola));
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu bilgilerle bir kayıt zaten var.");
        }
    }

    public String giris(String kullaniciAdi, String parola) {
        Kullanici kullanici = kullanicilar.kullaniciBul(kullaniciAdi).orElse(null);
        String hash = kullanici == null ? bosHash : kullanici.parolaHash();
        if (parola.getBytes(StandardCharsets.UTF_8).length > 72 || !parolalar.matches(parola, hash)
                || kullanici == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Kullanıcı adı veya parola hatalı.");
        }

        Instant simdi = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(Long.toString(kullanici.id()))
                .issuedAt(simdi)
                .expiresAt(simdi.plusSeconds(3600))
                .claim("kullaniciAdi", kullanici.kullaniciAdi())
                .claim("rol", kullanici.rol())
                .build();
        return jwt.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
    }
}
