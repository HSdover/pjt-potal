package com.example.governanceportal.metadata.integrated.dto;

import java.util.List;

public record IntegratedMetaDetail(
    String metaId,
    String metaType,
    String metaName,
    List<MetaSection> sections,
    List<StructuredColumnMeta> columns
) {
}
