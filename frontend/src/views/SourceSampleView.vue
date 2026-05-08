<script setup lang="ts">
import { onMounted, ref } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import { ElButton, ElDatePicker, ElInput, ElOption, ElSelect } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import {
  columnDefs,
  defaultColDef,
  fetchSourceSample,
  type SourceSampleItem,
} from "./SourceSampleView";

// 화면 상태 연결 영역
// 기준: 원천 샘플 데이터는 이 화면에서만 쓰므로 화면 내부 ref로 관리한다.
const sourceSample = ref<SourceSampleItem[]>([]);
const loading = ref(false);
const dateRange = ref<[Date, Date] | null>(null);
const region = ref("");
const searchKeyword = ref("");
const searchText = ref("");

onMounted(async () => {
  loading.value = true;
  try {
    sourceSample.value = await fetchSourceSample();
  } finally {
    loading.value = false;
  }
});

function search() {
  searchText.value = [region.value, searchKeyword.value].filter(Boolean).join(" ");
}
</script>

<template>
  <!--
    GridPageLayout 사용 예 — 원천 샘플 데이터 화면
    · toolbar 슬롯: 미사용
    · default 슬롯: 원천 샘플 그리드
    · detail  슬롯: 미사용
  -->
  <GridPageLayout
    title="원천 샘플 데이터"
    description="심평원 요양기관 개설 현황 원천 CSV에서 추출한 1,000건 샘플 데이터입니다."
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
          <ElSelect v-model="region" clearable placeholder="지역" class="w-full md:!w-40">
            <ElOption label="서울" value="서울" />
            <ElOption label="경기" value="경기" />
            <ElOption label="부산" value="부산" />
            <ElOption label="대구" value="대구" />
          </ElSelect>
          <ElInput
            v-model="searchKeyword"
            :prefix-icon="Search"
            clearable
            placeholder="검색어"
            class="w-full md:!w-80"
            @keyup.enter="search"
          />
          <span class="text-xs font-semibold text-slate-500">전체 {{ sourceSample.length.toLocaleString() }}건</span>
        </div>
        <ElButton type="primary" :icon="Search" @click="search">조회</ElButton>
      </div>
    </template>

    <!-- ③ 그리드 본체: 원천 샘플 목록 -->
    <div class="ag-theme-quartz h-[520px] w-full">
      <AgGridVue
        class="h-full w-full"
        :row-data="sourceSample"
        :column-defs="columnDefs"
        :default-col-def="defaultColDef"
        :quick-filter-text="searchText"
        animate-rows
      />
    </div>

  </GridPageLayout>
</template>
