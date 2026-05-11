import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import { toListResponse } from "@/shared/utils/list-adapter";
import type { MetadataCatalogItem, MetadataSearchFilter } from "./types";

export async function fetchMetadata() {
  return http.get<MetadataCatalogItem[]>("/api/catalog/metadata");
}

// [7. 목록 조회 표준 계약] 현재 백엔드 전체조회 API를 SI 표준 목록 계약으로 맞춘다.
export async function fetchMetadataList(
  request: ListRequest<MetadataSearchFilter>,
): Promise<ListResponse<MetadataCatalogItem>> {
  const rows = await fetchMetadata();
  const filtered = request.filters.datasetType
    ? rows.filter((row) => row.datasetType === request.filters.datasetType)
    : rows;

  return toListResponse(filtered, request);
}
