package com.example.governanceportal.system.permission.dto;

import java.util.List;

public record PortalPermissionAssignment(
    String subjectType,
    String subjectId,
    List<String> permissionCodes
) {
    public PortalPermissionAssignment {
        permissionCodes = permissionCodes == null ? List.of() : List.copyOf(permissionCodes);
    }
}
