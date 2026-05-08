package com.example.governanceportal.catalog;

public record MetadataCatalogItem(
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
