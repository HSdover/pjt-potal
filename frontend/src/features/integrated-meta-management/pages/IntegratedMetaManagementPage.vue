<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import type { ColDef } from "ag-grid-community";
import { AgGridVue } from "ag-grid-vue3";
import { ElDialog, ElTabPane, ElTabs } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import PortalButton from "@/shared/components/tags/PortalButton.vue";
import { PortalTextInput } from "@/shared/components/tags";
import { handleApiError } from "@/shared/api/error-handler";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchIntegratedMetaDetail, fetchIntegratedMetaList } from "../api";
import type {
  IntegratedMetaDetail,
  IntegratedMetaItem,
  IntegratedMetaSearchFilter,
  MetaSearchType,
  MetaType,
  StructuredColumnMeta,
} from "../types";

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
const rows = ref<IntegratedMetaItem[]>([]);
const totalCount = ref(0);
const loading = ref(false);
const detailLoading = ref(false);
const detailDialogVisible = ref(false);
const detail = ref<IntegratedMetaDetail | null>(null);
const selectedSearchTypes = ref<MetaSearchType[]>(searchTypeOptions.map((option) => option.value));

const request = reactive<ListRequest<IntegratedMetaSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    metaType: activeMetaType.value,
    searchTypes: [...selectedSearchTypes.value],
    keyword: "",
  },
});

const columns = computed<ColDef<IntegratedMetaItem>[]>(() => [
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
]);
const structuredColumnDefs: ColDef<StructuredColumnMeta>[] = [
  { field: "ordinal", headerName: "순번", width: 80 },
  { field: "columnName", headerName: "컬럼명", minWidth: 160, flex: 1 },
  { field: "dataType", headerName: "데이터타입", width: 140 },
  { field: "nullable", headerName: "Null", width: 90 },
  { field: "keyType", headerName: "Key", width: 90 },
  { field: "securityLevel", headerName: "보안등급", width: 110 },
  { field: "description", headerName: "설명", minWidth: 180, flex: 1 },
];
const detailTitle = computed(() => detail.value ? `${detail.value.metaName} 상세` : "메타 상세");
const basicSection = computed(() => detail.value?.sections[0] ?? null);
const isAllSearchTypesSelected = computed(() => selectedSearchTypes.value.length === searchTypeOptions.length);

async function load() {
  loading.value = true;
  try {
    syncFilters();
    const response = await fetchIntegratedMetaList(request);
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
  request.sort = [];
  request.pageNo = 1;
  selectAllSearchTypes();
  void load();
}

function onMetaTypeChange() {
  request.pageNo = 1;
  detail.value = null;
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

async function openDetail(row: IntegratedMetaItem) {
  detailDialogVisible.value = true;
  detailLoading.value = true;
  detail.value = null;
  try {
    detail.value = await fetchIntegratedMetaDetail(row.metaId);
  } catch (error) {
    detailDialogVisible.value = false;
    handleApiError(error, "메타 상세 조회에 실패했습니다.");
  } finally {
    detailLoading.value = false;
  }
}

function isSelectedSearchType(value: MetaSearchType) {
  return selectedSearchTypes.value.includes(value);
}

function formatDate(value?: string) {
  if (!value) {
    return "-";
  }
  return value.replace("T", " ").slice(0, 16);
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="통합메타관리" description="정형, 파일, 반정형 메타를 통합 조회하고 상세 정보를 확인합니다.">
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
          <AuthButton auth="META_VIEW" @click="reset">초기화</AuthButton>
          <AuthButton auth="META_VIEW" type="primary" :icon="Search" @click="search">조회</AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
          <span class="ml-2 text-slate-400">
            {{ metaTypeOptions.find((option) => option.value === activeMetaType)?.label }}
          </span>
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
      @row-click="openDetail"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />

    <ElDialog v-model="detailDialogVisible" :title="detailTitle" width="980px">
      <div v-loading="detailLoading">
        <template v-if="detail">
          <ElTabs v-if="detail.metaType === 'STRUCTURED'">
            <ElTabPane label="기본정보" name="basic">
              <div v-if="basicSection" class="meta-key-value-grid">
                <label v-for="field in basicSection.fields" :key="field.key" class="meta-key-value-field">
                  <span>{{ field.key }}</span>
                  <input class="portal-control" :value="field.value" disabled />
                </label>
              </div>
            </ElTabPane>
            <ElTabPane label="컬럼정보" name="columns">
              <div class="ag-theme-quartz h-[300px] w-full overflow-hidden rounded-md border border-slate-200">
                <AgGridVue
                  class="h-full w-full"
                  :row-data="detail.columns"
                  :column-defs="structuredColumnDefs"
                  :default-col-def="{ sortable: true, filter: true, resizable: true }"
                />
              </div>
            </ElTabPane>
          </ElTabs>

          <ElTabs v-else>
            <ElTabPane
              v-for="section in detail.sections"
              :key="section.sectionId"
              :label="section.title"
              :name="section.sectionId"
            >
              <div class="meta-key-value-grid">
                <label v-for="field in section.fields" :key="field.key" class="meta-key-value-field">
                  <span>{{ field.key }}</span>
                  <input class="portal-control" :value="field.value" disabled />
                </label>
              </div>
            </ElTabPane>
          </ElTabs>
        </template>
        <div v-else class="py-12 text-center text-sm text-slate-400">메타 상세를 불러오는 중입니다.</div>
      </div>

      <template #footer>
        <AuthButton auth="META_VIEW" @click="detailDialogVisible = false">닫기</AuthButton>
      </template>
    </ElDialog>
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

.meta-key-value-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.meta-key-value-field {
  display: flex;
  min-width: 0;
  flex-direction: column;
  gap: 6px;
}

.meta-key-value-field span {
  color: #334155;
  font-size: 12px;
  font-weight: 800;
}

@media (max-width: 768px) {
  .meta-key-value-grid {
    grid-template-columns: 1fr;
  }
}
</style>
