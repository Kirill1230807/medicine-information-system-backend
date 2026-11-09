package com.bank.medicineinformationsystembackend.config;

import com.bank.medicineinformationsystembackend.scheduler.job.AppointmentCleanupJob;
import com.bank.medicineinformationsystembackend.scheduler.job.DailyReportJob;
import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    public static final String MAINTENANCE_GROUP = "maintenance-group";
    public static final String REPORTING_GROUP = "reporting-group";

    @Bean
    public JobDetail appointmentCleanupJobDetail() {
        return JobBuilder.newJob(AppointmentCleanupJob.class)
                .withIdentity(JobKey.jobKey("appointmentCleanupJob", MAINTENANCE_GROUP))
                .usingJobData(AppointmentCleanupJob.KEY_RETENTION_DAYS, "1")
                .usingJobData(AppointmentCleanupJob.KEY_TARGET_STATUS, "EXPIRED")
                .storeDurably()
                .build();
    }

    /**
     * Тригер 1 для AppointmentCleanupJob: частий запуск (використовує дефолтні параметри JobDetail).
     */
    @Bean
    public Trigger frequentCleanupTrigger(JobDetail appointmentCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(appointmentCleanupJobDetail)
                .withIdentity(TriggerKey.triggerKey("frequentCleanupTrigger", MAINTENANCE_GROUP))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(30)
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                .build();
    }

    /**
     * Тригер 2 для AppointmentCleanupJob: глибоке нічне очищення (перевизначає retentionDays на 30).
     */
    @Bean
    public Trigger deepNightlyCleanupTrigger(JobDetail appointmentCleanupJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(appointmentCleanupJobDetail)
                .withIdentity(TriggerKey.triggerKey("deepNightlyCleanupTrigger", MAINTENANCE_GROUP))
                .usingJobData(AppointmentCleanupJob.KEY_RETENTION_DAYS, "30")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("Europe/Kyiv"))
                        .withMisfireHandlingInstructionFireAndProceed())
                .build();
    }

    @Bean
    public JobDetail dailyReportJobDetail() {
        return JobBuilder.newJob(DailyReportJob.class)
                .withIdentity(JobKey.jobKey("dailyReportJob", REPORTING_GROUP))
                .usingJobData(DailyReportJob.KEY_REPORT_TYPE, "GENERAL_CLINIC_REPORT")
                .usingJobData(DailyReportJob.KEY_MAX_RETRIES, "2")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dailyReportTrigger(JobDetail dailyReportJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyReportJobDetail)
                .withIdentity(TriggerKey.triggerKey("dailyReportTrigger", REPORTING_GROUP))
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?")
                        .inTimeZone(TimeZone.getTimeZone("UTC"))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}