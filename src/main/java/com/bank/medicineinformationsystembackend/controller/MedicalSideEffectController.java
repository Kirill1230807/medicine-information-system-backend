package com.bank.medicineinformationsystembackend.controller;

import com.bank.medicineinformationsystembackend.service.MedicalSideEffectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/side-effects")
@RequiredArgsConstructor
public class MedicalSideEffectController {

    private final MedicalSideEffectService sideEffectService;

    @PostMapping("/email")
    public ResponseEntity<String> sendEmail(@RequestParam String email) {
        sideEffectService.sendEmailNotification(email);
        return ResponseEntity.ok("Запит прийнято в обробку. Перевірте консоль додатка через 1 секунду.");
    }

    @GetMapping("/report")
    public CompletableFuture<ResponseEntity<String>> generateReport() {
        return sideEffectService.generatePdfReport()
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/overload")
    public ResponseEntity<String> triggerOverload() {
        sideEffectService.generatePdfReport();
        sideEffectService.generatePdfReport();
        sideEffectService.generatePdfReport();
        sideEffectService.generatePdfReport();
        return ResponseEntity.ok("Всі завдання відправлені");
    }
}