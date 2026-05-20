import { http } from "./http";

export type CurrentUser = {
  userId: string;
  displayName: string;
  authenticated: boolean;
  permissions: string[];
};

export function fetchCurrentUser(): Promise<CurrentUser> {
  return http.get<CurrentUser>("/api/me");
}
