import { existsSync, mkdirSync, writeFileSync } from "node:fs";
import path from "node:path";

const SUPPORTED_PAGE_TYPES = ["search-grid", "integrated-meta-grid"];

export function generatePage(options, frontendRoot = process.cwd()) {
  const pageName = options.pageName;
  const pageType = options.pageType ?? "search-grid";
  const force = options.force === true;
  const dryRun = options.dryRun === true;

  validateOptions({ pageName, pageType });

  const featureRoot = path.join(frontendRoot, "src", "features", pageName);
  const pagesRoot = path.join(featureRoot, "pages");
  const pascalName = toPascalCase(pageName);
  const pageComponentName = pascalName.endsWith("List") ? `${pascalName}Page` : `${pascalName}ListPage`;
  const baseTypeName = pageComponentName.replace(/Page$/, "");
  const rowTypeName = `${baseTypeName}Item`;
  const filterTypeName = `${baseTypeName}SearchFilter`;
  const authCode = options.authCode ?? `${pageName.replaceAll("-", "_").toUpperCase()}_READ`;
  const apiPath = options.apiPath ?? `/api/${pageName}/search`;
  const title = options.title ?? toTitle(pageName);
  const description = options.description ?? "검색 그리드 화면입니다.";

  const files = [
    {
      path: path.join(featureRoot, "types.ts"),
      content: typesTemplateFor(pageType, rowTypeName, filterTypeName),
    },
    {
      path: path.join(featureRoot, "columns.ts"),
      content: columnsTemplateFor(pageType, rowTypeName),
    },
    {
      path: path.join(featureRoot, "api.ts"),
      content: apiTemplate(rowTypeName, filterTypeName, apiPath),
    },
    {
      path: path.join(pagesRoot, `${pageComponentName}.vue`),
      content: pageTemplateFor(pageType, {
        rowTypeName,
        filterTypeName,
        title,
        description,
        authCode,
      }),
    },
  ];

  for (const file of files) {
    if (existsSync(file.path) && !force) {
      throw new Error(`Refusing to overwrite existing file: ${relative(frontendRoot, file.path)}. Use --force to replace generated files.`);
    }
  }

  if (!dryRun) {
    mkdirSync(pagesRoot, { recursive: true });

    for (const file of files) {
      writeFileSync(file.path, file.content, "utf8");
    }
  }

  return {
    dryRun,
    pageComponentName,
    apiPath,
    files: files.map((file) => relative(frontendRoot, file.path)),
  };
}

export function toPascalCase(value) {
  return value
    .split("-")
    .filter(Boolean)
    .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
    .join("");
}

function validateOptions({ pageName, pageType }) {
  if (!pageName) {
    throw new Error("Feature name is required.");
  }

  if (!SUPPORTED_PAGE_TYPES.includes(pageType)) {
    throw new Error(`Unsupported page type: ${pageType}. Supported page types: ${SUPPORTED_PAGE_TYPES.join(", ")}.`);
  }

  if (!/^[a-z][a-z0-9-]*$/.test(pageName)) {
    throw new Error("Feature name must be kebab-case, for example user-list or metadata-history.");
  }
}

function toTitle(value) {
  return value
    .split("-")
    .map((part) => `${part.charAt(0).toUpperCase()}${part.slice(1)}`)
    .join(" ");
}

function relative(frontendRoot, filePath) {
  return path.relative(frontendRoot, filePath).replaceAll("\\", "/");
}

function typesTemplateFor(pageType, rowTypeName, filterTypeName) {
  if (pageType === "integrated-meta-grid") {
    return integratedMetaTypesTemplate(rowTypeName, filterTypeName);
  }

  return searchGridTypesTemplate(rowTypeName, filterTypeName);
}

function columnsTemplateFor(pageType, rowTypeName) {
  if (pageType === "integrated-meta-grid") {
    return integratedMetaColumnsTemplate(rowTypeName);
  }

  return searchGridColumnsTemplate(rowTypeName);
}

function pageTemplateFor(pageType, context) {
  if (pageType === "integrated-meta-grid") {
    return integratedMetaPageTemplate(context);
  }

  return searchGridPageTemplate(context);
}

function searchGridTypesTemplate(rowTypeName, filterTypeName) {
  return `export type ${rowTypeName} = {
  id: number;
  name: string;
  description: string;
};

export type ${filterTypeName} = {
  keyword?: string;
};
`;
}

function searchGridColumnsTemplate(rowTypeName) {
  return `import type { ColDef } from "ag-grid-community";
import type { ${rowTypeName} } from "./types";

export const columns: ColDef<${rowTypeName}>[] = [
  { field: "id", headerName: "ID", width: 100 },
  { field: "name", headerName: "명칭", minWidth: 180, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 240, flex: 1 },
];
`;
}

function integratedMetaTypesTemplate(rowTypeName, filterTypeName) {
  return `export type MetaType = "STRUCTURED" | "FILE" | "SEMI_STRUCTURED";

export type MetaSearchType = "BUSINESS" | "TECHNICAL" | "FILE" | "OWNER" | "SECURITY" | "STORAGE";

export type ${filterTypeName} = {
  metaType: MetaType;
  searchTypes: MetaSearchType[];
  keyword?: string;
};

export type ${rowTypeName} = {
  metaId: string;
  metaType: MetaType;
  metaName: string;
  assetKind: string;
  sourceSystem: string;
  ownerDepartment: string;
  securityLevel: string;
  searchTypes: MetaSearchType[];
  updatedAt: string;
};
`;
}

function integratedMetaColumnsTemplate(rowTypeName) {
  return `import type { ColDef } from "ag-grid-community";
import type { ${rowTypeName} } from "./types";

function formatDate(value?: string) {
  if (!value) {
    return "-";
  }

  return value.replace("T", " ").slice(0, 16);
}

export const columns: ColDef<${rowTypeName}>[] = [
  { field: "metaId", headerName: "메타ID", width: 120 },
  { field: "metaName", headerName: "메타명", minWidth: 220, flex: 1 },
  { field: "assetKind", headerName: "자산유형", width: 130 },
  { field: "sourceSystem", headerName: "원천시스템", width: 130 },
  { field: "ownerDepartment", headerName: "담당부서", width: 150 },
  { field: "securityLevel", headerName: "보안등급", width: 110 },
  {
    field: "updatedAt",
    headerName: "최종수정일",
    width: 170,
    valueFormatter: (params) => formatDate(params.value),
  },
];
`;
}

function apiTemplate(rowTypeName, filterTypeName, apiPath) {
  return `import { http } from "@/shared/api/http";
import type { ListRequest, ListResponse } from "@/shared/types/list";
import type { ${rowTypeName}, ${filterTypeName} } from "./types";

export function fetchList(
  request: ListRequest<${filterTypeName}>,
): Promise<ListResponse<${rowTypeName}>> {
  return http.post<ListResponse<${rowTypeName}>>(${JSON.stringify(apiPath)}, request);
}
`;
}

function searchGridPageTemplate({ rowTypeName, filterTypeName, title, description, authCode }) {
  const safeTitle = escapeVueAttribute(title);
  const safeDescription = escapeVueAttribute(description);
  const safeAuthCode = escapeVueAttribute(authCode);

  return `<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElInput, ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchList } from "../api";
import { columns as gridColumns } from "../columns";
import type { ${rowTypeName}, ${filterTypeName} } from "../types";

const rows = ref<${rowTypeName}[]>([]);
const loading = ref(false);
const totalCount = ref(0);

const request = reactive<ListRequest<${filterTypeName}>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    keyword: "",
  },
});

const columns = computed(() => gridColumns);

async function load() {
  loading.value = true;
  try {
    const response = await fetchList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.keyword = "";
  request.pageNo = 1;
  request.sort = [];
  void load();
}

function onPageChange(pageNo: number) {
  request.pageNo = pageNo;
  void load();
}

function onPageSizeChange(pageSize: number) {
  request.pageSize = pageSize;
  request.pageNo = 1;
  void load();
}

function onSortChange(sort: ListSort[]) {
  request.sort = sort;
  request.pageNo = 1;
  void load();
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="${safeTitle}" description="${safeDescription}">
    <template #toolbar>
      <SearchPanel>
        <ElInput
          v-model="request.filters.keyword"
          :prefix-icon="Search"
          clearable
          placeholder="검색어"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />

        <template #actions>
          <AuthButton auth="${safeAuthCode}" @click="reset">초기화</AuthButton>
          <AuthButton auth="${safeAuthCode}" type="primary" @click="search">조회</AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
        </template>
      </SearchPanel>
    </template>

    <BaseGrid
      :rows="rows"
      :columns="columns"
      :loading="loading"
      :total-count="totalCount"
      :page-no="request.pageNo"
      :page-size="request.pageSize"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />
  </GridPageLayout>
</template>
`;
}

function integratedMetaPageTemplate({ rowTypeName, filterTypeName, title, description, authCode }) {
  const safeTitle = escapeVueAttribute(title);
  const safeDescription = escapeVueAttribute(description);
  const safeAuthCode = escapeVueAttribute(authCode);

  return `<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import type { ColDef } from "ag-grid-community";
import { ElTabPane, ElTabs } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { PortalTextInput } from "@/shared/components/tags";
import { handleApiError } from "@/shared/api/error-handler";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchList } from "../api";
import { columns as gridColumns } from "../columns";
import type { ${rowTypeName}, ${filterTypeName}, MetaSearchType, MetaType } from "../types";

type MetaTypeOption = {
  label: string;
  value: MetaType;
};

type SearchTypeOption = {
  label: string;
  value: MetaSearchType;
};

const metaTypeOptions: MetaTypeOption[] = [
  { label: "정형메타", value: "STRUCTURED" },
  { label: "파일메타", value: "FILE" },
  { label: "반정형메타", value: "SEMI_STRUCTURED" },
];

const searchTypeOptions: SearchTypeOption[] = [
  { label: "업무", value: "BUSINESS" },
  { label: "기술", value: "TECHNICAL" },
  { label: "파일", value: "FILE" },
  { label: "담당", value: "OWNER" },
  { label: "보안", value: "SECURITY" },
  { label: "저장", value: "STORAGE" },
];

const activeMetaType = ref<MetaType>("STRUCTURED");
const selectedSearchTypes = ref<MetaSearchType[]>(searchTypeOptions.map((option) => option.value));
const rows = ref<${rowTypeName}[]>([]);
const loading = ref(false);
const totalCount = ref(0);

const request = reactive<ListRequest<${filterTypeName}>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    metaType: activeMetaType.value,
    searchTypes: [...selectedSearchTypes.value],
    keyword: "",
  },
});

const columns = computed<ColDef<${rowTypeName}>[]>(() => gridColumns);
const isAllSearchTypesSelected = computed(() => selectedSearchTypes.value.length === searchTypeOptions.length);
const activeMetaTypeLabel = computed(() => metaTypeOptions.find((option) => option.value === activeMetaType.value)?.label ?? "");

async function load() {
  loading.value = true;
  try {
    syncFilters();
    const response = await fetchList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;
  } catch (error) {
    handleApiError(error, "메타 목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function syncFilters() {
  request.filters.metaType = activeMetaType.value;
  request.filters.searchTypes = [...selectedSearchTypes.value];
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.keyword = "";
  request.pageNo = 1;
  request.sort = [];
  selectAllSearchTypes();
  void load();
}

function onMetaTypeChange() {
  request.pageNo = 1;
  void load();
}

function onPageChange(pageNo: number) {
  request.pageNo = pageNo;
  void load();
}

function onPageSizeChange(pageSize: number) {
  request.pageSize = pageSize;
  request.pageNo = 1;
  void load();
}

function onSortChange(sort: ListSort[]) {
  request.sort = sort;
  request.pageNo = 1;
  void load();
}

function toggleSearchType(value: MetaSearchType) {
  if (selectedSearchTypes.value.includes(value)) {
    selectedSearchTypes.value = selectedSearchTypes.value.filter((item) => item !== value);
    return;
  }

  selectedSearchTypes.value = [...selectedSearchTypes.value, value];
}

function selectAllSearchTypes() {
  selectedSearchTypes.value = searchTypeOptions.map((option) => option.value);
}

function clearSearchTypes() {
  selectedSearchTypes.value = [];
}

function isSelectedSearchType(value: MetaSearchType) {
  return selectedSearchTypes.value.includes(value);
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="${safeTitle}" description="${safeDescription}">
    <template #toolbar>
      <ElTabs v-model="activeMetaType" class="meta-tabs" @tab-change="onMetaTypeChange">
        <ElTabPane
          v-for="option in metaTypeOptions"
          :key="option.value"
          :label="option.label"
          :name="option.value"
        />
      </ElTabs>

      <SearchPanel>
        <div class="meta-search-block">
          <span class="portal-field-label">검색구분</span>
          <div class="meta-search-buttons">
            <PortalButton
              v-for="option in searchTypeOptions"
              :key="option.value"
              :variant="isSelectedSearchType(option.value) ? 'primary' : 'secondary'"
              @click="toggleSearchType(option.value)"
            >
              {{ option.label }}
            </PortalButton>
            <PortalButton :variant="isAllSearchTypesSelected ? 'primary' : 'secondary'" @click="selectAllSearchTypes">
              전체
            </PortalButton>
            <PortalButton variant="ghost" @click="clearSearchTypes">해제</PortalButton>
          </div>
        </div>

        <PortalTextInput
          v-model="request.filters.keyword"
          clearable
          placeholder="키워드 검색"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />

        <template #actions>
          <AuthButton auth="${safeAuthCode}" @click="reset">초기화</AuthButton>
          <AuthButton auth="${safeAuthCode}" type="primary" :icon="Search" @click="search">조회</AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
          <span class="ml-2 text-slate-400">{{ activeMetaTypeLabel }}</span>
          <span v-if="selectedSearchTypes.length === 0" class="ml-2 text-red-500">검색구분 미선택</span>
        </template>
      </SearchPanel>
    </template>

    <BaseGrid
      :rows="rows"
      :columns="columns"
      :loading="loading"
      :total-count="totalCount"
      :page-no="request.pageNo"
      :page-size="request.pageSize"
      height-class="h-[520px]"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />
  </GridPageLayout>
</template>

<style scoped>
.meta-tabs {
  margin-bottom: 0.75rem;
}

.meta-search-block {
  display: flex;
  min-width: min(100%, 520px);
  flex-direction: column;
  gap: 6px;
}

.meta-search-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
</style>
`;
}

function escapeVueAttribute(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}
