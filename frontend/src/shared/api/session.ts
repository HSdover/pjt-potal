import { http } from "./http";

export type CurrentUser = {
  userId: string;
  displayName: string;
  authenticated: boolean;
  permissions: string[];
};

export type LocalLoginRequest = {
  userId: string;
  password: string;
};

export function fetchCurrentUser(): Promise<CurrentUser> {
  return http.get<CurrentUser>("/api/me");
}

export function loginLocal(request: LocalLoginRequest): Promise<CurrentUser> {
  return http.post<CurrentUser>("/api/auth/login", request);
}

export function logoutLocal(): Promise<void> {
  return http.post<void>("/api/auth/logout");
}
