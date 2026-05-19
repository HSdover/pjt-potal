package com.example.governanceportal.reference.approval.dto;

import java.time.LocalDateTime;
import java.util.List;

public record RefApprovalItem(
    Long id,
    String title,
    String requesterName,
    String requesterTeam,
    String requestType,
    String summary,
    String status,
    LocalDateTime submittedAt,
    LocalDateTime decidedAt,
    List<RefApprovalHistoryItem> history
) {
}
