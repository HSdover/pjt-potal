import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { canAccessRoute } from "@/shared/auth/permissions";

// [참고 화면] dev profile에서만 노출. .env.development=true, .env.production=false 기본값.
const referenceMenuEnabled = import.meta.env.VITE_REFERENCE_MENU_ENABLED === "true";

const referenceRoutes: RouteRecordRaw[] = referenceMenuEnabled
  ? [
      {
        path: "/_ref-dashboard",
        component: () => import("@/features/_ref-dashboard/pages/RefDashboardPage.vue"),
        meta: { title: "참고: 대시보드", menu: true, order: 910, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-detail",
        component: () => import("@/features/_ref-detail/pages/RefDetailPage.vue"),
        meta: { title: "참고: 상세 조회", menu: true, order: 920, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-form",
        component: () => import("@/features/_ref-form/pages/RefFormPage.vue"),
        meta: { title: "참고: 신청/등록 폼", menu: true, order: 930, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-approval",
        component: () => import("@/features/_ref-approval/pages/RefApprovalPage.vue"),
        meta: { title: "참고: 승인 워크플로우", menu: true, order: 940, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-tags",
        component: () => import("@/features/_ref-tags/pages/RefTagComponentsPage.vue"),
        meta: { title: "참고: 공통 태그", menu: true, order: 950, auth: "REF_VIEW" },
      },
    ]
  : [];

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // [12. 메뉴, 라우터, 권한 표준] 라우트별 권한 코드를 meta에 둔다.
    {
      path: "/",
      component: () => import("@/views/DashboardView.vue"),
      meta: { title: "대시보드", menu: true, order: 10, auth: "DASHBOARD_READ" },
    },
    { path: "/dashboard", redirect: "/" },
    {
      path: "/forbidden",
      component: () => import("@/views/ForbiddenView.vue"),
      meta: { title: "접근 제한", menu: false },
    },
    {
      path: "/sample-list",
      component: () => import("@/features/sample-list/pages/SampleListPage.vue"),
      meta: { title: "샘플 CRUD", menu: true, order: 20, auth: "SAMPLE_READ" },
    },
    {
      path: "/sample-list-jpa",
      component: () => import("@/features/sample-list-jpa/pages/SampleListJpaPage.vue"),
      meta: { title: "JPA 샘플", menu: true, order: 30, auth: "SAMPLE_JPA_READ" },
    },
    ...referenceRoutes,
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
