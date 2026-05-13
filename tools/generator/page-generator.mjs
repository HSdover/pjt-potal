import { existsSync, mkdirSync, writeFileSync } from "node:fs";
import path from "node:path";

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
  const description = options.description ?? "표준 검색 그리드 화면입니다.";

  const files = [
    {
      path: path.join(featureRoot, "types.ts"),
      content: typesTemplate(rowTypeName, filterTypeName),
    },
    {
      path: path.join(featureRoot, "columns.ts"),
      content: columnsTemplate(rowTypeName),
    },
    {
      path: path.join(featureRoot, "api.ts"),
      content: apiTemplate(rowTypeName, filterTypeName, apiPath),
    },
    {
      path: path.join(pagesRoot, `${pageComponentName}.vue`),
      content: pageTemplate({
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

  if (pageType !== "search-grid") {
    throw new Error(`Unsupported page type: ${pageType}. Only search-grid is supported.`);
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

function typesTemplate(rowTypeName, filterTypeName) {
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

function columnsTemplate(rowTypeName) {
  return `import type { ColDef } from "ag-grid-community";
import type { ${rowTypeName} } from "./types";

export const columns: ColDef<${rowTypeName}>[] = [
  { field: "id", headerName: "ID", width: 100 },
  { field: "name", headerName: "명칭", minWidth: 180, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 240, flex: 1 },
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

function pageTemplate({ rowTypeName, filterTypeName, title, description, authCode }) {
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

function escapeVueAttribute(value) {
  return value
    .replaceAll("&", "&amp;")
    .replaceAll("\"", "&quot;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}
