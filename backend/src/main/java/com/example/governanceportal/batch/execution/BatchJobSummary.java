package com.example.governanceportal.batch.execution;

import java.time.LocalDateTime;

public record BatchJobSummary(
    String name,
    Long lastExecutionId,
    String lastStatus,
    String lastExitCode,
    LocalDateTime lastStartTime,
    LocalDateTime lastEndTime
) {
}
