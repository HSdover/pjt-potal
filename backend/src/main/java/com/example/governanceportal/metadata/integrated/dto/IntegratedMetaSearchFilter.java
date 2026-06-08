package com.example.governanceportal.metadata.integrated.dto;

import java.util.List;

public record IntegratedMetaSearchFilter(
    String metaType,
    List<String> searchTypes,
    String keyword
) {
}
