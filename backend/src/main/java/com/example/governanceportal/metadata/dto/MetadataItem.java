package com.example.governanceportal.metadata.dto;

public record MetadataItem(
    Long metadataId,
    String datasetName,
    String datasetType,
    String sourceName,
    String storageLocation,
    Long rowCount,
    String columnsSummary,
    String description
) {
}
