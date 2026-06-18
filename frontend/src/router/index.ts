import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import { canAccessRoute } from "@/shared/auth/permissions";
import { useAuthStore } from "@/stores/auth";

type MenuRoot = {
  menuLevel1Key: string;
  menuLevel1Title: string;
  menuLevel1Order: number;
};

function menuRoot(key: string, title: string, order: number): MenuRoot {
  return {
    menuLevel1Key: key,
    menuLevel1Title: title,
    menuLevel1Order: order,
  };
}

function menuItem(root: MenuRoot, key: string, title: string, order: number) {
  return {
    ...root,
    menuLevel2Key: key,
    menuLevel2Title: title,
    menuLevel2Order: order,
  };
}

function placeholderRoute(
  path: string,
  root: MenuRoot,
  key: string,
  title: string,
  order: number,
  auth?: string | string[],
): RouteRecordRaw {
  return {
    path,
    component: () => import("@/views/MenuPlaceholderView.vue"),
    meta: {
      ...menuItem(root, key, title, order),
      title,
      menuTitle: title,
      menu: true,
      auth,
    },
  };
}

const metadataMenu = menuRoot("metadata", "메타데이터", 10);
const pipelineMenu = menuRoot("pipeline", "파이프라인관리", 20);
const aiDataMenu = menuRoot("ai-data", "AI데이터관리", 30);
const requestMenu = menuRoot("request", "신청관리", 40);
const dpViewerMenu = menuRoot("dp-viewer", "DP뷰어", 50);
const dashboardMenu = menuRoot("dashboard", "대시보드", 60);
const systemMenu = menuRoot("system", "시스템 관리", 70);
const developmentReferenceMenu = menuRoot("development-reference", "개발참고", 900);
const referenceDashboardMenu = menuItem(developmentReferenceMenu, "dashboard", "대시보드", 10);
const referenceGridSampleMenu = menuItem(developmentReferenceMenu, "grid-sample", "그리드 샘플", 20);

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/",
      component: () => import("@/views/DashboardView.vue"),
      meta: {
        ...menuItem(dashboardMenu, "daily", "일현황", 10),
        title: "일현황",
        menuTitle: "일현황",
        menu: true,
        auth: "DASHBOARD_READ",
      },
    },
    { path: "/dashboard", redirect: "/" },
    {
      path: "/login",
      component: () => import("@/views/LoginView.vue"),
      meta: { title: "로그인", menu: false, public: true },
    },
    {
      path: "/forbidden",
      component: () => import("@/views/ForbiddenView.vue"),
      meta: { title: "접근 제한", menu: false },
    },
    {
      path: "/metadata/management",
      component: () => import("@/features/integrated-meta-management/pages/IntegratedMetaManagementPage.vue"),
      meta: {
        ...menuItem(metadataMenu, "management", "메타관리", 10),
        title: "메타관리",
        menuTitle: "메타관리",
        menu: true,
        auth: "META_VIEW",
      },
    },
    { path: "/metadata/integrated", redirect: "/metadata/management" },
    placeholderRoute("/pipelines", pipelineMenu, "list", "파이프라인목록", 10, "PIPELINE_READ"),
    placeholderRoute("/ai-data/agent-mapping", aiDataMenu, "agent-mapping", "AI에이전트 매핑", 10, "AI_AGENT_READ"),
    placeholderRoute("/ai-data/data-mesh", aiDataMenu, "data-mesh", "데이터 매쉬", 20, "AI_AGENT_READ"),
    placeholderRoute("/requests/pipelines", requestMenu, "pipeline-requests", "파이프라인신청 목록", 10, "REQUEST_READ"),
    placeholderRoute("/requests/ai-agents", requestMenu, "ai-agent-requests", "AI에이전트 신청목록", 20, "REQUEST_READ"),
    placeholderRoute("/dp-viewer/document-parsing", dpViewerMenu, "document-parsing", "문서 파싱 목록", 10, "DP_VIEWER_READ"),
    placeholderRoute("/dashboard/pipelines", dashboardMenu, "pipeline-status", "파이프라인 현황", 20, "DASHBOARD_READ"),
    placeholderRoute("/dashboard/ai-agents", dashboardMenu, "ai-agent-status", "AI에이전트 현황", 30, "DASHBOARD_READ"),
    placeholderRoute("/system/notices", systemMenu, "notices", "공지사항", 10, "NOTICE_READ"),
    {
      path: "/system/permissions",
      component: () => import("@/features/permission-management/pages/PermissionManagementPage.vue"),
      meta: {
        ...menuItem(systemMenu, "permissions", "권한관리", 20),
        title: "권한관리",
        menuTitle: "권한관리",
        menu: true,
        auth: "PERMISSION_MANAGE",
      },
    },
    {
      path: "/system/menus",
      component: () => import("@/features/menu-management/pages/MenuManagementListPage.vue"),
      meta: {
        ...menuItem(systemMenu, "menus", "메뉴관리", 30),
        title: "메뉴관리",
        menuTitle: "메뉴관리",
        menu: true,
        auth: "MENU_MANAGE",
      },
    },
    {
      path: "/development-reference/dashboard-1",
      component: () => import("@/views/DashboardView.vue"),
      meta: {
        ...referenceDashboardMenu,
        title: "참고: 대시보드1",
        menuTitle: "대시보드1",
        menuLevel3Title: "대시보드1",
        menuLevel3Order: 10,
        menu: true,
        auth: "DASHBOARD_READ",
      },
    },
    {
      path: "/_ref-dashboard",
      component: () => import("@/features/_ref-dashboard/pages/RefDashboardPage.vue"),
      meta: {
        ...referenceDashboardMenu,
        title: "참고: 대시보드2",
        menuTitle: "대시보드2",
        menuLevel3Title: "대시보드2",
        menuLevel3Order: 20,
        menu: true,
        auth: "REF_VIEW",
      },
    },
    {
      path: "/sample-list",
      component: () => import("@/features/sample-list/pages/SampleListPage.vue"),
      meta: {
        ...referenceGridSampleMenu,
        title: "샘플 CRUD",
        menuTitle: "샘플 CRUD",
        menuLevel3Title: "샘플 CRUD",
        menuLevel3Order: 10,
        menu: true,
        auth: "SAMPLE_READ",
      },
    },
    {
      path: "/sample-list-jpa",
      component: () => import("@/features/sample-list-jpa/pages/SampleListJpaPage.vue"),
      meta: {
        ...referenceGridSampleMenu,
        title: "JPA 샘플",
        menuTitle: "JPA 샘플",
        menuLevel3Title: "JPA 샘플",
        menuLevel3Order: 20,
        menu: true,
        auth: "SAMPLE_JPA_READ",
      },
    },
    {
      path: "/development-reference/metadata-management",
      component: () => import("@/features/integrated-meta-management/pages/IntegratedMetaManagementPage.vue"),
      meta: {
        ...referenceGridSampleMenu,
        title: "통합메타관리",
        menuTitle: "통합메타관리",
        menuLevel3Title: "통합메타관리",
        menuLevel3Order: 30,
        menu: true,
        auth: "META_VIEW",
      },
    },
    {
      path: "/_ref-detail",
      component: () => import("@/features/_ref-detail/pages/RefDetailPage.vue"),
      meta: {
        ...menuItem(developmentReferenceMenu, "detail", "상세 조회", 30),
        title: "참고: 상세 조회",
        menuTitle: "상세 조회",
        menu: true,
        auth: "REF_VIEW",
      },
    },
    {
      path: "/_ref-form",
      component: () => import("@/features/_ref-form/pages/RefFormPage.vue"),
      meta: {
        ...menuItem(developmentReferenceMenu, "form", "신청/등록 폼", 40),
        title: "참고: 신청/등록 폼",
        menuTitle: "신청/등록 폼",
        menu: true,
        auth: "REF_VIEW",
      },
    },
    {
      path: "/_ref-approval",
      component: () => import("@/features/_ref-approval/pages/RefApprovalPage.vue"),
      meta: {
        ...menuItem(developmentReferenceMenu, "approval", "승인 워크플로우", 50),
        title: "참고: 승인 워크플로우",
        menuTitle: "승인 워크플로우",
        menu: true,
        auth: "REF_VIEW",
      },
    },
    {
      path: "/_ref-board",
      component: () => import("@/features/_ref-board/pages/RefBoardPage.vue"),
      meta: {
        ...menuItem(developmentReferenceMenu, "board", "게시판", 60),
        title: "참고: 게시판",
        menuTitle: "게시판",
        menu: true,
        auth: "REF_VIEW",
      },
    },
    {
      path: "/_ref-tags",
      component: () => import("@/features/_ref-tags/pages/RefTagComponentsPage.vue"),
      meta: {
        ...menuItem(developmentReferenceMenu, "tags", "공통 태그", 70),
        title: "참고: 공통 태그",
        menuTitle: "공통 태그",
        menu: true,
        auth: "REF_VIEW",
      },
    },
  ],
});

router.beforeEach(async (to) => {
  const auth = useAuthStore();

  if (!auth.isSessionReady) {
    await auth.loadSession();
  }

  if (to.meta.public === true) {
    if (to.path === "/login" && auth.isAuthenticated) {
      const redirect = to.query.redirect;
      return typeof redirect === "string" && redirect.startsWith("/") ? redirect : "/";
    }

    return true;
  }

  if (!auth.isAuthenticated) {
    return {
      path: "/login",
      query: { redirect: to.fullPath },
    };
  }

  if (!canAccessRoute(to)) {
    return to.path === "/forbidden" ? true : "/forbidden";
  }

  return true;
});

export default router;
