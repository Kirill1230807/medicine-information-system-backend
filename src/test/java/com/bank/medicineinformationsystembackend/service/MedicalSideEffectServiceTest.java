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

        String result = task1.thenCombine(task2, (r1, r2) -> r1 + " " + r2).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        assertTrue(duration < 1500, "Завдання виконувалися послідовно, а не паралельно");
        assertEquals("PDF_REPORT_GENERATED EXTERNAL_SYSTEM_NOTIFIED", result);
    }

    @Test
    void testExecutorOverload_ThrowsTaskRejectedException() {

        assertThrows(TaskRejectedException.class, () -> {
            sideEffectService.generatePdfReport();
            sideEffectService.generatePdfReport();
            sideEffectService.generatePdfReport();
            sideEffectService.generatePdfReport();
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