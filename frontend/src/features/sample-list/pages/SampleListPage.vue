<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import useVuelidate from "@vuelidate/core";
import { ElDialog, ElForm, ElFormItem, ElInput, ElMessage, ElMessageBox } from "element-plus";
import { Delete, Edit, Plus, Search } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fieldError, maxLengthText, requiredText } from "@/shared/validation/vuelidate";
import { createSample, deleteSample, fetchList, updateSample } from "../api";
import { columns as gridColumns } from "../columns";
import type { SampleListItem, SampleListSearchFilter } from "../types";

type SampleForm = {
  name: string;
  description: string;
};

const rows = ref<SampleListItem[]>([]);
const loading = ref(false);
const saving = ref(false);
const totalCount = ref(0);
const selectedRow = ref<SampleListItem | null>(null);
const dialogVisible = ref(false);
const dialogMode = ref<"create" | "update">("create");

const request = reactive<ListRequest<SampleListSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    keyword: "",
  },
});

const form = reactive<SampleForm>({
  name: "",
  description: "",
});

const columns = computed(() => gridColumns);
const dialogTitle = computed(() => (dialogMode.value === "create" ? "샘플 등록" : "샘플 수정"));
const rules = computed(() => ({
  name: {
    required: requiredText("이름"),
    maxLength: maxLengthText("이름", 200),
  },
  description: {
    maxLength: maxLengthText("설명", 1000),
  },
}));
const v$ = useVuelidate(rules, form, { $autoDirty: true });

async function load() {
  loading.value = true;
  try {
    const response = await fetchList(request);
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;

    if (selectedRow.value && !response.rows.some((row) => row.id === selectedRow.value?.id)) {
      selectedRow.value = null;
    }
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
  selectedRow.value = null;
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

function onRowClick(row: SampleListItem) {
  selectedRow.value = row;
}

function openCreateDialog() {
  dialogMode.value = "create";
  form.name = "";
  form.description = "";
  v$.value.$reset();
  dialogVisible.value = true;
}

function openUpdateDialog() {
  if (!selectedRow.value) {
    ElMessage.warning("수정할 행을 선택하세요.");
    return;
  }

  dialogMode.value = "update";
  form.name = selectedRow.value.name;
  form.description = selectedRow.value.description ?? "";
  v$.value.$reset();
  dialogVisible.value = true;
}

async function save() {
  const valid = await v$.value.$validate();
  if (!valid) {
    ElMessage.warning("입력값을 확인하세요.");
    return;
  }

  saving.value = true;
  try {
    const payload = {
      name: form.name.trim(),
      description: form.description?.trim() || undefined,
    };

    if (dialogMode.value === "create") {
      selectedRow.value = await createSample(payload);
      ElMessage.success("등록되었습니다.");
    } else if (selectedRow.value) {
      selectedRow.value = await updateSample(selectedRow.value.id, payload);
      ElMessage.success("수정되었습니다.");
    }

    dialogVisible.value = false;
    void load();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

async function remove() {
  if (!selectedRow.value) {
    ElMessage.warning("삭제할 행을 선택하세요.");
    return;
  }

  try {
    await ElMessageBox.confirm("선택한 샘플을 삭제하시겠습니까?", "삭제 확인", {
      confirmButtonText: "삭제",
      cancelButtonText: "취소",
      type: "warning",
    });

    await deleteSample(selectedRow.value.id);
    selectedRow.value = null;
    ElMessage.success("삭제되었습니다.");
    void load();
  } catch (error) {
    if (error === "cancel" || error === "close") {
      return;
    }
    ElMessage.error(error instanceof Error ? error.message : "삭제에 실패했습니다.");
  }
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="샘플 목록" description="샘플 데이터를 검색하고 관리합니다.">
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
          <AuthButton auth="SAMPLE_READ" type="primary" :icon="Search" @click="search">조회</AuthButton>
          <AuthButton auth="SAMPLE_CREATE" type="success" :icon="Plus" @click="openCreateDialog">등록</AuthButton>
          <AuthButton
            auth="SAMPLE_UPDATE"
            type="warning"
            :icon="Edit"
            :disabled="!selectedRow"
            @click="openUpdateDialog"
          >
            수정
          </AuthButton>
          <AuthButton auth="SAMPLE_DELETE" type="danger" :icon="Delete" :disabled="!selectedRow" @click="remove">
            삭제
          </AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
          <span v-if="selectedRow" class="ml-2 text-slate-400">선택: {{ selectedRow.name }}</span>
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
      @row-click="onRowClick"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="480px">
      <ElForm label-position="top">
        <ElFormItem label="이름" required :error="fieldError(v$.name)">
          <ElInput v-model="form.name" maxlength="200" show-word-limit @blur="v$.name.$touch()" />
        </ElFormItem>
        <ElFormItem label="설명" :error="fieldError(v$.description)">
          <ElInput
            v-model="form.description"
            type="textarea"
            :rows="4"
            maxlength="1000"
            show-word-limit
            @blur="v$.description.$touch()"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <AuthButton :auth="dialogMode === 'create' ? 'SAMPLE_CREATE' : 'SAMPLE_UPDATE'" :disabled="saving" @click="dialogVisible = false">취소</AuthButton>
        <AuthButton :auth="dialogMode === 'create' ? 'SAMPLE_CREATE' : 'SAMPLE_UPDATE'" type="primary" :disabled="saving" @click="save">저장</AuthButton>
      </template>
    </ElDialog>
  </GridPageLayout>
</template>
