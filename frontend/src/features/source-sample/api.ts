import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { SourceSampleItem, SourceSampleSearchFilter } from "./types";

export async function fetchSourceSample() {
  return http.get<SourceSampleItem[]>("/api/source-sample");
}

// [7. 목록 조회 표준 계약] 샘플 API 응답을 표준 목록 응답으로 변환한다.
export async function fetchSourceSampleList(
  request: ListRequest<SourceSampleSearchFilter>,
): Promise<ListResponse<SourceSampleItem>> {
  return http.post<ListResponse<SourceSampleItem>>("/api/source-sample/search", request);
}
