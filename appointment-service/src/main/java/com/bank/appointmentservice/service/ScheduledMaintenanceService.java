package com.bank.appointmentservice.service;

import java.time.ZonedDateTime;

import com.bank.appointmentservice.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduledMaintenanceService {

    private final AppointmentRepository appointmentRepository;

    // Ідемпотентно переводить прострочені записи в статус EXPIRED.
    @Transactional
    public int expireOutdatedAppointments(int retentionDays, String targetStatus) {
        ZonedDateTime threshold = ZonedDateTime.now().minusDays(retentionDays);
        int updatedCount = appointmentRepository.markOutdatedAppointments(threshold, targetStatus);
        log.info("Updated {} outdated appointments older than {} days to status {}",
                updatedCount, retentionDays, targetStatus);
        return updatedCount;
    }

    //Формування звіту (імітація роботи із зовнішнім сервісом або БД).
    @Transactional
    public void generateDailySummaryReport(String reportType) {
        log.info("Generating daily report of type: {}", reportType);
    }
}