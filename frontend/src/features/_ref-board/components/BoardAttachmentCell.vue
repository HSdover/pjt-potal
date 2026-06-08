<script setup lang="ts">
import { computed } from "vue";
import type { ICellRendererParams } from "ag-grid-community";
import { Paperclip } from "@element-plus/icons-vue";
import type { RefBoardItem } from "../types";

const props = defineProps<{
  params: ICellRendererParams<RefBoardItem, number>;
}>();

const attachment = computed(() => props.params.data?.attachment ?? null);
</script>

<template>
  <span
    v-if="attachment"
    class="inline-flex h-4 w-4 items-center justify-center align-middle text-slate-500"
    :title="attachment.fileName"
    aria-label="첨부파일 있음"
  >
    <Paperclip class="h-3 w-3" />
  </span>
  <span v-else class="inline-flex h-4 w-4 align-middle" aria-label="첨부파일 없음" />
</template>
