package com.example.governanceportal.batch.execution;

import java.time.LocalDateTime;
import java.util.List;

public record BatchJobExecutionResponse(
    String jobName,
    Long executionId,
    String status,
    String exitCode,
    LocalDateTime createTime,
    LocalDateTime startTime,
    LocalDateTime endTime,
    List<String> failureMessages
) {
}
