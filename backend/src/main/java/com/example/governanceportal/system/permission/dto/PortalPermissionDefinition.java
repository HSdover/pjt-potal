package com.example.governanceportal.system.permission.dto;

public record PortalPermissionDefinition(
    String permissionCode,
    String permissionName,
    String permissionType,
    String targetKey,
    String actionCode,
    String description
) {
}
