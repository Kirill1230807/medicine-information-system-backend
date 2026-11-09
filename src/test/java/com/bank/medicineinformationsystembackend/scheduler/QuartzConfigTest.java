package com.bank.medicineinformationsystembackend.scheduler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bank.medicineinformationsystembackend.config.QuartzConfig;
import com.bank.medicineinformationsystembackend.scheduler.job.AppointmentCleanupJob;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.quartz.CronTrigger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

class QuartzConfigTest {

    private final QuartzConfig quartzConfig = new QuartzConfig();

    @Test
    void jobsAndTriggers_haveExplicitKeysAndGroups() {
        JobDetail cleanupJob = quartzConfig.appointmentCleanupJobDetail();
        Trigger frequentTrigger = quartzConfig.frequentCleanupTrigger(cleanupJob);
        Trigger nightlyTrigger = quartzConfig.deepNightlyCleanupTrigger(cleanupJob);

        assertEquals("appointmentCleanupJob", cleanupJob.getKey().getName());
        assertEquals(QuartzConfig.MAINTENANCE_GROUP, cleanupJob.getKey().getGroup());
        assertEquals(cleanupJob.getKey(), frequentTrigger.getJobKey());
        assertEquals(cleanupJob.getKey(), nightlyTrigger.getJobKey());
    }

    @Test
    void triggerJobDataMap_overridesJobDefaults() {
        JobDetail cleanupJob = quartzConfig.appointmentCleanupJobDetail();
        Trigger nightlyTrigger = quartzConfig.deepNightlyCleanupTrigger(cleanupJob);

        JobDataMap merged = new JobDataMap();
        merged.putAll(cleanupJob.getJobDataMap());
        merged.putAll(nightlyTrigger.getJobDataMap());

        // Перевизначений ключ
        assertEquals("30", merged.getString(AppointmentCleanupJob.KEY_RETENTION_DAYS));
        // Ключ за замовчуванням зберігся
        assertEquals("EXPIRED", merged.getString(AppointmentCleanupJob.KEY_TARGET_STATUS));
    }

    @Test
    void triggers_haveExplicitMisfireInstructionsAndTimeZones() {
        JobDetail cleanupJob = quartzConfig.appointmentCleanupJobDetail();
        SimpleTrigger simpleTrigger = (SimpleTrigger) quartzConfig.frequentCleanupTrigger(cleanupJob);
        CronTrigger cronTrigger = (CronTrigger) quartzConfig.deepNightlyCleanupTrigger(cleanupJob);

        assertEquals(SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT,
                simpleTrigger.getMisfireInstruction());
        assertEquals(CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW,
                cronTrigger.getMisfireInstruction());

        assertNotNull(cronTrigger.getTimeZone());
        assertEquals("Europe/Kyiv", cronTrigger.getTimeZone().getID());

        // Перевірка поведінки невідомого часового поясу
        TimeZone unknownTz = TimeZone.getTimeZone("Mars/Phobos");
        assertEquals("GMT", unknownTz.getID());
    }

    @Test
    void cleanupJob_disallowsConcurrentExecution() {
        assertTrue(AppointmentCleanupJob.class.isAnnotationPresent(DisallowConcurrentExecution.class));
    }
}