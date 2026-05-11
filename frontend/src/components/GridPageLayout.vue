<script setup lang="ts">
import { ElCard } from "element-plus";

// [11. 레이아웃 및 화면 스타일 표준] 목록형 업무 화면의 공통 본문 레이아웃이다.
withDefaults(defineProps<{
  title: string;
  description?: string;
}>(), {
  description: "",
});
</script>

<template>
  <main class="mx-auto max-w-screen-2xl px-6 py-6">
    <header class="mb-4">
      <h2 class="text-xl font-bold text-slate-800">{{ title }}</h2>
      <p v-if="description" class="mt-1 text-sm text-slate-500">{{ description }}</p>
    </header>

    <!-- [10. 검색 영역 표준화] 검색/버튼 영역은 toolbar 슬롯으로 통일한다. -->
    <div v-if="$slots.toolbar" class="mb-3">
      <slot name="toolbar" />
    </div>

    <!-- [9. AG Grid 표준 래퍼] 본문에는 BaseGrid 같은 표준 그리드 컴포넌트를 배치한다. -->
    <ElCard shadow="never">
      <slot />
    </ElCard>

    <!-- [5.2 SearchGridDetailPage] 행 선택 상세 영역은 detail 슬롯으로 표준화한다. -->
    <div v-if="$slots.detail" class="mt-4">
      <slot name="detail" />
    </div>
  </main>
</template>
