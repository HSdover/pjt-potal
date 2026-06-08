import { http } from "@/shared/api/http";
import type {
  PortalPermissionAssignment,
  PortalPermissionAssignmentUpdateRequest,
  PortalPermissionManagementData,
} from "./types";

export function fetchPermissionManagementData(): Promise<PortalPermissionManagementData> {
  return http.get<PortalPermissionManagementData>("/api/system/permissions");
}

export function updatePermissionAssignment(
  request: PortalPermissionAssignmentUpdateRequest,
): Promise<PortalPermissionAssignment> {
  return http.put<PortalPermissionAssignment>("/api/system/permissions/assignments", request);
}
