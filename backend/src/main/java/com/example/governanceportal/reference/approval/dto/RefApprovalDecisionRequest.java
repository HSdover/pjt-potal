package com.example.governanceportal.reference.approval.dto;

import jakarta.validation.constraints.Size;

public record RefApprovalDecisionRequest(
    @Size(max = 500) String comment
) {
}
