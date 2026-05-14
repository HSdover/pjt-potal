import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { SampleListItem, SampleListSaveRequest, SampleListSearchFilter } from "./types";

export function fetchList(
  request: ListRequest<SampleListSearchFilter>,
): Promise<ListResponse<SampleListItem>> {
  return http.post<ListResponse<SampleListItem>>("/api/samples/search", request);
}

export function createSample(request: SampleListSaveRequest): Promise<SampleListItem> {
  return http.post<SampleListItem>("/api/samples", request);
}

export function updateSample(id: number, request: SampleListSaveRequest): Promise<SampleListItem> {
  return http.put<SampleListItem>(`/api/samples/${id}`, request);
}

export function deleteSample(id: number): Promise<void> {
  return http.delete<void>(`/api/samples/${id}`);
}
