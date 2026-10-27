package com.bank.medicineinformationsystembackend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MedicalSideEffectService {

    @Async("customTaskExecutor")
    public void sendEmailNotification(String email) {
        simulateDelay();
        if (email.contains("error")) {
            throw new RuntimeException("Помилка під час надсилання листа");
        }
        log.info("Email успішно надіслано на {}", email);
    }

    @Async("customTaskExecutor")
    public CompletableFuture<String> generatePdfReport() {
        simulateDelay();
        return CompletableFuture.completedFuture("PDF_REPORT_GENERATED");
    }

    @Async("customTaskExecutor")
    public CompletableFuture<String> notifyExternalSystem() {
        simulateDelay();
        return CompletableFuture.completedFuture("EXTERNAL_SYSTEM_NOTIFIED");
    }

    @Async("customTaskExecutor")
    public CompletableFuture<String> failingTask() {
        simulateDelay();
        CompletableFuture<String> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Зовнішній сервіс недоступний"));
        return future;
    }

    @Async("customTaskExecutor")
    public CompletableFuture<String> verySlowTask() {
        try { Thread.sleep(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        return CompletableFuture.completedFuture("TOO_LATE");
    }

    private void simulateDelay() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}