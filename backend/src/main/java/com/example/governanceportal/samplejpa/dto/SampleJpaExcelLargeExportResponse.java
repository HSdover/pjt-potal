package com.example.governanceportal.samplejpa.dto;

public record SampleJpaExcelLargeExportResponse(
    String jobId,
    String status,
    long totalRows,
    String message,
    String downloadUrl
) {
}
