import type { RouteLocationNormalized } from "vue-router";

export const samplePermissions = new Set([
  "DASHBOARD_READ",
  "METADATA_READ",
  "METADATA_SAVE",
  "SOURCE_SAMPLE_READ",
  "LINEAGE_READ",
  "TABLE_DETAIL_READ",
]);

// [12. 메뉴, 라우터, 권한 표준] 샘플 템플릿은 권한 판정 진입점을 먼저 고정한다.
export function hasPermission(auth?: string | string[]) {
  if (!auth) {
    return true;
  }

  const required = Array.isArray(auth) ? auth : [auth];
  return required.every((permission) => samplePermissions.has(permission));
}

export function canAccessRoute(route: RouteLocationNormalized) {
  return hasPermission(route.meta.auth as string | string[] | undefined);
}
