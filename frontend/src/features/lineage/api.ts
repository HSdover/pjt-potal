import { http } from "@/shared/api/http";
import type { LineageFlowItem } from "./types";

// [8. 공통 API 클라이언트] 리니지 조회도 화면 직접 fetch 대신 http 래퍼를 사용한다.
export function fetchLineage() {
  return http.get<LineageFlowItem[]>("/api/lineage");
}
