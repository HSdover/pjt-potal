package com.example.governanceportal.batch.execution;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
public class BatchJobLauncherService {

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Map<String, Job> jobsByName;

    public BatchJobLauncherService(JobLauncher jobLauncher, JobExplorer jobExplorer, List<Job> jobs) {
        this.jobLauncher = jobLauncher;
        this.jobExplorer = jobExplorer;
        this.jobsByName = jobs.stream()
            .collect(Collectors.toUnmodifiableMap(Job::getName, Function.identity()));
    }

    public List<BatchJobSummary> listJobs() {
        return jobsByName.keySet().stream()
            .sorted()
            .map(this::toSummary)
            .toList();
    }

    public BatchJobExecutionResponse run(String jobName, Map<String, String> parameters) throws Exception {
        Job job = findJob(jobName);
        JobParameters jobParameters = buildParameters(parameters);
        JobExecution execution = jobLauncher.run(job, jobParameters);

        return toResponse(execution);
    }

    public BatchJobExecutionResponse getExecution(long executionId) {
        JobExecution execution = jobExplorer.getJobExecution(executionId);
        if (execution == null) {
            throw new IllegalArgumentException("Batch execution not found: " + executionId);
        }

        return toResponse(execution);
    }

    private Job findJob(String jobName) {
        Job job = jobsByName.get(jobName);
        if (job == null) {
            throw new IllegalArgumentException("Batch job not found: " + jobName);
        }
        return job;
    }

    private JobParameters buildParameters(Map<String, String> parameters) {
        Map<String, String> safeParameters = parameters == null ? Map.of() : parameters;
        JobParametersBuilder builder = new JobParametersBuilder();
        safeParameters.entrySet().stream()
            .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank())
            .filter(entry -> entry.getValue() != null)
            .forEach(entry -> builder.addString(entry.getKey(), entry.getValue()));

        // 같은 업무 파라미터로도 운영자가 재실행할 수 있도록 실행 시각을 JobInstance 식별자에 포함한다.
        return builder
            .addLong("requestedAt", System.currentTimeMillis())
            .toJobParameters();
    }

    private BatchJobSummary toSummary(String jobName) {
        JobExecution lastExecution = findLastExecution(jobName);
        if (lastExecution == null) {
            return new BatchJobSummary(jobName, null, null, null, null, null);
        }

        return new BatchJobSummary(
            jobName,
            lastExecution.getId(),
            lastExecution.getStatus().name(),
            lastExecution.getExitStatus().getExitCode(),
            lastExecution.getStartTime(),
            lastExecution.getEndTime()
        );
    }

    private JobExecution findLastExecution(String jobName) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName(jobName, 0, 1);
        if (instances.isEmpty()) {
            return null;
        }

        return jobExplorer.getJobExecutions(instances.get(0)).stream()
            .filter(Objects::nonNull)
            .max(Comparator.comparing(JobExecution::getCreateTime))
            .orElse(null);
    }

    private BatchJobExecutionResponse toResponse(JobExecution execution) {
        return new BatchJobExecutionResponse(
            execution.getJobInstance().getJobName(),
            execution.getId(),
            execution.getStatus().name(),
            execution.getExitStatus().getExitCode(),
            execution.getCreateTime(),
            execution.getStartTime(),
            execution.getEndTime(),
            execution.getFailureExceptions().stream()
                .map(Throwable::getMessage)
                .toList()
        );
    }
}
