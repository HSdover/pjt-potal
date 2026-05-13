<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElInput, ElMessage } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchList } from "../api";
import { columns as gridColumns } from "../columns";
import type { SampleListItem, SampleListSearchFilter } from "../types";

const rows = ref<SampleListItem[]>([]);
const loading = ref(false);
const totalCount = ref(0);

const request = reactive<ListRequest<SampleListSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    keyword: "",
  },
});

const columns = computed(() => gridColumns);

async function load() {
  loading.value = true;
  try {
    const response = await fetchList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.keyword = "";
  request.pageNo = 1;
  request.sort = [];
  void load();
}

function onPageChange(pageNo: number) {
  request.pageNo = pageNo;
  void load();
}

function onPageSizeChange(pageSize: number) {
  request.pageSize = pageSize;
  request.pageNo = 1;
  void load();
}

function onSortChange(sort: ListSort[]) {
  request.sort = sort;
  request.pageNo = 1;
  void load();
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="샘플 목록" description="샘플 데이터를 검색하고 조회합니다.">
    <template #toolbar>
      <SearchPanel>
        <ElInput
          v-model="request.filters.keyword"
          :prefix-icon="Search"
          clearable
          placeholder="검색어"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />

        <template #actions>
          <AuthButton auth="SAMPLE_READ" @click="reset">초기화</AuthButton>
          <AuthButton auth="SAMPLE_READ" type="primary" @click="search">조회</AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
        </template>
      </SearchPanel>
    </template>

    <BaseGrid
      :rows="rows"
      :columns="columns"
      :loading="loading"
      :total-count="totalCount"
      :page-no="request.pageNo"
      :page-size="request.pageSize"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />
  </GridPageLayout>
</template>
