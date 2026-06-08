package com.example.governanceportal.samplejpa.service;

import java.nio.file.Path;
import java.time.OffsetDateTime;

class SampleJpaExcelLargeExportJob {

    private final String jobId;
    private final long totalRows;
    private final OffsetDateTime requestedAt;
    private volatile SampleJpaExcelLargeExportStatus status;
    private volatile String message;
    private volatile Path filePath;

    SampleJpaExcelLargeExportJob(String jobId, long totalRows) {
        this.jobId = jobId;
        this.totalRows = totalRows;
        this.requestedAt = OffsetDateTime.now();
        this.status = SampleJpaExcelLargeExportStatus.REQUESTED;
        this.message = "Large Excel export was requested.";
    }

    String getJobId() {
        return jobId;
    }

    long getTotalRows() {
        return totalRows;
    }

    OffsetDateTime getRequestedAt() {
        return requestedAt;
    }

    SampleJpaExcelLargeExportStatus getStatus() {
        return status;
    }

    String getMessage() {
        return message;
    }

    Path getFilePath() {
        return filePath;
    }

    void running() {
        this.status = SampleJpaExcelLargeExportStatus.RUNNING;
        this.message = "Large Excel export is running.";
    }

    void completed(Path filePath) {
        this.status = SampleJpaExcelLargeExportStatus.COMPLETED;
        this.filePath = filePath;
        this.message = "Large Excel export is completed.";
    }

    void failed(String message) {
        this.status = SampleJpaExcelLargeExportStatus.FAILED;
        this.message = message;
    }
}
