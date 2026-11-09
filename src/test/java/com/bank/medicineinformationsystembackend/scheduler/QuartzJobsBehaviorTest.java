package com.bank.medicineinformationsystembackend.scheduler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bank.medicineinformationsystembackend.scheduler.job.AppointmentCleanupJob;
import com.bank.medicineinformationsystembackend.scheduler.job.DailyReportJob;
import com.bank.medicineinformationsystembackend.service.ScheduledMaintenanceService;
import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerContext;

class QuartzJobsBehaviorTest {

    private JobExecutionContext createMockContext() throws Exception {
        JobExecutionContext context = mock(JobExecutionContext.class);
        Scheduler scheduler = mock(Scheduler.class);
        when(scheduler.getContext()).thenReturn(new SchedulerContext());
        when(context.getScheduler()).thenReturn(scheduler);
        return context;
    }

    @Test
    void cleanupJob_missingParameter_throwsJobExecutionException() throws Exception {
        ScheduledMaintenanceService service = mock(ScheduledMaintenanceService.class);
        AppointmentCleanupJob job = new AppointmentCleanupJob(service);

        JobExecutionContext context = createMockContext();
        when(context.getMergedJobDataMap()).thenReturn(new JobDataMap());

        assertThrows(JobExecutionException.class, () -> job.execute(context));
    }

    @Test
    void dailyReportJob_serviceFails_retriesUpToMaxRetriesAndStops() throws Exception {
        ScheduledMaintenanceService service = mock(ScheduledMaintenanceService.class);
        doThrow(new RuntimeException("DB connection timeout"))
                .when(service).generateDailySummaryReport("TEST_REPORT");

        DailyReportJob job = new DailyReportJob(service);

        JobDetail jobDetail = JobBuilder.newJob(DailyReportJob.class)
                .usingJobData(DailyReportJob.KEY_REPORT_TYPE, "TEST_REPORT")
                .usingJobData(DailyReportJob.KEY_MAX_RETRIES, "2")
                .build();

        JobExecutionContext context = createMockContext();
        when(context.getJobDetail()).thenReturn(jobDetail);
        when(context.getMergedJobDataMap()).thenReturn(jobDetail.getJobDataMap());

        // Спроба 1 -> refireImmediately = true
        JobExecutionException ex1 = assertThrows(JobExecutionException.class, () -> job.execute(context));
        assertTrue(ex1.refireImmediately());

        // Спроба 2 -> refireImmediately = true
        JobExecutionException ex2 = assertThrows(JobExecutionException.class, () -> job.execute(context));
        assertTrue(ex2.refireImmediately());

        // Спроба 3 (ліміт вичерпано) -> refireImmediately = false
        JobExecutionException ex3 = assertThrows(JobExecutionException.class, () -> job.execute(context));
        assertFalse(ex3.refireImmediately());

        verify(service, times(3)).generateDailySummaryReport("TEST_REPORT");
    }
}