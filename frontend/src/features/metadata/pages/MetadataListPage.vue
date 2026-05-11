<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import { ElCard, ElInput, ElMessage, ElOption, ElSelect, ElTag } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fetchMetadataList } from "../api";
import { metadataColumns } from "../columns";
import type { MetadataCatalogItem, MetadataSearchFilter } from "../types";

const rows = ref<MetadataCatalogItem[]>([]);
const selected = ref<MetadataCatalogItem | null>(null);
const loading = ref(false);
const totalCount = ref(0);

// [7. 목록 조회 표준 계약] 목록 화면은 검색조건, 페이징, 정렬을 하나의 요청 상태로 관리한다.
const request = reactive<ListRequest<MetadataSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    datasetType: "",
    keyword: "",
  },
});

const columns = computed(() => metadataColumns);

async function load() {
  loading.value = true;
  try {
    const response = await fetchMetadataList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "메타데이터 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.datasetType = "";
  request.filters.keyword = "";
  request.pageNo = 1;
  request.sort = [];
  selected.value = null;
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

function selectRow(row: unknown) {
  const item = row as MetadataCatalogItem;
  selected.value = selected.value?.metadataId === item.metadataId ? null : item;
}

onMounted(load);
</script>

<template>
  <!-- [15. 기준 샘플 화면] SI 목록+상세 화면의 기준 구현체이다. -->
  <GridPageLayout
    title="메타데이터 조회"
    description="등록된 데이터셋의 메타데이터를 조회합니다. 행을 클릭하면 컬럼 상세를 확인할 수 있습니다."
  >
    <template #toolbar>
      <SearchPanel>
        <ElSelect v-model="request.filters.datasetType" clearable placeholder="데이터 유형" class="w-full md:!w-44">
          <ElOption label="원천 파일" value="원천 파일" />
          <ElOption label="샘플 파일" value="샘플 파일" />
          <ElOption label="원천 샘플 테이블" value="원천 샘플 테이블" />
          <ElOption label="TABLE" value="TABLE" />
          <ElOption label="FILE" value="FILE" />
          <ElOption label="SCREEN" value="SCREEN" />
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
          <!-- [12. 메뉴, 라우터, 권한 표준] 조회/초기화 버튼도 권한 제어 기준을 따른다. -->
          <AuthButton auth="METADATA_READ" @click="reset">초기화</AuthButton>
          <AuthButton auth="METADATA_READ" type="primary" @click="search">조회</AuthButton>
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
      @row-click="selectRow"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />

    <template #detail>
      <transition name="el-fade-in-linear">
        <ElCard v-if="selected" shadow="never">
          <template #header>
            <span class="font-semibold">{{ selected.datasetName }}</span>
            <ElTag type="info" size="small" class="ml-2">컬럼 상세</ElTag>
          </template>
          <div class="flex flex-wrap gap-2">
            <ElTag
              v-for="col in selected.columnsSummary?.split(',').map((value) => value.trim()).filter(Boolean)"
              :key="col"
              type="primary"
              effect="plain"
              size="small"
            >
              {{ col }}
            </ElTag>
          </div>
          <p class="mt-3 text-sm text-slate-500">{{ selected.description }}</p>
        </ElCard>
      </transition>
    </template>
  </GridPageLayout>
</template>
