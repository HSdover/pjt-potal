package com.example.governanceportal.batch.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class SampleMaintenanceJobConfig {

    public static final String JOB_NAME = "sampleMaintenanceJob";

    private static final Logger log = LoggerFactory.getLogger(SampleMaintenanceJobConfig.class);

    @Bean
    Job sampleMaintenanceJob(JobRepository jobRepository, Step sampleMaintenanceStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(sampleMaintenanceStep)
            .build();
    }

    @Bean
    Step sampleMaintenanceStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sampleMaintenanceStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                // 실제 업무 확정 전까지 배치 실행/이력/스케줄 동작을 검증하기 위한 기준 Job이다.
                log.info("Sample maintenance batch executed. jobParameters={}", chunkContext.getStepContext().getJobParameters());
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
