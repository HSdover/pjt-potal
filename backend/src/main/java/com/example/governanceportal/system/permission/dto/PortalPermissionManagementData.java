package com.example.governanceportal.system.permission.dto;

import java.util.List;

public record PortalPermissionManagementData(
    List<PortalPermissionDefinition> permissions,
    List<PortalPermissionSubject> subjects,
    List<PortalPermissionAssignment> assignments
) {
}
