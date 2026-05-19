<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElTag } from "element-plus";
import {
  fetchDashboardData,
  formatNumber,
  type DashboardData,
  type DashboardTone,
} from "./DashboardView";

const data = ref<DashboardData>({
  asOf: "",
  overallStatus: "",
  overallTone: "info",
  metrics: [],
  integrations: [],
  approvalQueues: [],
  failureJobs: [],
  agentUsages: [],
  dataDomains: [],
});
const loading = ref(false);
const loadFailed = ref(false);

const overallStatus = computed(() => loadFailed.value ? "오류" : data.value.overallStatus);
const overallStatusType = computed(() => loadFailed.value ? "danger" : data.value.overallTone);
const normalIntegrationCount = computed(() =>
  data.value.integrations.filter((item) => item.tone === "success").length,
);
const approvalTotal = computed(() =>
  data.value.approvalQueues.reduce((sum, item) => sum + item.count, 0),
);
const failureTotal = computed(() =>
  data.value.failureJobs.reduce((sum, item) => sum + item.failedCount, 0),
);

function toneTextClass(tone: DashboardTone) {
  return {
    success: "text-emerald-700",
    warning: "text-amber-700",
    danger: "text-rose-700",
    info: "text-sky-700",
  }[tone];
}

function toneBarClass(tone: DashboardTone) {
  return {
    success: "bg-emerald-600",
    warning: "bg-amber-500",
    danger: "bg-rose-600",
    info: "bg-sky-600",
  }[tone];
}

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
  <main class="portal-page grid gap-4" v-loading="loading">
    <header class="portal-hero flex flex-wrap items-end justify-between gap-4 p-5 md:p-6">
      <div>
        <p class="text-xs font-bold uppercase tracking-wide text-blue-100">SAMSUNG SECURITIES</p>
        <h1 class="mt-1 text-2xl font-extrabold text-white">AI 데이터 거버넌스 대시보드</h1>
        <p class="mt-2 text-sm text-blue-100">기준시 {{ data.asOf }}</p>
      </div>
      <div class="flex flex-wrap items-center gap-2 text-sm">
        <ElTag :type="overallStatusType" effect="dark">{{ overallStatus }}</ElTag>
        <span class="rounded-full bg-white/10 px-3 py-1 font-semibold text-white">
          연계 정상 {{ normalIntegrationCount }} / {{ data.integrations.length }}
        </span>
      </div>
    </header>

    <section class="grid gap-3 sm:grid-cols-2 lg:grid-cols-3 2xl:grid-cols-6">
      <article
        v-for="metric in data.metrics"
        :key="metric.id"
        class="portal-kpi-card p-4 pt-5"
      >
        <div class="flex items-start justify-between gap-2">
          <div>
            <div class="text-xs font-semibold text-slate-500">{{ metric.label }}</div>
            <div class="mt-2 text-2xl font-bold text-slate-950">
              {{ formatNumber(metric.value) }}<span class="ml-1 text-sm font-semibold text-slate-500">{{ metric.unit }}</span>
            </div>
          </div>
          <ElTag :type="metric.tone" size="small" effect="plain">{{ metric.delta }}</ElTag>
        </div>
        <p class="mt-3 min-h-10 text-xs leading-5 text-slate-500">{{ metric.description }}</p>
      </article>
    </section>

    <section class="grid gap-4 xl:grid-cols-[1.4fr_1fr]">
      <article class="portal-card p-5">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 class="portal-panel-title">외부 연계 상태</h2>
            <p class="portal-panel-subtitle">지연, 실패, backlog 기준</p>
          </div>
          <ElTag effect="plain">실패 {{ failureTotal }}건</ElTag>
        </div>

        <div class="overflow-x-auto">
          <table class="portal-table text-sm">
            <thead>
              <tr>
                <th class="whitespace-nowrap py-2 pr-4">시스템</th>
                <th class="whitespace-nowrap px-4 py-2">용도</th>
                <th class="whitespace-nowrap px-4 py-2">상태</th>
                <th class="whitespace-nowrap px-4 py-2 text-right">응답</th>
                <th class="whitespace-nowrap px-4 py-2 text-right">대기</th>
                <th class="whitespace-nowrap py-2 pl-4 text-right">동기화</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="item in data.integrations" :key="item.systemName">
                <td class="whitespace-nowrap py-3 pr-4 font-semibold text-slate-900">{{ item.systemName }}</td>
                <td class="min-w-48 px-4 py-3 text-slate-600">{{ item.purpose }}</td>
                <td class="whitespace-nowrap px-4 py-3">
                  <ElTag :type="item.tone" size="small" effect="plain">{{ item.status }}</ElTag>
                </td>
                <td class="whitespace-nowrap px-4 py-3 text-right text-slate-600">
                  {{ item.latencyMs > 0 ? `${formatNumber(item.latencyMs)}ms` : "-" }}
                </td>
                <td class="whitespace-nowrap px-4 py-3 text-right font-semibold" :class="toneTextClass(item.tone)">
                  {{ formatNumber(item.backlogCount) }}
                </td>
                <td class="whitespace-nowrap py-3 pl-4 text-right text-slate-500">{{ item.lastSyncedAt }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </article>

      <article class="portal-card p-5">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 class="portal-panel-title">승인/요청 큐</h2>
            <p class="portal-panel-subtitle">처리 대기 {{ approvalTotal }}건</p>
          </div>
          <ElTag type="warning" effect="plain">SLA 관리</ElTag>
        </div>

        <div class="divide-y divide-slate-100">
          <div v-for="queue in data.approvalQueues" :key="queue.label" class="grid grid-cols-[1fr_auto] gap-3 py-4">
            <div>
              <div class="font-semibold text-slate-900">{{ queue.label }}</div>
              <div class="mt-1 text-xs text-slate-500">담당 {{ queue.owner }} · SLA {{ queue.sla }}</div>
            </div>
            <div class="text-right">
              <div class="text-2xl font-bold" :class="toneTextClass(queue.tone)">{{ queue.count }}</div>
              <div class="text-xs text-slate-500">건</div>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="grid gap-4 xl:grid-cols-[1fr_1fr]">
      <article class="portal-card p-5">
        <div class="mb-4 flex flex-wrap items-center justify-between gap-2">
          <div>
            <h2 class="portal-panel-title">실패/재처리</h2>
            <p class="portal-panel-subtitle">외부 REST 연계와 webhook 처리 결과</p>
          </div>
          <ElTag type="danger" effect="plain">DLQ 후보</ElTag>
        </div>

        <div class="divide-y divide-slate-100">
          <div v-for="job in data.failureJobs" :key="job.jobName" class="py-4">
            <div class="flex flex-wrap items-start justify-between gap-3">
              <div>
                <div class="font-semibold text-slate-900">{{ job.jobName }}</div>
                <div class="mt-1 text-xs text-slate-500">{{ job.sourceSystem }} · 마지막 실패 {{ job.lastFailedAt }}</div>
              </div>
              <ElTag :type="job.tone" effect="plain">{{ job.failedCount }}건</ElTag>
            </div>
            <div class="mt-2 text-sm text-slate-600">{{ job.nextAction }}</div>
          </div>
        </div>
      </article>

      <article class="portal-card p-5">
        <div class="mb-4">
          <h2 class="portal-panel-title">Agent 데이터 사용</h2>
          <p class="portal-panel-subtitle">오늘 접근량 기준 상위 Agent</p>
        </div>

        <div class="divide-y divide-slate-100">
          <div v-for="agent in data.agentUsages" :key="agent.agentName" class="grid gap-3 py-4 sm:grid-cols-[1fr_auto]">
            <div>
              <div class="font-semibold text-slate-900">{{ agent.agentName }}</div>
              <div class="mt-1 text-xs text-slate-500">
                매핑 {{ agent.mappedDatasets }}개 · 최근 사용 {{ agent.lastUsedAt }}
              </div>
            </div>
            <div class="flex items-center justify-between gap-3 sm:justify-end">
              <div class="text-right">
                <div class="text-lg font-bold text-slate-950">{{ formatNumber(agent.todayAccessCount) }}</div>
                <div class="text-xs text-slate-500">접근</div>
              </div>
              <ElTag :type="agent.tone" size="small" effect="plain">{{ agent.status }}</ElTag>
            </div>
          </div>
        </div>
      </article>
    </section>

    <section class="portal-card p-5">
      <div class="mb-4">
        <h2 class="portal-panel-title">데이터 도메인 상태</h2>
        <p class="portal-panel-subtitle">권한, 민감도, 비정형 처리 관점의 관리 대상</p>
      </div>

      <div class="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <div v-for="domain in data.dataDomains" :key="domain.label">
          <div class="mb-2 flex items-center justify-between gap-3">
            <span class="text-sm font-semibold text-slate-800">{{ domain.label }}</span>
            <span class="text-sm font-bold" :class="toneTextClass(domain.tone)">{{ formatNumber(domain.count) }}건</span>
          </div>
          <div class="h-2 overflow-hidden rounded-full bg-slate-100">
            <div class="h-full rounded-full" :class="toneBarClass(domain.tone)" :style="{ width: `${domain.ratio}%` }" />
          </div>
          <div class="mt-2 text-xs text-slate-500">관리 비중 {{ domain.ratio }}%</div>
        </div>
      </div>
    </section>
  </main>
</template>
