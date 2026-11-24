package com.otel.controller;

import com.otel.entity.AylikRapor;
import com.otel.entity.OdaDurumu;
import com.otel.service.RaporService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/yonetici/raporlar")
public class RaporController {
    private final RaporService raporlar;

    public RaporController(RaporService raporlar) {
        this.raporlar = raporlar;
    }

    @GetMapping("/aylik")
    public AylikRapor aylik(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ay) {
        return raporlar.aylik(ay);
    }

    @GetMapping("/oda-durumlari")
    public List<OdaDurumu> odaDurumlari() {
        return raporlar.odaDurumlari();
    }
}
