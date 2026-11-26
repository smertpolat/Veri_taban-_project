package com.otel.controller;

import com.otel.entity.MusaitOda;
import com.otel.entity.OdaTipi;
import com.otel.service.OdaService;
import com.otel.service.OdaTipiYonetimService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OdaController {
    private final OdaService odalar;
    private final OdaTipiYonetimService odaTipleri;

    public OdaController(OdaService odalar, OdaTipiYonetimService odaTipleri) {
        this.odalar = odalar;
        this.odaTipleri = odaTipleri;
    }

    @GetMapping("/api/oda-tipleri")
    public List<OdaTipi> odaTipleriniListele() {
        return odaTipleri.listele();
    }

    @GetMapping("/api/odalar/musait")
    public List<MusaitOda> musaitOdalariBul(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate giris,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate cikis,
            @RequestParam long odaTipiId) {
        return odalar.musaitOdalariBul(giris, cikis, odaTipiId);
    }
}
