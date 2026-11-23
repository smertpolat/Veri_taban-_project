package com.otel.service;

import com.otel.entity.MusaitOda;
import com.otel.repository.OdaRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OdaService {
    private final OdaRepository odalar;

    public OdaService(OdaRepository odalar) {
        this.odalar = odalar;
    }

    @Transactional(readOnly = true)
    public List<MusaitOda> musaitOdalariBul(LocalDate giris, LocalDate cikis, long odaTipiId) {
        if (!giris.isBefore(cikis) || odaTipiId < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Çıkış tarihi giriş tarihinden sonra ve oda tipi kimliği pozitif olmalıdır.");
        }
        return odalar.musaitOdalariBul(giris, cikis, odaTipiId);
    }
}
