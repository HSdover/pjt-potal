<script setup lang="ts">
import { computed } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { ElMenu, ElMenuItem, ElSubMenu } from "element-plus";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { hasPermission } from "@/shared/auth/permissions";

const route = useRoute();
const router = useRouter();

type MenuItem = {
  path: string;
  title: string;
  order: number;
};

type MenuGroup = {
  key: string;
  title: string;
  order: number;
  items: MenuItem[];
};

const activeMenu = computed(() => route.path);
const menuGroups = computed(() => {
  const groups = new Map<string, MenuGroup>();

  router.getRoutes()
    .filter((item) => item.meta.menu && hasPermission(item.meta.auth as string | string[] | undefined))
    .forEach((item) => {
      const groupKey = String(item.meta.menuDomain ?? item.path);
      const groupTitle = String(item.meta.menuDomainTitle ?? item.meta.title ?? item.name ?? item.path);
      const groupOrder = Number(item.meta.menuDomainOrder ?? item.meta.order ?? 0);
      const screenTitle = String(item.meta.menuTitle ?? item.meta.title ?? item.name ?? item.path);

      if (!groups.has(groupKey)) {
        groups.set(groupKey, {
          key: groupKey,
          title: groupTitle,
          order: groupOrder,
          items: [],
        });
      }

      groups.get(groupKey)?.items.push({
        path: item.path,
        title: screenTitle,
        order: Number(item.meta.order ?? 0),
      });
    });

  return Array.from(groups.values())
    .map((group) => ({
      ...group,
      items: group.items.sort((left, right) => left.order - right.order),
    }))
    .sort((left, right) => left.order - right.order);
});

function handleSelect(key: string) {
  void router.push(key);
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
            <template v-for="group in menuGroups" :key="group.key">
              <ElMenuItem v-if="group.items.length === 1" :index="group.items[0].path">
                {{ group.title }}
              </ElMenuItem>
              <ElSubMenu v-else :index="group.key">
                <template #title>{{ group.title }}</template>
                <ElMenuItem v-for="item in group.items" :key="item.path" :index="item.path">
                  {{ item.title }}
                </ElMenuItem>
              </ElSubMenu>
            </template>
          </ElMenu>
        </div>
      </nav>
    </header>
    <RouterView />
  </div>
</template>
