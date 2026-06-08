export type PermissionSubjectType = "IAM_ROLE" | "GROUP" | "USER";

export type PortalPermissionDefinition = {
  permissionCode: string;
  permissionName: string;
  permissionType: string;
  targetKey: string;
  actionCode: string;
  description?: string;
};

export type PortalPermissionSubject = {
  subjectType: PermissionSubjectType;
  subjectId: string;
  subjectName: string;
  description?: string;
};

export type PortalPermissionAssignment = {
  subjectType: PermissionSubjectType;
  subjectId: string;
  permissionCodes: string[];
};

export type PortalPermissionManagementData = {
  permissions: PortalPermissionDefinition[];
  subjects: PortalPermissionSubject[];
  assignments: PortalPermissionAssignment[];
};

export type PortalPermissionAssignmentUpdateRequest = {
  subjectType: PermissionSubjectType;
  subjectId: string;
  subjectName: string;
  permissionCodes: string[];
};
