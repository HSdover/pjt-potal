import { createRouter, createWebHistory } from "vue-router";
import DashboardView from "@/views/DashboardView.vue";
import { canAccessRoute } from "@/shared/auth/permissions";
import MetadataListPage from "@/features/metadata/pages/MetadataListPage.vue";
import SourceSampleListPage from "@/features/source-sample/pages/SourceSampleListPage.vue";
import LineageView from "@/views/LineageView.vue";
import TableDetailSampleView from "@/views/TableDetailSampleView.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // [12. 메뉴, 라우터, 권한 표준] 라우트별 권한 코드를 meta에 둔다.
    { path: "/",              component: DashboardView, meta: { auth: "DASHBOARD_READ" } },
    { path: "/dashboard",     redirect: "/" },
    { path: "/metadata",      component: MetadataListPage, meta: { auth: "METADATA_READ" } },
    { path: "/source-sample", component: SourceSampleListPage, meta: { auth: "SOURCE_SAMPLE_READ" } },
    { path: "/lineage",       component: LineageView, meta: { auth: "LINEAGE_READ" } },
    { path: "/table-detail-sample", component: TableDetailSampleView, meta: { auth: "TABLE_DETAIL_READ" } },
  ],
});

router.beforeEach((to) => {
  // [12. 메뉴, 라우터, 권한 표준] 화면 접근 권한의 공통 진입점이다.
  if (!canAccessRoute(to)) {
    return "/";
  }

  return true;
});

export default router;
