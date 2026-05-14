import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { SampleListJpaItem, SampleListJpaSaveRequest, SampleListJpaSearchFilter } from "./types";

export function fetchList(
  request: ListRequest<SampleListJpaSearchFilter>,
): Promise<ListResponse<SampleListJpaItem>> {
  return http.post<ListResponse<SampleListJpaItem>>("/api/samples-jpa/search", request);
}

export function createSample(request: SampleListJpaSaveRequest): Promise<SampleListJpaItem> {
  return http.post<SampleListJpaItem>("/api/samples-jpa", request);
}

export function updateSample(id: number, request: SampleListJpaSaveRequest): Promise<SampleListJpaItem> {
  return http.put<SampleListJpaItem>(`/api/samples-jpa/${id}`, request);
}

export function deleteSample(id: number): Promise<void> {
  return http.delete<void>(`/api/samples-jpa/${id}`);
}
