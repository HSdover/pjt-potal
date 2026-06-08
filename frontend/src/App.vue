<script setup lang="ts">
import { computed } from "vue";
import { RouterView, useRoute, useRouter } from "vue-router";
import { ElMenu, ElMenuItem, ElSubMenu } from "element-plus";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { hasPermission } from "@/shared/auth/permissions";
import { useAuthStore } from "@/stores/auth";

const route = useRoute();
const router = useRouter();
const auth = useAuthStore();

type MenuScreen = {
  path: string;
  title: string;
  order: number;
};

type MenuSection = {
  key: string;
  title: string;
  order: number;
  path: string;
  children: MenuScreen[];
};

type MenuRoot = {
  key: string;
  title: string;
  order: number;
  sections: MenuSection[];
};

const activeMenu = computed(() => route.path);
const publicRoute = computed(() => route.meta.public === true);
const menuGroups = computed(() => {
  const roots = new Map<string, MenuRoot>();

  router.getRoutes()
    .filter((item) => item.meta.menu && hasPermission(item.meta.auth as string | string[] | undefined))
    .forEach((item) => {
      const rootKey = String(item.meta.menuLevel1Key ?? item.meta.menuDomain ?? item.path);
      const rootTitle = String(item.meta.menuLevel1Title ?? item.meta.menuDomainTitle ?? item.meta.title ?? item.name ?? item.path);
      const rootOrder = Number(item.meta.menuLevel1Order ?? item.meta.menuDomainOrder ?? item.meta.order ?? 0);
      const sectionKey = `${rootKey}:${String(item.meta.menuLevel2Key ?? item.path)}`;
      const sectionTitle = String(item.meta.menuLevel2Title ?? item.meta.menuTitle ?? item.meta.title ?? item.name ?? item.path);
      const sectionOrder = Number(item.meta.menuLevel2Order ?? item.meta.order ?? 0);
      const screenTitle = String(item.meta.menuLevel3Title ?? item.meta.menuTitle ?? item.meta.title ?? item.name ?? item.path);
      const screenOrder = Number(item.meta.menuLevel3Order ?? item.meta.order ?? 0);
      const hasThirdLevel = Boolean(item.meta.menuLevel3Title);

      if (!roots.has(rootKey)) {
        roots.set(rootKey, {
          key: rootKey,
          title: rootTitle,
          order: rootOrder,
          sections: [],
        });
      }

      const root = roots.get(rootKey);
      if (!root) {
        return;
      }

      let section = root.sections.find((candidate) => candidate.key === sectionKey);
      if (!section) {
        section = {
          key: sectionKey,
          title: sectionTitle,
          order: sectionOrder,
          path: "",
          children: [],
        };
        root.sections.push(section);
      }

      if (hasThirdLevel) {
        section.children.push({
          path: item.path,
          title: screenTitle,
          order: screenOrder,
        });
        return;
      }

      section.path = item.path;
      section.title = sectionTitle;
    });

  return Array.from(roots.values())
    .map((root) => ({
      ...root,
      sections: root.sections
        .map((section) => ({
          ...section,
          children: section.children.sort((left, right) => left.order - right.order),
        }))
        .sort((left, right) => left.order - right.order),
    }))
    .sort((left, right) => left.order - right.order);
});

function handleSelect(key: string) {
  void router.push(key);
}

async function logout() {
  await auth.logout();
  void router.replace("/login");
}
</script>

<template>
  <RouterView v-if="publicRoute" />
  <div v-else class="portal-shell">
    <div class="portal-top-strip">
      <div class="portal-top-strip-inner">
        <span>Samsung Securities Governance Portal</span>
        <div class="portal-utility-links">
          <span>{{ auth.userName }}</span>
          <button type="button" class="portal-utility-button" @click="logout">로그아웃</button>
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
            <template v-for="root in menuGroups" :key="root.key">
              <ElSubMenu :index="root.key">
                <template #title>{{ root.title }}</template>

                <template v-for="section in root.sections" :key="section.key">
                  <ElSubMenu v-if="section.children.length > 0" :index="section.key">
                    <template #title>{{ section.title }}</template>
                    <ElMenuItem v-for="screen in section.children" :key="screen.path" :index="screen.path">
                      {{ screen.title }}
                    </ElMenuItem>
                  </ElSubMenu>
                  <ElMenuItem v-else-if="section.path" :index="section.path">
                    {{ section.title }}
                  </ElMenuItem>
                </template>
              </ElSubMenu>
            </template>
          </ElMenu>
        </div>
      </nav>
    </header>
    <RouterView />
  </div>
</template>
