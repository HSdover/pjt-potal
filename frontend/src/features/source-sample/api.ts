import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import { toListResponse } from "@/shared/utils/list-adapter";
import type { SourceSampleItem, SourceSampleSearchFilter } from "./types";

export async function fetchSourceSample() {
  return http.get<SourceSampleItem[]>("/api/catalog/source-sample");
}

// [7. 목록 조회 표준 계약] 샘플 API 응답을 표준 목록 응답으로 변환한다.
export async function fetchSourceSampleList(
  request: ListRequest<SourceSampleSearchFilter>,
): Promise<ListResponse<SourceSampleItem>> {
  const rows = await fetchSourceSample();
  const filtered = request.filters.region
    ? rows.filter((row) => row.sidoName === request.filters.region)
    : rows;

  return toListResponse(filtered, request);
}
