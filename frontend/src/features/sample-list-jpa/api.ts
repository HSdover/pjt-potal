import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type {
  SampleJpaExcelImportResult,
  SampleJpaExcelLargeExport,
  SampleListJpaItem,
  SampleListJpaSaveRequest,
  SampleListJpaSearchFilter,
} from "./types";

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

export function downloadExcel(request: ListRequest<SampleListJpaSearchFilter>): Promise<void> {
  return http.downloadPost("/api/samples-jpa/excel/download", request, {
    accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    filename: "sample-jpa.xlsx",
  });
}

export function requestLargeExcel(
  request: ListRequest<SampleListJpaSearchFilter>,
): Promise<SampleJpaExcelLargeExport> {
  return http.post<SampleJpaExcelLargeExport>("/api/samples-jpa/excel/large-download", request);
}

export function fetchLargeExcel(jobId: string): Promise<SampleJpaExcelLargeExport> {
  return http.get<SampleJpaExcelLargeExport>(`/api/samples-jpa/excel/large-download/${jobId}`);
}

export function downloadLargeExcel(jobId: string): Promise<void> {
  return http.downloadGet(`/api/samples-jpa/excel/large-download/${jobId}/file`, {
    accept: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    filename: `sample-jpa-large-${jobId}.xlsx`,
  });
}

export function uploadExcel(file: File): Promise<SampleJpaExcelImportResult> {
  const formData = new FormData();
  formData.append("file", file);
  return http.upload<SampleJpaExcelImportResult>("/api/samples-jpa/excel/upload", formData);
}
