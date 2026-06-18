import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { PortalMenuItem, PortalMenuSaveRequest, PortalMenuSearchFilter } from "./types";

const BASE_PATH = "/api/system/menus";

export function searchPortalMenus(
  request: ListRequest<PortalMenuSearchFilter>,
): Promise<ListResponse<PortalMenuItem>> {
  return http.post<ListResponse<PortalMenuItem>>(`${BASE_PATH}/search`, request);
}

export function fetchPortalMenus(): Promise<PortalMenuItem[]> {
  return http.get<PortalMenuItem[]>(BASE_PATH);
}

export function createPortalMenu(request: PortalMenuSaveRequest): Promise<PortalMenuItem> {
  return http.post<PortalMenuItem>(BASE_PATH, request);
}

export function updatePortalMenu(menuId: string, request: PortalMenuSaveRequest): Promise<PortalMenuItem> {
  return http.put<PortalMenuItem>(`${BASE_PATH}/${encodeURIComponent(menuId)}`, request);
}

export function deletePortalMenu(menuId: string): Promise<void> {
  return http.delete<void>(`${BASE_PATH}/${encodeURIComponent(menuId)}`);
}
