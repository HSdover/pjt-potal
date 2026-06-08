package com.example.governanceportal.system.permission.api;

import com.example.governanceportal.system.permission.dto.PortalPermissionAssignment;
import com.example.governanceportal.system.permission.dto.PortalPermissionAssignmentUpdateRequest;
import com.example.governanceportal.system.permission.dto.PortalPermissionManagementData;
import com.example.governanceportal.system.permission.service.PortalPermissionManagementService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system/permissions")
@PreAuthorize("@portalPermissionService.hasPermission(authentication, 'PERMISSION_MANAGE')")
public class PortalPermissionManagementController {

    private final PortalPermissionManagementService portalPermissionManagementService;

    public PortalPermissionManagementController(PortalPermissionManagementService portalPermissionManagementService) {
        this.portalPermissionManagementService = portalPermissionManagementService;
    }

    @GetMapping
    public PortalPermissionManagementData findManagementData() {
        return portalPermissionManagementService.findManagementData();
    }

    @PutMapping("/assignments")
    public PortalPermissionAssignment updateAssignment(@Valid @RequestBody PortalPermissionAssignmentUpdateRequest request) {
        return portalPermissionManagementService.updateAssignment(request);
    }
}
