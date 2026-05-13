package com.example.governanceportal.sourcesample.dto;

import java.time.LocalDate;

public record SourceSampleItem(
    Long sampleId,
    String institutionName,
    String institutionType,
    String sidoName,
    String sigunguName,
    String specialtyName,
    LocalDate openedDate
) {
}
