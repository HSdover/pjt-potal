<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElCard, ElMessage, ElTag } from "element-plus";
import { use } from "echarts/core";
import { CanvasRenderer } from "echarts/renderers";
import { BarChart, LineChart, PieChart } from "echarts/charts";
import {
  GridComponent,
  LegendComponent,
  TitleComponent,
  TooltipComponent,
} from "echarts/components";
import VChart from "vue-echarts";
import { fetchDashboard } from "../api";
import type { RefDashboardData } from "../types";

// [참고 화면] dashboard 템플릿 견본이다. KPI 카드 + 라인/바/도넛 차트.
use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
]);

const data = ref<RefDashboardData | null>(null);
const loading = ref(false);

async function load() {
  loading.value = true;
  try {
    data.value = await fetchDashboard();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "대시보드 데이터 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function formatNumber(value: number) {
  return new Intl.NumberFormat("ko-KR").format(value);
}

const lineOption = computed(() => {
  if (!data.value) {
    return {};
  }
  return {
    tooltip: { trigger: "axis" },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: "category",
      data: data.value.timeSeries.labels,
      boundaryGap: false,
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "처리 건수",
        type: "line",
        smooth: true,
        data: data.value.timeSeries.values,
        lineStyle: { color: "#2563eb", width: 2 },
        areaStyle: { color: "rgba(37,99,235,0.12)" },
        symbol: "circle",
        symbolSize: 6,
      },
    ],
  };
});

const barOption = computed(() => {
  if (!data.value) {
    return {};
  }
  return {
    tooltip: { trigger: "axis" },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: "category", data: data.value.categoryBars.labels },
    yAxis: { type: "value" },
    series: [
      {
        name: "도메인별 건수",
        type: "bar",
        data: data.value.categoryBars.values,
        itemStyle: { color: "#0f766e", borderRadius: [4, 4, 0, 0] },
      },
    ],
  };
});

const donutOption = computed(() => {
  if (!data.value) {
    return {};
  }
  return {
    tooltip: { trigger: "item" },
    legend: { bottom: 0 },
    series: [
      {
        name: "상태 분포",
        type: "pie",
        radius: ["45%", "70%"],
        avoidLabelOverlap: true,
        label: { show: false },
        data: data.value.statusDonut.map((slice) => ({
          name: slice.name,
          value: slice.value,
        })),
        color: ["#0f766e", "#2563eb", "#dc2626", "#d97706"],
      },
    ],
  };
});

onMounted(load);
</script>

<template>
  <main class="portal-page" v-loading="loading">
    <header class="portal-page-header">
      <h2 class="portal-page-title">참고: 대시보드 (dashboard)</h2>
      <p class="portal-page-description">KPI 카드와 ECharts 라인/바/도넛 차트를 결합한 견본이다.</p>
    </header>

    <section v-if="data" class="grid gap-4">
      <div class="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <div v-for="kpi in data.kpis" :key="kpi.label" class="portal-kpi-card p-4 pt-5">
          <div class="flex items-center justify-between">
            <span class="text-xs font-semibold text-slate-500">{{ kpi.label }}</span>
            <ElTag size="small" effect="plain">{{ kpi.trend }}</ElTag>
          </div>
          <div class="mt-2 text-2xl font-bold text-slate-900">
            {{ formatNumber(kpi.value) }}<span class="ml-1 text-sm font-semibold text-slate-500">{{ kpi.unit }}</span>
          </div>
        </div>
      </div>

      <div class="grid gap-4 xl:grid-cols-[1.4fr_1fr]">
        <ElCard class="portal-card" shadow="never">
          <template #header>처리 건수 추이 (14일)</template>
          <VChart :option="lineOption" autoresize style="height: 280px" />
        </ElCard>

        <ElCard class="portal-card" shadow="never">
          <template #header>상태 분포</template>
          <VChart :option="donutOption" autoresize style="height: 280px" />
        </ElCard>
      </div>

      <ElCard class="portal-card" shadow="never">
        <template #header>도메인별 처리 건수</template>
        <VChart :option="barOption" autoresize style="height: 280px" />
      </ElCard>
    </section>
  </main>
</template>
