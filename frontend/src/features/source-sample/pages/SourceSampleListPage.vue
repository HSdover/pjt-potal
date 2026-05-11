<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElInput, ElMessage, ElOption, ElSelect } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchSourceSampleList } from "../api";
import { sourceSampleColumns } from "../columns";
import type { SourceSampleItem, SourceSampleSearchFilter } from "../types";

const rows = ref<SourceSampleItem[]>([]);
const loading = ref(false);
const totalCount = ref(0);

// [7. 목록 조회 표준 계약] 원천 샘플 화면도 기준 목록 요청 상태를 사용한다.
const request = reactive<ListRequest<SourceSampleSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    region: "",
    keyword: "",
  },
});

const columns = computed(() => sourceSampleColumns);

async function load() {
  loading.value = true;
  try {
    const response = await fetchSourceSampleList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "원천 샘플 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.region = "";
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
  <!-- [15. 기준 샘플 화면] 두 번째 목록 화면으로 공통 템플릿 재사용성을 검증한다. -->
  <GridPageLayout
    title="원천 샘플 데이터"
    description="심평원 요양기관 개설 현황 원천 CSV에서 추출한 샘플 데이터를 조회합니다."
  >
    <template #toolbar>
      <SearchPanel>
        <ElSelect v-model="request.filters.region" clearable placeholder="지역" class="w-full md:!w-40">
          <ElOption label="서울" value="서울" />
          <ElOption label="경기" value="경기" />
          <ElOption label="부산" value="부산" />
          <ElOption label="대구" value="대구" />
        </ElSelect>
        <ElInput
          v-model="request.filters.keyword"
          :prefix-icon="Search"
          clearable
          placeholder="검색어"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />

        <template #actions>
          <!-- [12. 메뉴, 라우터, 권한 표준] 업무 버튼은 권한 컴포넌트를 통해 노출한다. -->
          <AuthButton auth="SOURCE_SAMPLE_READ" @click="reset">초기화</AuthButton>
          <AuthButton auth="SOURCE_SAMPLE_READ" type="primary" @click="search">조회</AuthButton>
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
