package com.otel.service;

import com.otel.entity.AylikRapor;
import com.otel.entity.OdaDurumu;
import com.otel.repository.RaporRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RaporService {
    private final RaporRepository raporlar;

    public RaporService(RaporRepository raporlar) {
        this.raporlar = raporlar;
    }

    // Prosedürdeki doluluk ve gelir sorguları aynı veri anını görür.
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public AylikRapor aylik(LocalDate tarih) {
        return raporlar.aylik(tarih);
    }

    public List<OdaDurumu> odaDurumlari() {
        return raporlar.odaDurumlari();
    }
}
