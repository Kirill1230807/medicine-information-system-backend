package com.bank.appointmentservice.scheduler.job;

import com.bank.appointmentservice.service.ScheduledMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@DisallowConcurrentExecution
@RequiredArgsConstructor
public class AppointmentCleanupJob extends QuartzJobBean {

    public static final String KEY_RETENTION_DAYS = "retentionDays";
    public static final String KEY_TARGET_STATUS = "targetStatus";

    private final ScheduledMaintenanceService maintenanceService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();

        if (!dataMap.containsKey(KEY_RETENTION_DAYS) || !dataMap.containsKey(KEY_TARGET_STATUS)) {
            throw new JobExecutionException("Missing required JobDataMap parameters: retentionDays or targetStatus", false);
        }

        int retentionDays;
        try {
            retentionDays = Integer.parseInt(dataMap.get(KEY_RETENTION_DAYS).toString());
        } catch (NumberFormatException ex) {
            throw new JobExecutionException("Invalid format for retentionDays", ex, false);
        }

        String targetStatus = dataMap.getString(KEY_TARGET_STATUS);

        try {
            maintenanceService.expireOutdatedAppointments(retentionDays, targetStatus);
        } catch (Exception ex) {
            // Стратегія 1: фіксація помилки та очікування наступного запуску за розкладом (refireImmediately = false)
            log.error("Failed to execute AppointmentCleanupJob. Will wait for next scheduled run.", ex);
            throw new JobExecutionException(ex, false);
        }
    }
}