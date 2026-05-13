import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { MetadataItem, MetadataSearchFilter } from "./types";

export async function fetchMetadata() {
  return http.get<MetadataItem[]>("/api/metadata");
}

// [7. 목록 조회 표준 계약] 서버 표준 목록 계약으로 메타데이터를 조회한다.
export async function fetchMetadataList(
  request: ListRequest<MetadataSearchFilter>,
): Promise<ListResponse<MetadataItem>> {
  return http.post<ListResponse<MetadataItem>>("/api/metadata/search", request);
}
