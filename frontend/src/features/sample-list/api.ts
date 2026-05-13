import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { SampleListItem, SampleListSearchFilter } from "./types";

export function fetchList(
  request: ListRequest<SampleListSearchFilter>,
): Promise<ListResponse<SampleListItem>> {
  return http.post<ListResponse<SampleListItem>>("/api/samples/search", request);
}
