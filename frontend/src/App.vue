<script setup lang="ts">
import { computed } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { ElMenu, ElMenuItem } from "element-plus";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { hasPermission } from "@/shared/auth/permissions";

const route = useRoute();
const router = useRouter();

const activeMenu = computed(() => route.path);
const menuItems = computed(() =>
  router.getRoutes()
    .filter((item) => item.meta.menu && hasPermission(item.meta.auth as string | string[] | undefined))
    .sort((left, right) => Number(left.meta.order ?? 0) - Number(right.meta.order ?? 0))
    .map((item) => ({
      path: item.path,
      title: String(item.meta.title ?? item.name ?? item.path),
    })),
);

function handleSelect(key: string) {
  router.push(key);
}
</script>

<template>
  <div class="portal-shell">
    <div class="portal-top-strip">
      <div class="portal-top-strip-inner">
        <span>Samsung Securities Governance Portal</span>
        <div class="portal-utility-links">
          <span>로그인</span>
          <span>검색</span>
          <span>바로가기</span>
        </div>
      </div>
    </div>

    <header class="portal-header">
      <div class="portal-header-inner">
        <div class="portal-brand">
          <div class="portal-brand-mark">POP</div>
          <div>
            <div class="portal-brand-title">Governance Portal</div>
            <div class="portal-brand-subtitle">AI DATA PLATFORM</div>
          </div>
        </div>
        <div class="portal-header-actions">
          <PortalButton variant="secondary">신청 현황</PortalButton>
          <PortalButton variant="secondary">운영 알림</PortalButton>
          <PortalButton variant="primary">Quick Menu</PortalButton>
        </div>
      </div>

      <nav class="portal-nav">
        <div class="portal-nav-inner">
          <ElMenu
            mode="horizontal"
            :default-active="activeMenu"
            :ellipsis="false"
            class="portal-menu"
            @select="handleSelect"
          >
            <!-- [12. 메뉴, 라우터, 권한 표준] 메뉴 항목은 라우트 meta 설정에서 생성한다. -->
            <ElMenuItem v-for="item in menuItems" :key="item.path" :index="item.path">
              {{ item.title }}
            </ElMenuItem>
          </ElMenu>
        </div>
      </nav>
    </header>
    <RouterView />
  </div>
</template>
