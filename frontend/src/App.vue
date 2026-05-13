<script setup lang="ts">
import { computed } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { ElMenu, ElMenuItem } from "element-plus";
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
  <div class="min-h-screen bg-slate-50">
    <!-- [11. 레이아웃 및 화면 스타일 표준] SI 포털형 상단 메뉴 레이아웃의 현재 기준이다. -->
    <header class="sticky top-0 z-50 border-b border-slate-200 bg-white shadow-sm">
      <div class="mx-auto flex max-w-screen-2xl items-center px-6">
        <div class="mr-6 shrink-0">
          <span class="text-base font-bold text-slate-800">Governance Portal</span>
          <span class="ml-2 text-xs font-semibold text-brand-teal">Sample</span>
        </div>
        <ElMenu
          mode="horizontal"
          :default-active="activeMenu"
          :ellipsis="false"
          class="flex-1"
          @select="handleSelect"
        >
          <!-- [12. 메뉴, 라우터, 권한 표준] 메뉴 항목은 라우트 meta 설정에서 생성한다. -->
          <ElMenuItem v-for="item in menuItems" :key="item.path" :index="item.path">
            {{ item.title }}
          </ElMenuItem>
        </ElMenu>
      </div>
    </header>
    <RouterView />
  </div>
</template>

<style>
.el-menu--horizontal.el-menu {
  border-bottom: none;
}
</style>
