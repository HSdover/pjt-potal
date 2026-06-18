<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  ElForm,
  ElFormItem,
  ElInputNumber,
  ElMessage,
  ElSwitch,
  ElTable,
  ElTableColumn,
  ElTag,
} from "element-plus";
import { Delete, Plus } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import { PortalSelect, PortalTextInput } from "@/shared/components/tags";
import { handleApiError } from "@/shared/api/error-handler";
import { confirmDelete, confirmSave, confirmUpdate } from "@/shared/feedback/confirm-dialog";
import { createPortalMenu, deletePortalMenu, fetchPortalMenus, searchPortalMenus, updatePortalMenu } from "../api";
import type { PortalMenuItem, PortalMenuSaveRequest } from "../types";

type MenuForm = {
  menuId: string;
  parentMenuId: string;
  menuName: string;
  routePath: string;
  permissionCode: string;
  sortOrder: number;
  visible: boolean;
  enabled: boolean;
  autoCreatePermission: boolean;
};

const loading = ref(false);
const saving = ref(false);
const deleting = ref(false);
const keyword = ref("");
const selectedMenuId = ref("");
const menus = ref<PortalMenuItem[]>([]);

const form = reactive<MenuForm>(emptyForm());

const filteredMenus = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  const sorted = [...menus.value].sort(compareMenu);
  if (!normalizedKeyword) {
    return sorted;
  }

  return sorted.filter((menu) =>
    [
      menu.menuId,
      menu.parentMenuId ?? "",
      menu.menuName,
      menu.routePath ?? "",
      menu.permissionCode ?? "",
    ].some((value) => value.toLowerCase().includes(normalizedKeyword)),
  );
});

const parentOptions = computed(() => [
  { label: "상위 없음", value: "" },
  ...menus.value
    .filter((menu) => menu.menuId !== form.menuId)
    .sort(compareMenu)
    .map((menu) => ({
      label: `${menu.menuName} (${menu.menuId})`,
      value: menu.menuId,
    })),
]);

const selectedMenu = computed(() =>
  menus.value.find((menu) => menu.menuId === selectedMenuId.value) ?? null,
);

const editMode = computed(() => Boolean(selectedMenu.value));

function emptyForm(): MenuForm {
  return {
    menuId: "",
    parentMenuId: "",
    menuName: "",
    routePath: "",
    permissionCode: "",
    sortOrder: 0,
    visible: true,
    enabled: true,
    autoCreatePermission: true,
  };
}

function compareMenu(left: PortalMenuItem, right: PortalMenuItem) {
  const parentCompare = (left.parentMenuId ?? "").localeCompare(right.parentMenuId ?? "");
  if (parentCompare !== 0) {
    return parentCompare;
  }
  const orderCompare = left.sortOrder - right.sortOrder;
  return orderCompare === 0 ? left.menuId.localeCompare(right.menuId) : orderCompare;
}

async function load() {
  loading.value = true;
  try {
    const response = await searchPortalMenus({
      pageNo: 1,
      pageSize: 1000,
      sort: [{ field: "sortOrder", direction: "asc" }],
      filters: {
        keyword: keyword.value.trim(),
      },
    });
    menus.value = response.rows;
  } catch (error) {
    await loadFlatMenus(error);
  } finally {
    loading.value = false;
  }
}

async function loadFlatMenus(searchError: unknown) {
  try {
    menus.value = await fetchPortalMenus();
  } catch {
    handleApiError(searchError, "메뉴 정보를 조회하지 못했습니다.");
  }
}

function selectMenu(menu: PortalMenuItem) {
  selectedMenuId.value = menu.menuId;
  Object.assign(form, {
    menuId: menu.menuId,
    parentMenuId: menu.parentMenuId ?? "",
    menuName: menu.menuName,
    routePath: menu.routePath ?? "",
    permissionCode: menu.permissionCode ?? "",
    sortOrder: menu.sortOrder,
    visible: menu.visible,
    enabled: menu.enabled,
    autoCreatePermission: false,
  });
}

function newMenu() {
  selectedMenuId.value = "";
  Object.assign(form, emptyForm());
}

function validateForm() {
  if (!form.menuId.trim()) {
    ElMessage.warning("메뉴 ID를 입력하세요.");
    return false;
  }
  if (!form.menuName.trim()) {
    ElMessage.warning("메뉴명을 입력하세요.");
    return false;
  }
  if (form.permissionCode?.trim() && !form.routePath?.trim()) {
    ElMessage.warning("권한 코드가 있는 메뉴는 라우트 경로를 입력하세요.");
    return false;
  }
  return true;
}

function normalizedRequest(): PortalMenuSaveRequest {
  return {
    menuId: form.menuId.trim(),
    parentMenuId: form.parentMenuId?.trim() || null,
    menuName: form.menuName.trim(),
    routePath: form.routePath?.trim() || null,
    permissionCode: form.permissionCode?.trim() || null,
    sortOrder: Number(form.sortOrder ?? 0),
    visible: form.visible,
    enabled: form.enabled,
    autoCreatePermission: form.autoCreatePermission,
  };
}

async function save() {
  if (!validateForm()) {
    return;
  }

  const request = normalizedRequest();
  const confirmed = editMode.value
    ? await confirmUpdate(`${form.menuName} 메뉴를 수정하시겠습니까?`)
    : await confirmSave(`${form.menuName} 메뉴를 등록하시겠습니까?`);
  if (!confirmed) {
    return;
  }

  saving.value = true;
  try {
    const saved = editMode.value
      ? await updatePortalMenu(selectedMenuId.value, request)
      : await createPortalMenu(request);
    upsertMenu(saved);
    selectMenu(saved);
    ElMessage.success("메뉴가 저장되었습니다.");
  } catch (error) {
    handleApiError(error, "메뉴 저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

async function remove() {
  if (!selectedMenu.value) {
    ElMessage.warning("삭제할 메뉴를 선택하세요.");
    return;
  }

  const confirmed = await confirmDelete(`${selectedMenu.value.menuName} 메뉴를 삭제하시겠습니까?`);
  if (!confirmed) {
    return;
  }

  deleting.value = true;
  try {
    await deletePortalMenu(selectedMenu.value.menuId);
    menus.value = menus.value.filter((menu) => menu.menuId !== selectedMenu.value?.menuId);
    newMenu();
    ElMessage.success("메뉴가 삭제되었습니다.");
  } catch (error) {
    handleApiError(error, "메뉴 삭제에 실패했습니다.");
  } finally {
    deleting.value = false;
  }
}

function upsertMenu(saved: PortalMenuItem) {
  menus.value = [
    ...menus.value.filter((menu) => menu.menuId !== saved.menuId),
    saved,
  ];
}

function rowKey(row: PortalMenuItem) {
  return row.menuId;
}

function statusTag(menu: PortalMenuItem) {
  if (!menu.enabled) {
    return { label: "비활성", type: "warning" as const };
  }
  if (!menu.visible) {
    return { label: "숨김", type: "info" as const };
  }
  return { label: "노출", type: "success" as const };
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="메뉴관리" description="메뉴 경로와 진입 권한을 관리합니다.">
    <template #toolbar>
      <SearchPanel>
        <PortalTextInput v-model="keyword" clearable placeholder="메뉴 검색" class="w-full md:!w-80" />

        <template #actions>
          <AuthButton auth="MENU_MANAGE" :icon="Plus" @click="newMenu">
            신규
          </AuthButton>
          <AuthButton auth="MENU_MANAGE" type="primary" :disabled="saving" @click="save">
            저장
          </AuthButton>
          <AuthButton auth="MENU_MANAGE" type="danger" :icon="Delete" :disabled="!selectedMenu || deleting" @click="remove">
            삭제
          </AuthButton>
        </template>

        <template #summary>
          전체 메뉴 {{ menus.length.toLocaleString() }}개
          <span class="ml-2 text-slate-400">표시 {{ filteredMenus.length.toLocaleString() }}개</span>
        </template>
      </SearchPanel>
    </template>

    <div class="grid gap-4 xl:grid-cols-[minmax(0,1fr)_420px]">
      <section class="portal-section">
        <ElTable
          v-loading="loading"
          :data="filteredMenus"
          :row-key="rowKey"
          border
          height="560"
          highlight-current-row
          class="menu-table"
          @row-click="selectMenu"
        >
          <ElTableColumn label="상태" width="90" fixed>
            <template #default="{ row }: { row: PortalMenuItem }">
              <ElTag :type="statusTag(row).type" effect="plain" size="small">
                {{ statusTag(row).label }}
              </ElTag>
            </template>
          </ElTableColumn>
          <ElTableColumn prop="menuName" label="메뉴명" min-width="150" />
          <ElTableColumn prop="menuId" label="메뉴 ID" min-width="190" />
          <ElTableColumn prop="parentMenuId" label="상위 메뉴" min-width="190" />
          <ElTableColumn prop="routePath" label="라우트" min-width="220" show-overflow-tooltip />
          <ElTableColumn prop="permissionCode" label="권한코드" min-width="160" />
          <ElTableColumn prop="sortOrder" label="정렬" width="90" align="right" />
        </ElTable>
      </section>

      <section class="portal-section">
        <div class="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 class="text-base font-bold text-slate-900">메뉴 정보</h2>
            <p class="mt-1 text-xs text-slate-500">{{ editMode ? "선택한 메뉴를 수정합니다." : "신규 메뉴를 등록합니다." }}</p>
          </div>
          <ElTag v-if="editMode" type="info" effect="plain">수정</ElTag>
          <ElTag v-else type="success" effect="plain">신규</ElTag>
        </div>

        <ElForm label-position="top" class="menu-form">
          <ElFormItem label="메뉴 ID">
            <PortalTextInput v-model="form.menuId" :disabled="editMode" placeholder="system-menus" />
          </ElFormItem>
          <ElFormItem label="상위 메뉴">
            <PortalSelect v-model="form.parentMenuId" :options="parentOptions" />
          </ElFormItem>
          <ElFormItem label="메뉴명">
            <PortalTextInput v-model="form.menuName" placeholder="메뉴관리" />
          </ElFormItem>
          <ElFormItem label="라우트 경로">
            <PortalTextInput v-model="form.routePath" placeholder="/system/menus" />
          </ElFormItem>
          <ElFormItem label="권한 코드">
            <PortalTextInput v-model="form.permissionCode" placeholder="MENU_MANAGE" />
          </ElFormItem>
          <ElFormItem label="정렬 순서">
            <ElInputNumber v-model="form.sortOrder" :min="0" :step="10" class="!w-full" />
          </ElFormItem>
          <div class="grid grid-cols-2 gap-3">
            <ElFormItem label="메뉴 노출">
              <ElSwitch v-model="form.visible" />
            </ElFormItem>
            <ElFormItem label="사용 여부">
              <ElSwitch v-model="form.enabled" />
            </ElFormItem>
          </div>
          <ElFormItem label="권한 자동 생성">
            <ElSwitch v-model="form.autoCreatePermission" :disabled="editMode" />
          </ElFormItem>
        </ElForm>
      </section>
    </div>
  </GridPageLayout>
</template>

<style scoped>
.menu-table :deep(.el-table__cell) {
  vertical-align: middle;
}

.menu-form :deep(.el-form-item) {
  margin-bottom: 14px;
}
</style>
