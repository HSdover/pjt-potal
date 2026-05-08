<script setup lang="ts">
import { onMounted, ref } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import type { RowClickedEvent } from "ag-grid-community";
import { ElButton, ElCard, ElDatePicker, ElInput, ElOption, ElSelect, ElTag } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import {
  columnDefs,
  defaultColDef,
  fetchMetadata,
  toggleSelectedMetadata,
  type MetadataCatalogItem,
} from "./MetadataView";

// 화면 상태 연결 영역
// 기준: 화면에서만 사용하는 데이터는 store 없이 이 .vue의 ref로 보관한다.
const metadata = ref<MetadataCatalogItem[]>([]);
const loading = ref(false);
const dateRange = ref<[Date, Date] | null>(null);
const datasetType = ref("");
const searchKeyword = ref("");
const searchText = ref("");

/** 행 클릭으로 선택된 메타데이터 항목. 같은 행 재클릭 시 null로 초기화된다. */
const selected = ref<MetadataCatalogItem | null>(null);

onMounted(async () => {
  loading.value = true;
  try {
    metadata.value = await fetchMetadata();
  } finally {
    loading.value = false;
  }
});

function onRowClick(event: RowClickedEvent<MetadataCatalogItem>) {
  selected.value = toggleSelectedMetadata(selected.value, event);
}

function search() {
  searchText.value = [datasetType.value, searchKeyword.value].filter(Boolean).join(" ");
}
</script>

<template>
  <!--
    GridPageLayout 사용 예 — 메타데이터 조회 화면
    · toolbar 슬롯: 미사용 (AG Grid 내장 필터로 대체)
    · default 슬롯: 메타데이터 목록 그리드
    · detail  슬롯: 행 클릭 시 컬럼 상세 카드
  -->
  <GridPageLayout
    title="메타데이터 조회"
    description="등록된 데이터셋의 메타데이터를 조회합니다. 행을 클릭하면 컬럼 상세를 확인할 수 있습니다."
    v-loading="loading"
  >

    <template #toolbar>
      <div class="flex flex-col gap-3 rounded-md border border-slate-200 bg-white p-3 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex flex-col gap-2 md:flex-row md:flex-wrap md:items-center">
          <ElDatePicker
            v-model="dateRange"
            type="daterange"
            range-separator="~"
            start-placeholder="시작일"
            end-placeholder="종료일"
            class="w-full md:!w-[260px]"
          />
          <ElSelect v-model="datasetType" clearable placeholder="데이터 유형" class="w-full md:!w-40">
            <ElOption label="TABLE" value="TABLE" />
            <ElOption label="FILE" value="FILE" />
            <ElOption label="SCREEN" value="SCREEN" />
          </ElSelect>
          <ElInput
            v-model="searchKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="검색어"
            class="w-full md:!w-80"
            @keyup.enter="search"
          />
          <span class="text-xs font-semibold text-slate-500">전체 {{ metadata.length.toLocaleString() }}건</span>
        </div>
        <ElButton type="primary" :icon="Search" @click="search">조회</ElButton>
      </div>
    </template>

    <!-- ③ 그리드 본체: 메타데이터 목록 -->
    <div class="ag-theme-quartz h-[420px] w-full">
      <AgGridVue
        class="h-full w-full"
        :row-data="metadata"
        :column-defs="columnDefs"
        :default-col-def="defaultColDef"
        :quick-filter-text="searchText"
        row-selection="single"
        animate-rows
        @row-clicked="onRowClick"
      />
    </div>

    <!-- ④ 상세 패널: 선택된 행의 컬럼 목록을 태그로 표시 -->
    <template #detail>
      <transition name="el-fade-in-linear">
        <ElCard v-if="selected" shadow="never">
          <template #header>
            <span class="font-semibold">{{ selected.datasetName }}</span>
            <ElTag type="info" size="small" class="ml-2">컬럼 상세</ElTag>
          </template>
          <!-- columnsSummary를 쉼표 구분으로 파싱해 개별 태그로 표시 -->
          <div class="flex flex-wrap gap-2">
            <ElTag
              v-for="col in selected.columnsSummary?.split(',').map((s) => s.trim()).filter(Boolean)"
              :key="col"
              type="primary"
              effect="plain"
              size="small"
            >
              {{ col }}
            </ElTag>
          </div>
          <p class="mt-3 text-sm text-slate-500">{{ selected.description }}</p>
        </ElCard>
      </transition>
    </template>

  </GridPageLayout>
</template>
