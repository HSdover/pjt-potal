package com.example.governanceportal.system.permission.dto;

public record PortalPermissionSubject(
    String subjectType,
    String subjectId,
    String subjectName,
    String description
) {
}
