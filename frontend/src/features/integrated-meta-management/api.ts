import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { IntegratedMetaDetail, IntegratedMetaItem, IntegratedMetaSearchFilter } from "./types";

export function fetchIntegratedMetaList(
  request: ListRequest<IntegratedMetaSearchFilter>,
): Promise<ListResponse<IntegratedMetaItem>> {
  return http.post<ListResponse<IntegratedMetaItem>>("/api/metadata/integrated/search", request);
}

export function fetchIntegratedMetaDetail(metaId: string): Promise<IntegratedMetaDetail> {
  return http.get<IntegratedMetaDetail>(`/api/metadata/integrated/${metaId}`);
}
