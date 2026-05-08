package com.example.governanceportal.catalog;

public record LineageFlowItem(
    Long flowId,
    String sourceName,
    String sourceType,
    String targetName,
    String targetType,
    String processName,
    String transformType,
    String description,
    Integer sortOrder
) {
}
