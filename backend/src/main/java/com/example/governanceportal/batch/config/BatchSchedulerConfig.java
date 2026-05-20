package com.example.governanceportal.batch.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "app.batch.scheduler", name = "enabled", havingValue = "true")
public class BatchSchedulerConfig {
}
