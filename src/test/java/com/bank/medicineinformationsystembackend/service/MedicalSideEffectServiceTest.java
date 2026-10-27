package com.bank.medicineinformationsystembackend.service;

import com.bank.medicineinformationsystembackend.config.AsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {AsyncConfig.class, MedicalSideEffectService.class})
@Import({AsyncConfig.class, MedicalSideEffectService.class})
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MedicalSideEffectServiceTest {

    @Autowired
    private MedicalSideEffectService sideEffectService;

    @Test
    void testConcurrentExecution_RunsSimultaneously() {
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> task1 = sideEffectService.generatePdfReport();
        CompletableFuture<String> task2 = sideEffectService.notifyExternalSystem();

        // Об'єднуємо обидва завдання
        String result = task1.thenCombine(task2, (r1, r2) -> r1 + " " + r2).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Кожен метод триває 1 сек. Якщо вони виконуються послідовно, це займе 2 сек.
        // Оскільки вони паралельні, виконання має тривати трохи більше 1 сек (< 1500 мс).
        assertTrue(duration < 1500, "Завдання виконувалися послідовно, а не паралельно");
        assertEquals("PDF_REPORT_GENERATED EXTERNAL_SYSTEM_NOTIFIED", result);
    }

    @Test
    void testExecutorOverload_ThrowsTaskRejectedException() {
        // Наш пул налаштовано: 2 (core/max) + 1 (queue) = максимум 3 завдання одночасно.
        // Відправка 4 завдань миттєво спричинить TaskRejectedException для останнього.

        assertThrows(TaskRejectedException.class, () -> {
            sideEffectService.generatePdfReport(); // Потік 1
            sideEffectService.generatePdfReport(); // Потік 2
            sideEffectService.generatePdfReport(); // В черзі (1)
            sideEffectService.generatePdfReport(); // Черга заповнена -> викидає exception
        });
    }

    @Test
    void testTimeout_HandledGracefully() {
        CompletableFuture<String> slowTask = sideEffectService.verySlowTask();

        String result = slowTask
                .orTimeout(1, java.util.concurrent.TimeUnit.SECONDS)
                .exceptionally(ex -> "TIMEOUT_FALLBACK")
                .join();

        assertEquals("TIMEOUT_FALLBACK", result);
    }
}