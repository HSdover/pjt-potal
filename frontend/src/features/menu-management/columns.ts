import type { ColDef } from "ag-grid-community";
import type { PortalMenuItem } from "./types";

export const columns: ColDef<PortalMenuItem>[] = [
  { field: "menuName", headerName: "메뉴명", minWidth: 150, flex: 1 },
  { field: "menuId", headerName: "메뉴 ID", minWidth: 190 },
  { field: "parentMenuId", headerName: "상위 메뉴", minWidth: 190 },
  { field: "routePath", headerName: "라우트", minWidth: 220 },
  { field: "permissionCode", headerName: "권한코드", minWidth: 160 },
  { field: "sortOrder", headerName: "정렬", width: 90 },
  { field: "visible", headerName: "노출", width: 90 },
  { field: "enabled", headerName: "사용", width: 90 },
];
