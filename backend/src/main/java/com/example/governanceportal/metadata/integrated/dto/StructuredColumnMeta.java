package com.example.governanceportal.metadata.integrated.dto;

public record StructuredColumnMeta(
    int ordinal,
    String columnName,
    String dataType,
    String nullable,
    String keyType,
    String securityLevel,
    String description
) {
}
