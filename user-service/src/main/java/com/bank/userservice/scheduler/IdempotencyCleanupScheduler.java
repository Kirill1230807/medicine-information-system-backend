package com.bank.userservice.scheduler;

import com.bank.userservice.repository.IdempotencyRecordRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
public class IdempotencyCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyCleanupScheduler.class);

    private final IdempotencyRecordRepository repository;
    private final long ttlHours;

    public IdempotencyCleanupScheduler(
            IdempotencyRecordRepository repository,
            @Value("${app.scheduling.idempotency-ttl-hours:24}") long ttlHours
    ) {
        this.repository = repository;
        this.ttlHours = ttlHours;
    }

    @Scheduled(fixedRateString = "${app.scheduling.idempotency-cleanup-rate-ms:30000}")
    @SchedulerLock(
            name = "cleanupExpiredIdempotencyRecords",
            lockAtLeastFor = "PT25S",
            lockAtMostFor = "PT5M"
    )
    @Transactional
    public void cleanupExpiredRecords() {
        Instant cutoff = Instant.now().minus(ttlHours, ChronoUnit.HOURS);
        int deleted = repository.deleteExpiredRecords(cutoff);
        log.info("[ShedLock] Завдання cleanupExpiredIdempotencyRecords виконано на цьому інстансі. Видалено записів: {}", deleted);
    }
}