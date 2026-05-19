import { http } from "@/shared/api/http";
import type { RefApprovalDecisionRequest, RefApprovalItem } from "./types";

export function fetchApprovalList(): Promise<RefApprovalItem[]> {
  return http.get<RefApprovalItem[]>("/api/reference/approvals");
}

export function fetchApproval(id: number): Promise<RefApprovalItem> {
  return http.get<RefApprovalItem>(`/api/reference/approvals/${id}`);
}

export function approve(id: number, request: RefApprovalDecisionRequest): Promise<RefApprovalItem> {
  return http.post<RefApprovalItem>(`/api/reference/approvals/${id}/approve`, request);
}

export function reject(id: number, request: RefApprovalDecisionRequest): Promise<RefApprovalItem> {
  return http.post<RefApprovalItem>(`/api/reference/approvals/${id}/reject`, request);
}
