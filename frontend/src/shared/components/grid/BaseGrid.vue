<script setup lang="ts" generic="TRow extends object">
import { computed } from "vue";
import { AgGridVue } from "ag-grid-vue3";
import type { ColDef, RowClassParams, RowClickedEvent, SortChangedEvent } from "ag-grid-community";
import PortalPagination from "@/shared/components/tags/PortalPagination.vue";
import type { ListSort } from "@/shared/types/list";

const props = withDefaults(defineProps<{
  rows: TRow[];
  columns: ColDef<TRow>[];
  loading?: boolean;
  totalCount?: number;
  pageNo?: number;
  pageSize?: number;
  heightClass?: string;
  rowSelection?: "single" | "multiple";
  pinnedBottomRows?: TRow[];
}>(), {
  loading: false,
  totalCount: 0,
  pageNo: 1,
  pageSize: 20,
  heightClass: "h-[520px]",
  rowSelection: "single",
});

const emit = defineEmits<{
  rowClick: [row: TRow];
  pageChange: [pageNo: number];
  pageSizeChange: [pageSize: number];
  sortChange: [sort: ListSort[]];
}>();

// [9. AG Grid 표준 래퍼] 화면별 중복되는 기본 컬럼 옵션을 공통화한다.
const defaultColDef: ColDef<TRow> = {
  sortable: true,
  filter: true,
  resizable: true,
};

const currentPage = computed({
  get: () => props.pageNo,
  set: (value) => emit("pageChange", value),
});

const currentPageSize = computed({
  get: () => props.pageSize,
  set: (value) => emit("pageSizeChange", value),
});

function onRowClicked(event: RowClickedEvent<TRow>) {
  if (event.node.rowPinned) {
    return;
  }

  if (event.data) {
    emit("rowClick", event.data);
  }
}

function onSortChanged(event: SortChangedEvent) {
  const sort = event.api
    .getColumnState()
    .filter((column) => column.sort)
    .map((column) => ({
      field: column.colId,
      direction: column.sort as "asc" | "desc",
    }));

  emit("sortChange", sort);
}

function getRowClass(params: RowClassParams<TRow>) {
  return params.node.rowPinned ? "portal-grid-stat-row" : undefined;
}
</script>

<template>
  <div>
    <!-- [9. AG Grid 표준 래퍼] 그리드는 API를 직접 알지 않고 이벤트만 화면으로 전달한다. -->
    <div class="ag-theme-quartz w-full overflow-hidden rounded-lg border border-slate-200" :class="heightClass" v-loading="loading">
      <AgGridVue
        class="h-full w-full"
        :row-data="rows"
        :column-defs="columns"
        :default-col-def="defaultColDef"
        :pinned-bottom-row-data="pinnedBottomRows"
        :get-row-class="getRowClass"
        :row-selection="rowSelection"
        animate-rows
        @row-clicked="onRowClicked"
        @sort-changed="onSortChanged"
      />
    </div>
    <div class="mt-3 flex items-center justify-between gap-3 border-t border-slate-100 pt-3">
      <span class="text-xs font-semibold text-slate-500">총 {{ totalCount.toLocaleString() }}건</span>
      <PortalPagination
        v-model:current-page="currentPage"
        v-model:page-size="currentPageSize"
        :total="totalCount"
        :page-size-options="[10, 20, 50, 100]"
      />
    </div>
  </div>
</template>

<style scoped>
:deep(.portal-grid-stat-row) {
  background: #f8fafc;
  color: #334155;
  font-weight: 700;
}

:deep(.portal-grid-stat-row .ag-cell) {
  border-top: 1px solid #cbd5e1;
}
</style>
