package com.otel.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {
        OdaYonetimController.class, OdaTipiYonetimController.class,
        SezonFiyatiYonetimController.class, EkHizmetYonetimController.class, PersonelYonetimController.class
})
public class YonetimHataHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail veriButunlugu(DataIntegrityViolationException hata) {
        if (hata.getMostSpecificCause() instanceof java.sql.SQLException sql
                && "23P01".equals(sql.getSQLState())) {
            return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                    "Bu oda tipi için sezon tarihleri başka bir sezonla çakışıyor.");
        }
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Kayıt zaten mevcut, ilişkili kayıt bulunamadı veya bu kayıt başka kayıtlar tarafından kullanılıyor.");
    }
}
