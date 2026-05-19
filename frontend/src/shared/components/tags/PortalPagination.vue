<script setup lang="ts">
import { computed } from "vue";

const currentPage = defineModel<number>("currentPage", { default: 1 });
const pageSize = defineModel<number>("pageSize", { default: 10 });

const props = withDefaults(defineProps<{
  total: number;
  pageSizeOptions?: number[];
}>(), {
  pageSizeOptions: () => [10, 20, 50],
});

const pageCount = computed(() => Math.max(1, Math.ceil(props.total / pageSize.value)));
const visiblePages = computed(() => {
  const pages = new Set<number>([1, pageCount.value, currentPage.value]);
  for (let page = currentPage.value - 1; page <= currentPage.value + 1; page += 1) {
    if (page >= 1 && page <= pageCount.value) {
      pages.add(page);
    }
  }
  return Array.from(pages).sort((left, right) => left - right);
});

function move(page: number) {
  currentPage.value = Math.min(Math.max(page, 1), pageCount.value);
}

function changePageSize(event: Event) {
  pageSize.value = Number((event.target as HTMLSelectElement).value);
  currentPage.value = 1;
}
</script>

<template>
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
