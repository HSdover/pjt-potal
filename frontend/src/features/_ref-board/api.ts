import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { RefBoardAttachment, RefBoardItem, RefBoardSaveRequest, RefBoardSearchFilter } from "./types";

export function fetchBoardList(request: ListRequest<RefBoardSearchFilter>): Promise<ListResponse<RefBoardItem>> {
  return http.post<ListResponse<RefBoardItem>>("/api/reference/boards/search", request);
}

export function fetchBoard(id: number): Promise<RefBoardItem> {
  return http.get<RefBoardItem>(`/api/reference/boards/${id}`);
}

export function createBoard(request: RefBoardSaveRequest): Promise<RefBoardItem> {
  return http.post<RefBoardItem>("/api/reference/boards", request);
}

export function updateBoard(id: number, request: RefBoardSaveRequest): Promise<RefBoardItem> {
  return http.put<RefBoardItem>(`/api/reference/boards/${id}`, request);
}

export function deleteBoard(id: number): Promise<void> {
  return http.delete<void>(`/api/reference/boards/${id}`);
}

export function uploadBoardAttachment(file: File): Promise<RefBoardAttachment> {
  const formData = new FormData();
  formData.append("file", file);
  return http.upload<RefBoardAttachment>("/api/reference/boards/attachments", formData);
}
