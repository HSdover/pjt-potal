import { http } from "@/shared/api/http";
import type { RefDetailItem } from "./types";

export function fetchDetailList(): Promise<RefDetailItem[]> {
  return http.get<RefDetailItem[]>("/api/reference/details");
}

export function fetchDetail(id: number): Promise<RefDetailItem> {
  return http.get<RefDetailItem>(`/api/reference/details/${id}`);
}
