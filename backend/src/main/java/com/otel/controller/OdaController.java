package com.otel.controller;

import com.otel.entity.MusaitOda;
import com.otel.service.OdaService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OdaController {
    private final OdaService odalar;

    public OdaController(OdaService odalar) {
        this.odalar = odalar;
    }

    @GetMapping("/api/odalar/musait")
    public List<MusaitOda> musaitOdalariBul(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giris,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cikis,
            @RequestParam long odaTipiId) {
        return odalar.musaitOdalariBul(giris, cikis, odaTipiId);
    }
}
