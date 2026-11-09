package com.bank.appointmentservice.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bank.appointmentservice.repository.AppointmentRepository;
import com.bank.appointmentservice.service.ScheduledMaintenanceService;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class QuartzPersistenceIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduledMaintenanceService maintenanceService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Test
    void quartzTablesExistViaFlyway_andTriggersArePersisted() throws Exception {
        Integer tableCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_name LIKE 'qrtz_%'",
                Integer.class);

        assertTrue(tableCount != null && tableCount >= 11, "All QRTZ_* tables must be created by Flyway");
        assertTrue(scheduler.checkExists(TriggerKey.triggerKey("frequentCleanupTrigger", "maintenance-group")));
        assertTrue(scheduler.checkExists(TriggerKey.triggerKey("deepNightlyCleanupTrigger", "maintenance-group")));
        assertTrue(scheduler.checkExists(TriggerKey.triggerKey("dailyReportTrigger", "reporting-group")));
    }

    @Test
    void expireOutdatedAppointments_isIdempotent() {
        // Попередньо переконайтеся, що в БД є тестовий Appointment або створіть його з doctor/patient
        int firstRunUpdated = maintenanceService.expireOutdatedAppointments(1, "EXPIRED");
        int secondRunUpdated = maintenanceService.expireOutdatedAppointments(1, "EXPIRED");

        // Другий запуск одразу після першого не змінює жодного запису
        assertEquals(0, secondRunUpdated);
    }
}