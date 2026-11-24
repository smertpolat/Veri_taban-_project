package com.otel.config;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class HesapTokenKontrol implements OAuth2TokenValidator<Jwt> {
    private final JdbcTemplate jdbc;

    public HesapTokenKontrol(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public static String parolaSurumu(String hash) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(hash.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        var sonuclar = jdbc.query("""
                SELECT parola_hash FROM public.kullanici
                WHERE id::text = ? AND kullanici_adi = ? AND rol = ?
                """, (rs, row) -> parolaSurumu(rs.getString("parola_hash")),
                token.getSubject(), token.getClaimAsString("kullaniciAdi"), token.getClaimAsString("rol"));
        if (sonuclar.size() == 1 && sonuclar.getFirst().equals(token.getClaimAsString("parolaSurumu"))) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Tekrar giriş yapın.", null));
    }
}
