<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import useVuelidate from "@vuelidate/core";
import { ElDialog, ElForm, ElFormItem, ElMessage, ElMessageBox } from "element-plus";
import { Delete, Download, Edit, Plus, Search, Upload } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import { PortalTextInput, PortalTextarea } from "@/shared/components/tags";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import { handleApiError } from "@/shared/api/error-handler";
import { confirmDelete, confirmSave, confirmUpdate } from "@/shared/feedback/confirm-dialog";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { fieldError, maxLengthText, requiredText } from "@/shared/validation/vuelidate";
import {
  createSample,
  deleteSample,
  downloadExcel as downloadSampleJpaExcel,
  downloadLargeExcel,
  fetchLargeExcel,
  fetchList,
  requestLargeExcel,
  updateSample,
  uploadExcel,
} from "../api";
import { columns as gridColumns } from "../columns";
import type { SampleListJpaItem, SampleListJpaSearchFilter } from "../types";

type SampleJpaForm = {
  name: string;
  description: string;
};

const rows = ref<SampleListJpaItem[]>([]);
const loading = ref(false);
const saving = ref(false);
const downloading = ref(false);
const requestingLargeDownload = ref(false);
const uploading = ref(false);
const totalCount = ref(0);
const selectedRow = ref<SampleListJpaItem | null>(null);
const dialogVisible = ref(false);
const dialogMode = ref<"create" | "update">("create");
const uploadInput = ref<HTMLInputElement | null>(null);

const request = reactive<ListRequest<SampleListJpaSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    keyword: "",
  },
});

const form = reactive<SampleJpaForm>({
  name: "",
  description: "",
});

const columns = computed(() => gridColumns);
const pinnedBottomRows = computed<SampleListJpaItem[]>(() => {
  const pageRowCount = rows.value.length;
  const describedRowCount = rows.value.filter((row) => row.description?.trim()).length;
  const emptyDescriptionCount = pageRowCount - describedRowCount;

  return [
    {
      id: 0,
      name: `현재 페이지 ${pageRowCount.toLocaleString()}건 / 전체 ${totalCount.value.toLocaleString()}건`,
      description: `설명 입력 ${describedRowCount.toLocaleString()}건 / 설명 미입력 ${emptyDescriptionCount.toLocaleString()}건 / 선택 ${selectedRow.value ? "1" : "0"}건`,
    },
  ];
});
const dialogTitle = computed(() => (dialogMode.value === "create" ? "JPA 샘플 등록" : "JPA 샘플 수정"));
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
    handleApiError(error, "목록 조회에 실패했습니다.");
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

function onRowClick(row: SampleListJpaItem) {
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

  const confirmed = dialogMode.value === "create"
    ? await confirmSave("JPA 샘플을 등록하시겠습니까?")
    : await confirmUpdate("JPA 샘플을 수정하시겠습니까?");
  if (!confirmed) {
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
    handleApiError(error, "저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

async function remove() {
  if (!selectedRow.value) {
    ElMessage.warning("삭제할 행을 선택하세요.");
    return;
  }

  const confirmed = await confirmDelete("선택한 JPA 샘플을 삭제하시겠습니까?");
  if (!confirmed) {
    return;
  }

  try {
    await deleteSample(selectedRow.value.id);
    selectedRow.value = null;
    ElMessage.success("삭제되었습니다.");
    void load();
  } catch (error) {
    handleApiError(error, "삭제에 실패했습니다.");
  }
}

async function downloadExcelFile() {
  downloading.value = true;
  try {
    await downloadSampleJpaExcel(request);
    ElMessage.success("엑셀 다운로드를 시작했습니다.");
  } catch (error) {
    handleApiError(error, "엑셀 다운로드에 실패했습니다.");
  } finally {
    downloading.value = false;
  }
}

async function requestLargeDownloadFile() {
  requestingLargeDownload.value = true;
  try {
    const requested = await requestLargeExcel(request);
    ElMessage.info(`대용량 엑셀 생성 요청을 등록했습니다. 대상 ${requested.totalRows.toLocaleString()}건`);

    const completed = await waitLargeDownload(requested.jobId);
    if (completed.downloadUrl) {
      await downloadLargeExcel(completed.jobId);
      ElMessage.success("대용량 엑셀 다운로드를 시작했습니다.");
    }
  } catch (error) {
    handleApiError(error, "대용량 엑셀 다운로드에 실패했습니다.");
  } finally {
    requestingLargeDownload.value = false;
  }
}

function openExcelUpload() {
  uploadInput.value?.click();
}

async function onExcelFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = "";

  if (!file) {
    return;
  }

  uploading.value = true;
  try {
    const result = await uploadExcel(file);
    if (result.errorRows > 0) {
      await ElMessageBox.alert(importErrorMessage(result), "엑셀 업로드 검증 결과", {
        confirmButtonText: "확인",
        type: "warning",
      });
      return;
    }

    ElMessage.success(`엑셀 업로드가 완료되었습니다. 반영 ${result.successRows.toLocaleString()}건`);
    void load();
  } catch (error) {
    handleApiError(error, "엑셀 업로드에 실패했습니다.");
  } finally {
    uploading.value = false;
  }
}

async function waitLargeDownload(jobId: string) {
  for (let attempt = 0; attempt < 60; attempt++) {
    const job = await fetchLargeExcel(jobId);
    if (job.status === "COMPLETED") {
      return job;
    }
    if (job.status === "FAILED") {
      throw new Error(job.message || "Large Excel export failed.");
    }
    await delay(1000);
  }

  throw new Error("Large Excel export timed out.");
}

function delay(ms: number) {
  return new Promise((resolve) => window.setTimeout(resolve, ms));
}

function importErrorMessage(result: { totalRows: number; errorRows: number; errors: { rowIndex: number; column: string; message: string }[] }) {
  const lines = result.errors
    .slice(0, 10)
    .map((error) => `- ${error.rowIndex}행 / ${error.column}: ${error.message}`);
  const suffix = result.errors.length > 10 ? `\n외 ${result.errors.length - 10}건` : "";

  return [
    `전체 ${result.totalRows.toLocaleString()}건 중 오류 ${result.errorRows.toLocaleString()}건이 있어 반영하지 않았습니다.`,
    ...lines,
  ].join("\n") + suffix;
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="JPA 샘플 목록" description="JPA 기반 샘플 데이터를 검색하고 관리합니다.">
    <template #toolbar>
      <SearchPanel>
        <PortalTextInput
          v-model="request.filters.keyword"
          clearable
          placeholder="검색어"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />

        <template #actions>
          <AuthButton auth="SAMPLE_JPA_READ" @click="reset">초기화</AuthButton>
          <AuthButton auth="SAMPLE_JPA_READ" type="primary" :icon="Search" @click="search">조회</AuthButton>
          <AuthButton auth="SAMPLE_JPA_EXPORT" type="info" :icon="Download" :disabled="downloading" @click="downloadExcelFile">
            엑셀 다운로드
          </AuthButton>
          <AuthButton
            auth="SAMPLE_JPA_EXPORT"
            type="info"
            :icon="Download"
            :disabled="requestingLargeDownload"
            @click="requestLargeDownloadFile"
          >
            대용량 다운로드
          </AuthButton>
          <AuthButton auth="SAMPLE_JPA_IMPORT" type="success" :icon="Upload" :disabled="uploading" @click="openExcelUpload">
            엑셀 업로드
          </AuthButton>
          <AuthButton auth="SAMPLE_JPA_CREATE" type="success" :icon="Plus" @click="openCreateDialog">등록</AuthButton>
          <AuthButton
            auth="SAMPLE_JPA_UPDATE"
            type="warning"
            :icon="Edit"
            :disabled="!selectedRow"
            @click="openUpdateDialog"
          >
            수정
          </AuthButton>
          <AuthButton auth="SAMPLE_JPA_DELETE" type="danger" :icon="Delete" :disabled="!selectedRow" @click="remove">
            삭제
          </AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
          <span v-if="selectedRow" class="ml-2 text-slate-400">선택: {{ selectedRow.name }}</span>
        </template>
      </SearchPanel>
      <input ref="uploadInput" class="hidden" type="file" accept=".xlsx" @change="onExcelFileChange" />
    </template>

    <BaseGrid
      :rows="rows"
      :columns="columns"
      :pinned-bottom-rows="pinnedBottomRows"
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
          <PortalTextInput v-model="form.name" :maxlength="200" show-count @blur="v$.name.$touch()" />
        </ElFormItem>
        <ElFormItem label="설명" :error="fieldError(v$.description)">
          <PortalTextarea
            v-model="form.description"
            :rows="4"
            :maxlength="1000"
            show-count
            @blur="v$.description.$touch()"
          />
        </ElFormItem>
      </ElForm>

      <template #footer>
        <AuthButton
          :auth="dialogMode === 'create' ? 'SAMPLE_JPA_CREATE' : 'SAMPLE_JPA_UPDATE'"
          :disabled="saving"
          @click="dialogVisible = false"
        >
          취소
        </AuthButton>
        <AuthButton
          :auth="dialogMode === 'create' ? 'SAMPLE_JPA_CREATE' : 'SAMPLE_JPA_UPDATE'"
          type="primary"
          :disabled="saving"
          @click="save"
        >
          저장
        </AuthButton>
      </template>
    </ElDialog>
  </GridPageLayout>
</template>
