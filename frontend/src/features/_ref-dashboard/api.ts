import { http } from "@/shared/api/http";
import type { RefDashboardData } from "./types";

export function fetchDashboard(): Promise<RefDashboardData> {
  return http.get<RefDashboardData>("/api/reference/dashboards/main");
}
