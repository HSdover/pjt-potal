package com.example.governanceportal.metadata.integrated.dto;

import java.util.List;

public record MetaSection(
    String sectionId,
    String title,
    List<MetaKeyValue> fields
) {
}
