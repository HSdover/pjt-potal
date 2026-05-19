import { http } from "@/shared/api/http";
import type { RefFormItem, RefFormSaveRequest } from "./types";

export function fetchFormList(): Promise<RefFormItem[]> {
  return http.get<RefFormItem[]>("/api/reference/forms");
}

export function fetchForm(id: number): Promise<RefFormItem> {
  return http.get<RefFormItem>(`/api/reference/forms/${id}`);
}

export function createForm(request: RefFormSaveRequest): Promise<RefFormItem> {
  return http.post<RefFormItem>("/api/reference/forms", request);
}

export function updateForm(id: number, request: RefFormSaveRequest): Promise<RefFormItem> {
  return http.put<RefFormItem>(`/api/reference/forms/${id}`, request);
}
