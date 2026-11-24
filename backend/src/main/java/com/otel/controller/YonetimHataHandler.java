package com.otel.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {OdaYonetimController.class, OdaTipiYonetimController.class})
public class YonetimHataHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail veriButunlugu(DataIntegrityViolationException hata) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Kayıt zaten mevcut, ilişkili kayıt bulunamadı veya bu kayıt başka kayıtlar tarafından kullanılıyor.");
    }
}
