import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { canAccessRoute } from "@/shared/auth/permissions";

const referenceMenuEnabled = import.meta.env.VITE_REFERENCE_MENU_ENABLED === "true";

const dashboardMenu = {
  menuDomain: "dashboard",
  menuDomainTitle: "대시보드",
  menuDomainOrder: 10,
};

const sampleMenu = {
  menuDomain: "sample",
  menuDomainTitle: "샘플",
  menuDomainOrder: 20,
};

const referenceMenu = {
  menuDomain: "reference",
  menuDomainTitle: "개발참고",
  menuDomainOrder: 900,
};

const referenceRoutes: RouteRecordRaw[] = referenceMenuEnabled
  ? [
      {
        path: "/_ref-dashboard",
        component: () => import("@/features/_ref-dashboard/pages/RefDashboardPage.vue"),
        meta: { ...referenceMenu, title: "참고: 대시보드", menuTitle: "대시보드", menu: true, order: 910, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-detail",
        component: () => import("@/features/_ref-detail/pages/RefDetailPage.vue"),
        meta: { ...referenceMenu, title: "참고: 상세 조회", menuTitle: "상세 조회", menu: true, order: 920, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-form",
        component: () => import("@/features/_ref-form/pages/RefFormPage.vue"),
        meta: { ...referenceMenu, title: "참고: 신청/등록 폼", menuTitle: "신청/등록 폼", menu: true, order: 930, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-approval",
        component: () => import("@/features/_ref-approval/pages/RefApprovalPage.vue"),
        meta: { ...referenceMenu, title: "참고: 승인 워크플로우", menuTitle: "승인 워크플로우", menu: true, order: 940, auth: "REF_VIEW" },
      },
      {
        path: "/_ref-tags",
        component: () => import("@/features/_ref-tags/pages/RefTagComponentsPage.vue"),
        meta: { ...referenceMenu, title: "참고: 공통 태그", menuTitle: "공통 태그", menu: true, order: 950, auth: "REF_VIEW" },
      },
    ]
  : [];

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: () => import("@/views/DashboardView.vue"),
      meta: { ...dashboardMenu, title: "대시보드", menuTitle: "현황", menu: true, order: 10, auth: "DASHBOARD_READ" },
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
      meta: { ...sampleMenu, title: "샘플 CRUD", menuTitle: "조회/등록", menu: true, order: 20, auth: "SAMPLE_READ" },
    },
    {
      path: "/sample-list-jpa",
      component: () => import("@/features/sample-list-jpa/pages/SampleListJpaPage.vue"),
      meta: { ...sampleMenu, title: "JPA 샘플", menuTitle: "JPA 조회/등록", menu: true, order: 30, auth: "SAMPLE_JPA_READ" },
    },
    ...referenceRoutes,
  ],
});

router.beforeEach((to) => {
  if (!canAccessRoute(to)) {
    return to.path === "/forbidden" ? true : "/forbidden";
  }

  return true;
});

export default router;
