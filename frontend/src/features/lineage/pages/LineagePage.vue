<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { ElCard, ElInput, ElMessage, ElOption, ElSelect } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import LineageFlow from "@/components/LineageFlow.vue";
import { fetchLineage } from "../api";
import { lineageColumns } from "../columns";
import type { LineageFlowItem } from "../types";

// 리니지는 전체 단계가 수십 건 이하의 소량 데이터이므로 클라이언트 필터를 허용한다.
const rows = ref<LineageFlowItem[]>([]);
const loading = ref(false);
const transformType = ref("");
const searchKeyword = ref("");
const appliedTransformType = ref("");
const appliedSearchKeyword = ref("");

const filteredRows = computed(() => {
  let result = rows.value;

  if (appliedTransformType.value) {
    result = result.filter((row) => row.transformType === appliedTransformType.value);
  }

  if (appliedSearchKeyword.value) {
    const keyword = appliedSearchKeyword.value.toLowerCase();
    result = result.filter((row) =>
      [row.processName, row.sourceName, row.targetName, row.description]
        .some((field) => String(field ?? "").toLowerCase().includes(keyword)),
    );
  }

  return result;
});

async function load() {
  loading.value = true;
  try {
    rows.value = await fetchLineage();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "데이터 리니지 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

function search() {
  appliedTransformType.value = transformType.value;
  appliedSearchKeyword.value = searchKeyword.value.trim();
}

function reset() {
  transformType.value = "";
  searchKeyword.value = "";
  search();
}

onMounted(load);
</script>

<template>
  <!-- [5.6 LineagePage] 그래프와 단계 목록을 함께 제공하는 데이터 흐름 화면의 기준 구현체이다. -->
  <main class="mx-auto max-w-screen-2xl px-6 py-6" v-loading="loading">
    <header class="mb-4">
      <h2 class="text-xl font-bold text-slate-800">데이터 리니지</h2>
      <p class="mt-1 text-sm text-slate-500">원천부터 화면까지 이어지는 데이터 처리 흐름입니다.</p>
    </header>

    <section class="grid gap-4 xl:grid-cols-[1.4fr_0.9fr]">
      <ElCard shadow="never">
        <template #header>흐름 그래프</template>
        <LineageFlow :rows="filteredRows" />
      </ElCard>

      <ElCard shadow="never">
        <template #header>
          <div class="flex flex-col gap-3">
            <span>처리 단계</span>
            <SearchPanel>
              <ElSelect v-model="transformType" clearable placeholder="처리 유형" class="w-full md:!w-36">
                <ElOption label="DOWNLOAD" value="DOWNLOAD" />
                <ElOption label="TRANSFORM" value="TRANSFORM" />
                <ElOption label="LOAD" value="LOAD" />
                <ElOption label="SERVE" value="SERVE" />
              </ElSelect>
              <ElInput
                v-model="searchKeyword"
                :prefix-icon="Search"
                clearable
                placeholder="검색어"
                class="w-full md:!w-52"
                @keyup.enter="search"
              />
              <template #actions>
                <AuthButton auth="LINEAGE_READ" @click="reset">초기화</AuthButton>
                <AuthButton auth="LINEAGE_READ" type="primary" @click="search">조회</AuthButton>
              </template>
              <template #summary>
                표시 {{ filteredRows.length.toLocaleString() }}건 / 전체 {{ rows.length.toLocaleString() }}건
              </template>
            </SearchPanel>
          </div>
        </template>

        <BaseGrid
          :rows="filteredRows"
          :columns="lineageColumns"
          :total-count="filteredRows.length"
          :page-size="50"
          height-class="h-96"
        />
      </ElCard>
    </section>
  </main>
</template>
