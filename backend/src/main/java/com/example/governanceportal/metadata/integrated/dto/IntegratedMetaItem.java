package com.example.governanceportal.metadata.integrated.dto;

import java.time.LocalDateTime;
import java.util.List;

public record IntegratedMetaItem(
    String metaId,
    String metaType,
    String metaName,
    String assetKind,
    String sourceSystem,
    String ownerDepartment,
    String securityLevel,
    List<String> searchTypes,
    LocalDateTime updatedAt
) {
}
