package com.bank.medicineinformationsystembackend.scheduler.job;

import com.bank.medicineinformationsystembackend.service.ScheduledMaintenanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReportJob extends QuartzJobBean {

    public static final String KEY_REPORT_TYPE = "reportType";
    public static final String KEY_MAX_RETRIES = "maxRetries";
    public static final String KEY_RETRY_COUNT = "retryCount";

    private final ScheduledMaintenanceService maintenanceService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap mergedDataMap = context.getMergedJobDataMap();
        String reportType = mergedDataMap.containsKey(KEY_REPORT_TYPE)
                ? mergedDataMap.getString(KEY_REPORT_TYPE)
                : "STANDARD_SUMMARY"; // Задокументоване значення за замовчуванням

        int maxRetries = mergedDataMap.containsKey(KEY_MAX_RETRIES)
                ? Integer.parseInt(mergedDataMap.get(KEY_MAX_RETRIES).toString())
                : 3;

        // Стан поточної спроби беремо з JobDetail.getJobDataMap() (зберігається між refire)
        JobDataMap jobStateMap = context.getJobDetail().getJobDataMap();
        int currentRetry = jobStateMap.containsKey(KEY_RETRY_COUNT)
                ? Integer.parseInt(jobStateMap.get(KEY_RETRY_COUNT).toString())
                : 0;

        try {
            maintenanceService.generateDailySummaryReport(reportType);
            jobStateMap.put(KEY_RETRY_COUNT, "0"); // Скидаємо лічильник при успіху
        } catch (Exception ex) {
            if (currentRetry < maxRetries) {
                int nextRetry = currentRetry + 1;
                jobStateMap.put(KEY_RETRY_COUNT, String.valueOf(nextRetry));
                log.warn("DailyReportJob failed (attempt {}/{}). Retrying immediately...", nextRetry, maxRetries);
                // Стратегія 2: негайний перезапуск із захистом від нескінченного циклу
                throw new JobExecutionException(ex, true);
            } else {
                jobStateMap.put(KEY_RETRY_COUNT, "0");
                log.error("DailyReportJob failed after {} max retries. Aborting until next schedule.", maxRetries, ex);
                throw new JobExecutionException(ex, false);
            }
        }
    }
}