package com.example.governanceportal.reference.detail.dto;

import java.time.LocalDateTime;

public record RefDetailItem(
    Long id,
    String name,
    String code,
    String category,
    String ownerName,
    String description,
    String status,
    LocalDateTime createdAt
) {
}
