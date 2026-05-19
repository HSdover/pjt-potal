package com.example.governanceportal.reference.approval.dto;

import java.time.LocalDateTime;

public record RefApprovalHistoryItem(
    String fromStatus,
    String toStatus,
    String actorName,
    String comment,
    LocalDateTime occurredAt
) {
}
