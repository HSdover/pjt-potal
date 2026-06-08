package com.example.governanceportal.system.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PortalPermissionAssignmentUpdateRequest(
    @NotBlank String subjectType,
    @NotBlank String subjectId,
    @NotBlank String subjectName,
    @NotNull List<String> permissionCodes
) {
    public PortalPermissionAssignmentUpdateRequest {
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
