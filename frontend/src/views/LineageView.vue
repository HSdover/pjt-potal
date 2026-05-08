<script setup lang="ts">
import { onMounted, ref } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import { ElButton, ElCard, ElDatePicker, ElInput, ElOption, ElSelect } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import LineageFlow from "@/components/LineageFlow.vue";
import {
  defaultColDef,
  fetchLineage,
  lineageColumnDefs,
  type LineageFlowItem,
} from "./LineageView";

// 화면 상태 연결 영역
// 기준: 리니지 데이터는 현재 화면에서만 사용하므로 화면 내부 ref로 관리한다.
const lineage = ref<LineageFlowItem[]>([]);
const loading = ref(false);
const dateRange = ref<[Date, Date] | null>(null);
const transformType = ref("");
const searchKeyword = ref("");
const searchText = ref("");

onMounted(async () => {
  loading.value = true;
  try {
    lineage.value = await fetchLineage();
  } finally {
    loading.value = false;
  }
});

function search() {
  searchText.value = [transformType.value, searchKeyword.value].filter(Boolean).join(" ");
}
</script>

<template>
  <main class="mx-auto max-w-screen-2xl px-6 py-6" v-loading="loading">
    <h2 class="mb-4 text-xl font-bold text-slate-800">데이터 리니지</h2>

    <section class="grid gap-4 xl:grid-cols-[1.4fr_0.9fr]">
      <ElCard shadow="never">
        <template #header>흐름 그래프</template>
        <LineageFlow :rows="lineage" />
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="flex flex-col gap-3">
            <span>처리 단계</span>
            <div class="flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between">
              <div class="flex flex-col gap-2 md:flex-row md:flex-wrap md:items-center">
                <ElDatePicker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="~"
                  start-placeholder="시작일"
                  end-placeholder="종료일"
                  class="w-full md:!w-[260px]"
                />
                <ElSelect v-model="transformType" clearable placeholder="처리 유형" class="w-full md:!w-40">
                  <ElOption label="INGEST" value="INGEST" />
                  <ElOption label="TRANSFORM" value="TRANSFORM" />
                  <ElOption label="SERVE" value="SERVE" />
                </ElSelect>
                <ElInput
                  v-model="searchKeyword"
                  :prefix-icon="Search"
                  clearable
                  placeholder="검색어"
                  class="w-full md:!w-64"
                  @keyup.enter="search"
                />
              </div>
              <ElButton type="primary" :icon="Search" @click="search">조회</ElButton>
            </div>
          </div>
        </template>
        <div class="ag-theme-quartz h-96 w-full">
          <AgGridVue
            class="h-full w-full"
            :row-data="lineage"
            :column-defs="lineageColumnDefs"
            :default-col-def="defaultColDef"
            :quick-filter-text="searchText"
            animate-rows
          />
        </div>
      </ElCard>
    </section>
  </main>
</template>
