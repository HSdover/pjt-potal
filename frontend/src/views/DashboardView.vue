<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElProgress, ElTag } from "element-plus";
import LineageFlow from "@/components/LineageFlow.vue";
import {
  fetchDashboardData,
  formatNumber,
  getDatasetTypeCounts,
  type DashboardData,
} from "./DashboardView";

const data = ref<DashboardData>({
  metadata: [],
  lineage: [],
  sourceSample: [],
});
const loading = ref(false);
const loadFailed = ref(false);

const totalRows = computed(() =>
  data.value.metadata.reduce((sum, item) => sum + item.rowCount, 0),
);

const datasetTypeCounts = computed(() => getDatasetTypeCounts(data.value.metadata));

const workItems = computed(() => [
  {
    label: "메타데이터 적재",
    value: data.value.metadata.length,
    total: 6,
    status: "진행중",
    color: "#0f766e",
  },
  {
    label: "원천 샘플 검증",
    value: data.value.sourceSample.length,
    total: 20,
    status: "완료",
    color: "#2563eb",
  },
  {
    label: "리니지 연결",
    value: data.value.lineage.length,
    total: 5,
    status: "진행중",
    color: "#7c3aed",
  },
]);

const lineageSteps = computed(() =>
  [...data.value.lineage].sort((a, b) => (a.sortOrder ?? a.flowId) - (b.sortOrder ?? b.flowId)),
);

const healthStatus = computed(() => loadFailed.value ? "오류" : "정상");
const healthStatusType = computed<"success" | "danger">(() => loadFailed.value ? "danger" : "success");

onMounted(async () => {
  loading.value = true;
  loadFailed.value = false;
  try {
    data.value = await fetchDashboardData();
  } catch (error) {
    loadFailed.value = true;
    ElMessage.error(error instanceof Error ? error.message : "대시보드 데이터 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
});
</script>

<template>
  <main class="mx-auto grid max-w-screen-2xl grid-rows-[auto_minmax(420px,1fr)] gap-4 px-6 py-6" v-loading="loading">
    <section class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-4 flex items-center justify-between">
          <div>
            <h2 class="text-base font-bold text-slate-900">작업 현황</h2>
            <p class="mt-1 text-xs text-slate-500">카탈로그 샘플 기준 진행 상태</p>
          </div>
          <ElTag :type="healthStatusType" effect="plain">{{ healthStatus }}</ElTag>
        </div>

        <div class="space-y-4">
          <div v-for="item in workItems" :key="item.label">
            <div class="mb-2 flex items-center justify-between text-sm">
              <span class="font-semibold text-slate-700">{{ item.label }}</span>
              <span class="text-slate-500">{{ item.value }} / {{ item.total }} · {{ item.status }}</span>
            </div>
            <ElProgress
              :percentage="Math.min(100, Math.round((item.value / item.total) * 100))"
              :color="item.color"
              :stroke-width="10"
            />
          </div>
        </div>
      </div>

      <div class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
        <div class="mb-4">
          <h2 class="text-base font-bold text-slate-900">데이터 현황</h2>
          <p class="mt-1 text-xs text-slate-500">현재 등록된 샘플 데이터 요약</p>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div class="rounded-md bg-slate-50 p-4">
            <div class="text-xs font-semibold text-slate-500">데이터셋</div>
            <div class="mt-2 text-2xl font-bold text-slate-900">{{ formatNumber(data.metadata.length) }}</div>
          </div>
          <div class="rounded-md bg-slate-50 p-4">
            <div class="text-xs font-semibold text-slate-500">전체 행 수</div>
            <div class="mt-2 text-2xl font-bold text-slate-900">{{ formatNumber(totalRows) }}</div>
          </div>
          <div class="rounded-md bg-slate-50 p-4">
            <div class="text-xs font-semibold text-slate-500">리니지 단계</div>
            <div class="mt-2 text-2xl font-bold text-slate-900">{{ formatNumber(data.lineage.length) }}</div>
          </div>
        </div>

        <div class="mt-4 flex flex-wrap gap-2">
          <ElTag v-for="(count, type) in datasetTypeCounts" :key="type" effect="plain">
            {{ type }} {{ count }}
          </ElTag>
        </div>
      </div>
    </section>

    <section class="rounded-lg border border-slate-200 bg-white p-5 shadow-sm">
      <div class="mb-4 flex items-center justify-between">
        <div>
          <h2 class="text-base font-bold text-slate-900">데이터 리니지</h2>
          <p class="mt-1 text-xs text-slate-500">원천부터 화면까지 이어지는 처리 흐름</p>
        </div>
        <ElTag effect="plain">{{ data.lineage.length }} steps</ElTag>
      </div>
      <LineageFlow :rows="data.lineage" height-class="h-[560px] xl:h-[620px]" layout="expanded" />

      <div class="mt-4 grid gap-3 md:grid-cols-2 xl:grid-cols-4">
        <div
          v-for="step in lineageSteps"
          :key="step.flowId"
          class="rounded-md border border-slate-200 bg-slate-50 p-3"
        >
          <div class="mb-2 flex items-center justify-between gap-2">
            <span class="text-xs font-bold text-slate-500">STEP {{ step.sortOrder ?? step.flowId }}</span>
            <ElTag size="small" effect="plain">{{ step.transformType }}</ElTag>
          </div>
          <div class="text-sm font-bold text-slate-900">{{ step.processName }}</div>
          <div class="mt-2 text-xs leading-5 text-slate-600">
            <span class="font-semibold text-slate-700">{{ step.sourceName }}</span>
            <span class="mx-1 text-slate-400">→</span>
            <span class="font-semibold text-slate-700">{{ step.targetName }}</span>
          </div>
        </div>
      </div>
    </section>
  </main>
</template>
