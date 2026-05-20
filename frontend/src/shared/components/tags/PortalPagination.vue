<script setup lang="ts">
import { computed } from "vue";

// 공통 페이지네이션: 목록 화면의 현재 페이지와 페이지 크기를 각각 v-model로 관리한다.
const currentPage = defineModel<number>("currentPage", { default: 1 });
const pageSize = defineModel<number>("pageSize", { default: 10 });

// total은 서버 목록 응답의 전체 건수이며, pageSizeOptions는 화면별로 조정 가능하다.
const props = withDefaults(defineProps<{
  total: number;
  pageSizeOptions?: number[];
}>(), {
  pageSizeOptions: () => [10, 20, 50],
});

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / pageSize.value)));
// 첫 페이지, 마지막 페이지, 현재 페이지 주변만 노출해 긴 페이지 목록을 단순화한다.
const visiblePages = computed(() => {
  const pages = new Set<number>([1, pageCount.value, currentPage.value]);
  for (let page = currentPage.value - 1; page <= currentPage.value + 1; page += 1) {
    if (page >= 1 && page <= pageCount.value) {
      pages.add(page);
    }
  }
  return Array.from(pages).sort((left, right) => left - right);
});

// 범위 밖 페이지 이동 요청은 1~pageCount 사이로 보정한다.
function move(page: number) {
  currentPage.value = Math.min(Math.max(page, 1), pageCount.value);
}

// 페이지 크기가 바뀌면 기존 페이지 번호가 무효해질 수 있어 1페이지로 되돌린다.
function changePageSize(event: Event) {
  pageSize.value = Number((event.target as HTMLSelectElement).value);
  currentPage.value = 1;
}
</script>

<template>
  <!-- 이전/다음과 처음/끝 이동을 모두 제공해 대량 목록 탐색을 줄인다. -->
  <nav class="portal-pagination" aria-label="목록 페이지">
    <button type="button" :disabled="currentPage === 1" @click="move(1)">«</button>
    <button type="button" :disabled="currentPage === 1" @click="move(currentPage - 1)">‹</button>
    <button
      v-for="page in visiblePages"
      :key="page"
      type="button"
      :class="{ active: page === currentPage }"
      @click="move(page)"
    >
      {{ page }}
    </button>
    <button type="button" :disabled="currentPage === pageCount" @click="move(currentPage + 1)">›</button>
    <button type="button" :disabled="currentPage === pageCount" @click="move(pageCount)">»</button>
    <select :value="pageSize" aria-label="페이지 크기" @change="changePageSize">
      <option v-for="size in pageSizeOptions" :key="size" :value="size">{{ size }}개</option>
    </select>
  </nav>
</template>
