package com.bank.medicineinformationsystembackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Відкритий ендпоінт (permitAll у SecurityFilterChain) — для демонстрації.
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("status", "UP");
    }
}