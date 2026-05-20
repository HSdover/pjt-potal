import type { RouteLocationNormalized } from "vue-router";
import { useAuthStore } from "@/stores/auth";

export function hasPermission(auth?: string | string[]) {
  if (!auth) {
    return true;
  }

  const required = Array.isArray(auth) ? auth : [auth];
  return useAuthStore().hasPermissions(required);
}

export function canAccessRoute(route: RouteLocationNormalized) {
  return hasPermission(route.meta.auth as string | string[] | undefined);
}
