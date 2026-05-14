import { createRouter, createWebHistory } from "vue-router";
import { canAccessRoute } from "@/shared/auth/permissions";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // [12. 메뉴, 라우터, 권한 표준] 라우트별 권한 코드를 meta에 둔다.
    {
      path: "/",
      component: () => import("@/views/DashboardView.vue"),
      meta: { title: "대시보드", menu: true, order: 10, auth: "DASHBOARD_READ" },
    },
    { path: "/dashboard",     redirect: "/" },
    {
      path: "/metadata",
      component: () => import("@/features/metadata/pages/MetadataListPage.vue"),
      meta: { title: "메타데이터", menu: true, order: 20, auth: "METADATA_READ" },
    },
    {
      path: "/source-sample",
      component: () => import("@/features/source-sample/pages/SourceSampleListPage.vue"),
      meta: { title: "원천 샘플", menu: true, order: 30, auth: "SOURCE_SAMPLE_READ" },
    },
    {
      path: "/lineage",
      component: () => import("@/features/lineage/pages/LineagePage.vue"),
      meta: { title: "데이터 리니지", menu: true, order: 40, auth: "LINEAGE_READ" },
    },
    {
      path: "/forbidden",
      component: () => import("@/views/ForbiddenView.vue"),
      meta: { title: "접근 제한", menu: false },
    },
    {
      path: "/sample-list",
      component: () => import("@/features/sample-list/pages/SampleListPage.vue"),
      meta: { title: "테스트페이지", menu: true, order: 50, auth: "SAMPLE_READ" },
    },
    {
      path: "/sample-list-jpa",
      component: () => import("@/features/sample-list-jpa/pages/SampleListJpaPage.vue"),
      meta: { title: "JPA 샘플", menu: true, order: 60, auth: "SAMPLE_JPA_READ" },
    },
  ],
});

router.beforeEach((to) => {
  // [12. 메뉴, 라우터, 권한 표준] 화면 접근 권한의 공통 진입점이다.
  if (!canAccessRoute(to)) {
    return to.path === "/forbidden" ? true : "/forbidden";
  }

  return true;
});

export default router;
