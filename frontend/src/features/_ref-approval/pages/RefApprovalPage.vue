<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  ElCard,
  ElDescriptions,
  ElDescriptionsItem,
  ElInput,
  ElMessage,
  ElTag,
  ElTimeline,
  ElTimelineItem,
} from "element-plus";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import { approve, fetchApproval, fetchApprovalList, reject } from "../api";
import type { RefApprovalItem } from "../types";

// [참고 화면] approval-workflow 템플릿 견본이다. 상태 enum + 액션 + 이력 타임라인.
const items = ref<RefApprovalItem[]>([]);
const selected = ref<RefApprovalItem | null>(null);
const comment = ref("");
const loading = ref(false);
const acting = ref(false);

const statusTagType = computed(() => {
  switch (selected.value?.status) {
    case "APPROVED":
      return "success";
    case "REJECTED":
      return "danger";
    case "REVIEW":
      return "warning";
    case "SUBMITTED":
      return "info";
    default:
      return "info";
  }
});

const canDecide = computed(() => {
  if (!selected.value) {
    return false;
  }
  return ["SUBMITTED", "REVIEW"].includes(selected.value.status);
});

async function loadList() {
  loading.value = true;
  try {
    items.value = await fetchApprovalList();
    if (items.value.length > 0) {
      await select(items.value[0].id);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "결재 목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

async function select(id: number) {
  try {
    selected.value = await fetchApproval(id);
    comment.value = "";
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "결재 상세 조회에 실패했습니다.");
  }
}

async function doApprove() {
  if (!selected.value) {
    return;
  }
  acting.value = true;
  try {
    selected.value = await approve(selected.value.id, { comment: comment.value || undefined });
    ElMessage.success("승인되었습니다.");
    comment.value = "";
    await loadList();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "승인 처리에 실패했습니다.");
  } finally {
    acting.value = false;
  }
}

async function doReject() {
  if (!selected.value) {
    return;
  }
  if (!comment.value.trim()) {
    ElMessage.warning("반려 사유를 입력하세요.");
    return;
  }
  acting.value = true;
  try {
    selected.value = await reject(selected.value.id, { comment: comment.value });
    ElMessage.success("반려되었습니다.");
    comment.value = "";
    await loadList();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "반려 처리에 실패했습니다.");
  } finally {
    acting.value = false;
  }
}

function formatDate(value?: string | null) {
  if (!value) {
    return "-";
  }
  return value.replace("T", " ").slice(0, 16);
}

function timelineColor(status: string) {
  switch (status) {
    case "APPROVED":
      return "#0f766e";
    case "REJECTED":
      return "#dc2626";
    case "REVIEW":
      return "#d97706";
    default:
      return "#2563eb";
  }
}

onMounted(loadList);
</script>

<template>
  <main class="portal-page" v-loading="loading">
    <header class="portal-page-header">
      <h2 class="portal-page-title">참고: 승인 워크플로우 (approval-workflow)</h2>
      <p class="portal-page-description">신청 정보, 현재 상태, 승인/반려 액션, 이력 타임라인을 함께 보여주는 견본.</p>
    </header>

    <section class="grid gap-4 xl:grid-cols-[280px_1fr]">
      <ElCard class="portal-card" shadow="never">
        <template #header>결재 대기 목록</template>
        <ul class="space-y-1">
          <li
            v-for="item in items"
            :key="item.id"
            class="cursor-pointer rounded-md px-3 py-2 text-sm"
            :class="selected?.id === item.id ? 'bg-slate-100 font-semibold text-slate-900' : 'text-slate-600 hover:bg-slate-50'"
            @click="select(item.id)"
          >
            <div>{{ item.title }}</div>
            <div class="mt-1 text-xs text-slate-400">{{ item.requestType }} · {{ item.status }}</div>
          </li>
        </ul>
      </ElCard>

      <div v-if="selected" class="space-y-4">
        <ElCard class="portal-card" shadow="never">
          <template #header>
            <div class="flex items-center justify-between">
              <span>신청 정보</span>
              <ElTag :type="statusTagType" effect="plain">{{ selected.status }}</ElTag>
            </div>
          </template>

          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="제목" :span="2">{{ selected.title }}</ElDescriptionsItem>
            <ElDescriptionsItem label="신청자">{{ selected.requesterName }} ({{ selected.requesterTeam }})</ElDescriptionsItem>
            <ElDescriptionsItem label="신청 유형">{{ selected.requestType }}</ElDescriptionsItem>
            <ElDescriptionsItem label="신청 일시">{{ formatDate(selected.submittedAt) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="결정 일시">{{ formatDate(selected.decidedAt) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="요약" :span="2">{{ selected.summary }}</ElDescriptionsItem>
          </ElDescriptions>
        </ElCard>

        <ElCard class="portal-card" shadow="never">
          <template #header>의사 결정</template>
          <div class="space-y-3">
            <ElInput
              v-model="comment"
              type="textarea"
              :rows="3"
              :disabled="!canDecide || acting"
              placeholder="검토/반려 사유를 입력하세요 (반려 시 필수)"
              maxlength="500"
              show-word-limit
            />
            <div class="flex justify-end gap-2">
              <AuthButton type="danger" :disabled="!canDecide || acting" @click="doReject">반려</AuthButton>
              <AuthButton type="primary" :disabled="!canDecide || acting" @click="doApprove">승인</AuthButton>
            </div>
            <p v-if="!canDecide" class="text-xs text-slate-400">
              현재 상태({{ selected.status }})에서는 추가 결정이 불가합니다.
            </p>
          </div>
        </ElCard>

        <ElCard class="portal-card" shadow="never">
          <template #header>승인 이력</template>
          <ElTimeline>
            <ElTimelineItem
              v-for="(entry, index) in selected.history"
              :key="index"
              :timestamp="formatDate(entry.occurredAt)"
              :color="timelineColor(entry.toStatus)"
            >
              <div class="text-sm font-semibold text-slate-900">
                {{ entry.fromStatus ?? '신규' }} → {{ entry.toStatus }}
              </div>
              <div class="mt-1 text-xs text-slate-500">{{ entry.actorName }}</div>
              <div v-if="entry.comment" class="mt-1 text-sm text-slate-700">{{ entry.comment }}</div>
            </ElTimelineItem>
          </ElTimeline>
        </ElCard>
      </div>
      <ElCard v-else class="portal-card" shadow="never">
        <div class="py-10 text-center text-sm text-slate-400">좌측 목록에서 결재 건을 선택하세요.</div>
      </ElCard>
    </section>
  </main>
</template>
