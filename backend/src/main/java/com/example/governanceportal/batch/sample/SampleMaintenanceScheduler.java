package com.example.governanceportal.batch.sample;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.governanceportal.batch.execution.BatchJobExecutionResponse;
import com.example.governanceportal.batch.execution.BatchJobLauncherService;

@Component
@ConditionalOnProperty(prefix = "app.batch.sample-maintenance", name = "scheduled", havingValue = "true")
public class SampleMaintenanceScheduler {

    private static final Logger log = LoggerFactory.getLogger(SampleMaintenanceScheduler.class);

    private final BatchJobLauncherService batchJobLauncherService;

    public SampleMaintenanceScheduler(BatchJobLauncherService batchJobLauncherService) {
        this.batchJobLauncherService = batchJobLauncherService;
    }

    @Scheduled(cron = "${app.batch.sample-maintenance.cron}")
    public void runSampleMaintenanceJob() {
        try {
            BatchJobExecutionResponse response = batchJobLauncherService.run(
                SampleMaintenanceJobConfig.JOB_NAME,
                Map.of("trigger", "scheduler")
            );
            if (!BatchStatus.COMPLETED.name().equals(response.status())) {
                log.warn("Scheduled batch finished with non-completed status. jobName={}, executionId={}, status={}",
                    response.jobName(), response.executionId(), response.status());
            }
        } catch (Exception error) {
            log.error("Scheduled batch execution failed. jobName={}", SampleMaintenanceJobConfig.JOB_NAME, error);
        }
    }
}
