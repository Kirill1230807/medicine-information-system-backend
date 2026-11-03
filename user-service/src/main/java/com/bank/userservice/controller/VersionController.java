package com.bank.userservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VersionController {
    @Value("${APP_VERSION:1.0.0}")
    private String appVersion;

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        return Map.of("version", appVersion);
    }
}
