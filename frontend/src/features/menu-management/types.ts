export type PortalMenuItem = {
  menuId: string;
  parentMenuId?: string | null;
  menuName: string;
  routePath?: string | null;
  permissionCode?: string | null;
  sortOrder: number;
  visible: boolean;
  enabled: boolean;
};

export type PortalMenuSearchFilter = {
  keyword?: string;
  parentMenuId?: string;
  visible?: boolean;
  enabled?: boolean;
};

export type PortalMenuSaveRequest = {
  menuId: string;
  parentMenuId?: string | null;
  menuName: string;
  routePath?: string | null;
  permissionCode?: string | null;
  sortOrder: number;
  visible: boolean;
  enabled: boolean;
  autoCreatePermission: boolean;
};
