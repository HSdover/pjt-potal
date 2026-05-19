<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElCard, ElDescriptions, ElDescriptionsItem, ElMessage, ElTag } from "element-plus";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import { fetchDetail, fetchDetailList } from "../api";
import type { RefDetailItem } from "../types";

// [참고 화면] detail-view 템플릿 견본이다. 좌측 목록에서 선택 시 우측 상세 패널을 갱신한다.
const items = ref<RefDetailItem[]>([]);
const selectedId = ref<number | null>(null);
const selected = ref<RefDetailItem | null>(null);
const loading = ref(false);

const statusTagType = computed(() => (selected.value?.status === "ACTIVE" ? "success" : "info"));

async function loadList() {
  loading.value = true;
  try {
    items.value = await fetchDetailList();
    if (items.value.length > 0) {
      await selectItem(items.value[0].id);
    }
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "참고 상세 목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

async function selectItem(id: number) {
  selectedId.value = id;
  try {
    selected.value = await fetchDetail(id);
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "참고 상세 조회에 실패했습니다.");
  }
}

function formatDate(value?: string) {
  if (!value) {
    return "-";
  }
  return value.replace("T", " ").slice(0, 16);
}

onMounted(loadList);
</script>

<template>
  <main class="portal-page" v-loading="loading">
    <header class="portal-page-header">
      <h2 class="portal-page-title">참고: 상세 조회 (detail-view)</h2>
      <p class="portal-page-description">목록과 상세 패널을 함께 보여주는 화면 템플릿이다.</p>
    </header>

    <section class="grid gap-4 xl:grid-cols-[280px_1fr]">
      <ElCard class="portal-card" shadow="never">
        <template #header>대상 목록</template>
        <ul class="space-y-1">
          <li
            v-for="item in items"
            :key="item.id"
            class="cursor-pointer rounded-md px-3 py-2 text-sm"
            :class="selectedId === item.id ? 'bg-slate-100 font-semibold text-slate-900' : 'text-slate-600 hover:bg-slate-50'"
            @click="selectItem(item.id)"
          >
            <div>{{ item.name }}</div>
            <div class="text-xs text-slate-400">{{ item.code }}</div>
          </li>
        </ul>
      </ElCard>

      <ElCard class="portal-card" shadow="never">
        <template #header>
          <div class="flex items-center justify-between">
            <span>상세 정보</span>
            <div class="flex gap-2">
              <AuthButton type="primary">수정</AuthButton>
              <AuthButton type="danger">삭제</AuthButton>
            </div>
          </div>
        </template>

        <div v-if="selected">
          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="ID">{{ selected.id }}</ElDescriptionsItem>
            <ElDescriptionsItem label="코드">{{ selected.code }}</ElDescriptionsItem>
            <ElDescriptionsItem label="명칭">{{ selected.name }}</ElDescriptionsItem>
            <ElDescriptionsItem label="분류">{{ selected.category }}</ElDescriptionsItem>
            <ElDescriptionsItem label="담당">{{ selected.ownerName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="상태">
              <ElTag :type="statusTagType" effect="plain">{{ selected.status }}</ElTag>
            </ElDescriptionsItem>
            <ElDescriptionsItem label="생성일시" :span="2">{{ formatDate(selected.createdAt) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="설명" :span="2">{{ selected.description }}</ElDescriptionsItem>
          </ElDescriptions>
        </div>
        <div v-else class="py-8 text-center text-sm text-slate-400">좌측 목록에서 항목을 선택하세요.</div>
      </ElCard>
    </section>
  </main>
</template>
