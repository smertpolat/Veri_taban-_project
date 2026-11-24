package com.otel.config;

import com.otel.entity.Personel;
import com.otel.service.PersonelYonetimService;
import java.time.LocalDate;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("ilk-yonetici")
public class IlkYoneticiKurulumu implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    private final PersonelYonetimService personel;
    private final Environment ortam;

    public IlkYoneticiKurulumu(JdbcTemplate jdbc, PersonelYonetimService personel, Environment ortam) {
        this.jdbc = jdbc;
        this.personel = personel;
        this.ortam = ortam;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        jdbc.execute("LOCK TABLE public.kullanici IN EXCLUSIVE MODE");
        if (Boolean.TRUE.equals(jdbc.queryForObject(
                "SELECT EXISTS(SELECT 1 FROM public.kullanici WHERE rol = 'yonetici')", Boolean.class))) {
            return;
        }
        String ad = ortam.getRequiredProperty("OTEL_ADMIN_USERNAME");
        String parola = ortam.getRequiredProperty("OTEL_ADMIN_PASSWORD");
        if (!ad.matches("[a-zA-Z0-9._-]{3,50}") || parola.isBlank() || parola.length() < 8) {
            throw new IllegalArgumentException("Yönetici kullanıcı adı veya parola biçimi geçersiz.");
        }
        long id = personel.ekle(new Personel(null, "İlk", "Yönetici", "yonetici", LocalDate.now()));
        personel.hesapKaydet(id, ad, parola);
    }
}
