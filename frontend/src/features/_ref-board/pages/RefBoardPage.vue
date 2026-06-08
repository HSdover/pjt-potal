<script setup lang="ts">
import { computed, markRaw, nextTick, onMounted, reactive, ref } from "vue";
import useVuelidate from "@vuelidate/core";
import type { ColDef } from "ag-grid-community";
import { ElDescriptions, ElDescriptionsItem, ElDialog, ElForm, ElFormItem, ElMessage } from "element-plus";
import { Delete, Edit, Plus, Search } from "@element-plus/icons-vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import BaseGrid from "@/shared/components/grid/BaseGrid.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import { handleApiError } from "@/shared/api/error-handler";
import { confirmDelete, confirmSave, confirmUpdate } from "@/shared/feedback/confirm-dialog";
import {
  PortalFileLink,
  PortalFilePicker,
  PortalRichTextEditor,
  PortalSelect,
  PortalTextInput,
  type PortalSelectOption,
} from "@/shared/components/tags";
import type { ListRequest, ListSort } from "@/shared/types/list";
import { sanitizeRichTextHtml } from "@/shared/utils/rich-text";
import { fieldError, maxLengthText, requiredText } from "@/shared/validation/vuelidate";
import { createBoard, deleteBoard, fetchBoard, fetchBoardList, updateBoard, uploadBoardAttachment } from "../api";
import BoardAttachmentCell from "../components/BoardAttachmentCell.vue";
import type { RefBoardAttachment, RefBoardItem, RefBoardSaveRequest, RefBoardSearchFilter } from "../types";

type BoardForm = RefBoardSaveRequest;
type FilePickerExpose = {
  commit: () => File[];
  clear: () => void;
};

const ALL_CATEGORY = "__ALL__";
const categories = ["공지", "자료", "운영", "질문"];
const categoryOptions: PortalSelectOption[] = categories.map((value) => ({ label: value, value }));
const categoryFilterOptions: PortalSelectOption[] = [
  { label: "전체", value: ALL_CATEGORY },
  ...categoryOptions,
];
const attachmentCellRenderer = markRaw(BoardAttachmentCell);

const rows = ref<RefBoardItem[]>([]);
const selected = ref<RefBoardItem | null>(null);
const loading = ref(false);
const detailLoading = ref(false);
const saving = ref(false);
const totalCount = ref(0);
const dialogVisible = ref(false);
const detailDialogVisible = ref(false);
const dialogMode = ref<"create" | "update">("create");
const pickedFiles = ref<File[]>([]);
const formAttachment = ref<RefBoardAttachment | null>(null);
const filePickerRef = ref<FilePickerExpose | null>(null);

const request = reactive<ListRequest<RefBoardSearchFilter>>({
  pageNo: 1,
  pageSize: 20,
  sort: [],
  filters: {
    keyword: "",
    category: ALL_CATEGORY,
  },
});

const form = reactive<BoardForm>({
  title: "",
  category: "공지",
  writerName: "Local Developer",
  content: "",
});

const columns = computed<ColDef<RefBoardItem>[]>(() => [
  { field: "id", headerName: "No", width: 90 },
  { field: "category", headerName: "분류", width: 110 },
  { field: "title", headerName: "제목", minWidth: 240, flex: 1 },
  {
    colId: "attachment",
    headerName: "첨부",
    width: 72,
    filter: false,
    cellRenderer: attachmentCellRenderer,
    valueGetter: (params) => (params.data?.attachment ? 1 : 0),
  },
  { field: "writerName", headerName: "작성자", width: 150 },
  { field: "viewCount", headerName: "조회", width: 100 },
  {
    field: "createdAt",
    headerName: "등록일",
    width: 170,
    valueFormatter: (params) => formatDate(params.value),
  },
]);

const dialogTitle = computed(() => (dialogMode.value === "create" ? "게시글 등록" : "게시글 수정"));
const rules = computed(() => ({
  title: {
    required: requiredText("제목"),
    maxLength: maxLengthText("제목", 200),
  },
  category: {
    required: requiredText("분류"),
  },
  writerName: {
    required: requiredText("작성자"),
    maxLength: maxLengthText("작성자", 50),
  },
  content: {
    required: requiredText("내용"),
    maxLength: maxLengthText("내용", 4000),
  },
}));
const v$ = useVuelidate(rules, form, { $autoDirty: true });
const sanitizedSelectedContent = computed(() => sanitizeRichTextHtml(selected.value?.content ?? ""));

async function load() {
  loading.value = true;
  try {
    const response = await fetchBoardList({
      ...request,
      filters: {
        keyword: request.filters.keyword,
        category: request.filters.category === ALL_CATEGORY ? "" : request.filters.category,
      },
    });
    rows.value = response.rows;
    totalCount.value = response.totalCount;
    request.pageNo = response.pageNo;
    request.pageSize = response.pageSize;

    if (selected.value && !response.rows.some((row) => row.id === selected.value?.id)) {
      selected.value = null;
    }
  } catch (error) {
    handleApiError(error, "게시글 목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

async function loadDetail(id: number) {
  detailDialogVisible.value = true;
  detailLoading.value = true;
  try {
    selected.value = await fetchBoard(id);
    void load();
  } catch (error) {
    detailDialogVisible.value = false;
    handleApiError(error, "게시글 상세 조회에 실패했습니다.");
  } finally {
    detailLoading.value = false;
  }
}

function search() {
  request.pageNo = 1;
  void load();
}

function reset() {
  request.filters.keyword = "";
  request.filters.category = ALL_CATEGORY;
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

function onRowClick(row: RefBoardItem) {
  void loadDetail(row.id);
}

function openCreateDialog() {
  dialogMode.value = "create";
  form.title = "";
  form.category = "공지";
  form.writerName = "Local Developer";
  form.content = "";
  resetAttachmentDraft(null);
  v$.value.$reset();
  dialogVisible.value = true;
}

function openUpdateDialog() {
  if (!selected.value) {
    ElMessage.warning("수정할 게시글을 선택하세요.");
    return;
  }

  dialogMode.value = "update";
  form.title = selected.value.title;
  form.category = selected.value.category;
  form.writerName = selected.value.writerName;
  form.content = selected.value.content;
  resetAttachmentDraft(selected.value.attachment ?? null);
  v$.value.$reset();
  detailDialogVisible.value = false;
  dialogVisible.value = true;
}

async function save() {
  const valid = await v$.value.$validate();
  if (!valid) {
    ElMessage.warning("입력값을 확인하세요.");
    return;
  }

  const confirmed = dialogMode.value === "create"
    ? await confirmSave("게시글을 등록하시겠습니까?")
    : await confirmUpdate("게시글을 수정하시겠습니까?");
  if (!confirmed) {
    return;
  }

  saving.value = true;
  try {
    const payload: RefBoardSaveRequest = {
      title: form.title.trim(),
      category: form.category,
      writerName: form.writerName.trim(),
      content: form.content.trim(),
      attachment: await resolveAttachmentForSave(),
    };

    if (dialogMode.value === "create") {
      selected.value = await createBoard(payload);
      ElMessage.success("등록되었습니다.");
    } else if (selected.value) {
      selected.value = await updateBoard(selected.value.id, payload);
      ElMessage.success("수정되었습니다.");
    }

    dialogVisible.value = false;
    await load();
  } catch (error) {
    handleApiError(error, "게시글 저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

async function remove() {
  if (!selected.value) {
    ElMessage.warning("삭제할 게시글을 선택하세요.");
    return;
  }

  const confirmed = await confirmDelete("선택한 게시글을 삭제하시겠습니까?");
  if (!confirmed) {
    return;
  }

  try {
    await deleteBoard(selected.value.id);
    selected.value = null;
    detailDialogVisible.value = false;
    ElMessage.success("삭제되었습니다.");
    await load();
  } catch (error) {
    handleApiError(error, "게시글 삭제에 실패했습니다.");
  }
}

async function resolveAttachmentForSave() {
  const committedFiles = filePickerRef.value?.commit();
  const files = committedFiles && committedFiles.length > 0 ? committedFiles : pickedFiles.value;
  const file = files[0];

  if (!file) {
    return formAttachment.value;
  }

  return uploadBoardAttachment(file);
}

function resetAttachmentDraft(attachment: RefBoardAttachment | null) {
  formAttachment.value = attachment;
  pickedFiles.value = [];
  void nextTick(() => filePickerRef.value?.clear());
}

async function removeFormAttachment() {
  const confirmed = await confirmDelete("저장 시 첨부파일 연결을 제거하시겠습니까?");
  if (!confirmed) {
    return;
  }

  formAttachment.value = null;
}

function onAttachmentFilesChange(files: File[]) {
  pickedFiles.value = files;
}

function formatDate(value?: string) {
  if (!value) {
    return "-";
  }
  return value.replace("T", " ").slice(0, 16);
}

function formatFileSize(size?: number) {
  if (size === undefined || size < 0) {
    return "-";
  }

  if (size < 1024) {
    return `${size} B`;
  }

  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="참고: 게시판" description="개발 참고 게시글을 조회하고 관리합니다.">
    <template #toolbar>
      <SearchPanel>
        <PortalTextInput
          v-model="request.filters.keyword"
          clearable
          placeholder="제목, 작성자, 내용"
          class="w-full md:!w-80"
          @keyup.enter="search"
        />
        <PortalSelect v-model="request.filters.category" :options="categoryFilterOptions" />

        <template #actions>
          <AuthButton auth="REF_VIEW" @click="reset">초기화</AuthButton>
          <AuthButton auth="REF_VIEW" type="primary" :icon="Search" @click="search">조회</AuthButton>
          <AuthButton auth="REF_VIEW" type="success" :icon="Plus" @click="openCreateDialog">등록</AuthButton>
          <AuthButton auth="REF_VIEW" type="warning" :icon="Edit" :disabled="!selected" @click="openUpdateDialog">
            수정
          </AuthButton>
          <AuthButton auth="REF_VIEW" type="danger" :icon="Delete" :disabled="!selected" @click="remove">
            삭제
          </AuthButton>
        </template>
        <template #summary>
          전체 {{ totalCount.toLocaleString() }}건
          <span v-if="selected" class="ml-2 text-slate-400">선택: {{ selected.title }}</span>
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
      height-class="h-[420px]"
      @row-click="onRowClick"
      @page-change="onPageChange"
      @page-size-change="onPageSizeChange"
      @sort-change="onSortChange"
    />

    <ElDialog v-model="detailDialogVisible" title="게시글 상세" width="760px">
      <div v-loading="detailLoading">
        <div v-if="selected" class="space-y-4">
          <ElDescriptions :column="2" border>
            <ElDescriptionsItem label="No">{{ selected.id }}</ElDescriptionsItem>
            <ElDescriptionsItem label="분류">{{ selected.category }}</ElDescriptionsItem>
            <ElDescriptionsItem label="제목" :span="2">{{ selected.title }}</ElDescriptionsItem>
            <ElDescriptionsItem label="작성자">{{ selected.writerName }}</ElDescriptionsItem>
            <ElDescriptionsItem label="조회">{{ selected.viewCount.toLocaleString() }}</ElDescriptionsItem>
            <ElDescriptionsItem label="등록일">{{ formatDate(selected.createdAt) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="수정일">{{ formatDate(selected.updatedAt) }}</ElDescriptionsItem>
            <ElDescriptionsItem label="첨부파일" :span="2">
              <div v-if="selected.attachment" class="flex flex-wrap items-center gap-2">
                <PortalFileLink
                  :file-name="selected.attachment.fileName"
                  :href="selected.attachment.downloadUrl"
                  :removable="false"
                />
                <span class="text-xs text-slate-500">{{ formatFileSize(selected.attachment.size) }}</span>
              </div>
              <span v-else class="text-sm text-slate-400">첨부파일 없음</span>
            </ElDescriptionsItem>
          </ElDescriptions>
          <div class="ref-board-content" v-html="sanitizedSelectedContent" />
        </div>
        <div v-else class="py-10 text-center text-sm text-slate-400">게시글을 불러오는 중입니다.</div>
      </div>

      <template #footer>
        <AuthButton auth="REF_VIEW" @click="detailDialogVisible = false">닫기</AuthButton>
        <AuthButton auth="REF_VIEW" type="warning" :icon="Edit" :disabled="!selected" @click="openUpdateDialog">
          수정
        </AuthButton>
        <AuthButton auth="REF_VIEW" type="danger" :icon="Delete" :disabled="!selected" @click="remove">
          삭제
        </AuthButton>
      </template>
    </ElDialog>

    <ElDialog v-model="dialogVisible" :title="dialogTitle" width="640px">
      <ElForm label-position="top">
        <div class="grid gap-4 md:grid-cols-2">
          <ElFormItem label="분류" required :error="fieldError(v$.category)">
            <PortalSelect v-model="form.category" :options="categoryOptions" @change="v$.category.$touch()" />
          </ElFormItem>
          <ElFormItem label="작성자" required :error="fieldError(v$.writerName)">
            <PortalTextInput v-model="form.writerName" :maxlength="50" show-count @blur="v$.writerName.$touch()" />
          </ElFormItem>
        </div>
        <ElFormItem label="제목" required :error="fieldError(v$.title)">
          <PortalTextInput v-model="form.title" :maxlength="200" show-count @blur="v$.title.$touch()" />
        </ElFormItem>
        <ElFormItem label="내용" required :error="fieldError(v$.content)">
          <PortalRichTextEditor
            v-model="form.content"
            :min-height="240"
            :maxlength="4000"
            show-count
            @blur="v$.content.$touch()"
          />
        </ElFormItem>
        <ElFormItem label="첨부파일">
          <div class="w-full space-y-2">
            <div v-if="formAttachment" class="flex flex-wrap items-center gap-2">
              <PortalFileLink
                :file-name="formAttachment.fileName"
                :href="formAttachment.downloadUrl"
                @remove="removeFormAttachment"
              />
              <span class="text-xs text-slate-500">{{ formatFileSize(formAttachment.size) }}</span>
            </div>
            <PortalFilePicker
              ref="filePickerRef"
              v-model="pickedFiles"
              accept=".zip,.pdf,.xlsx,.xls,.doc,.docx,.ppt,.pptx,.txt,.csv,.png,.jpg,.jpeg"
              defer
              :disabled="saving"
              @change="onAttachmentFilesChange"
            />
          </div>
        </ElFormItem>
      </ElForm>

      <template #footer>
        <AuthButton auth="REF_VIEW" :disabled="saving" @click="dialogVisible = false">취소</AuthButton>
        <AuthButton auth="REF_VIEW" type="primary" :disabled="saving" @click="save">저장</AuthButton>
      </template>
    </ElDialog>
  </GridPageLayout>
</template>

<style scoped>
.ref-board-content {
  min-height: 180px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: #fff;
  color: #334155;
  font-size: 13px;
  line-height: 1.75;
  padding: 1rem;
  white-space: pre-wrap;
}

.ref-board-content :deep(h2) {
  margin: 0.25rem 0 0.5rem;
  color: #0f172a;
  font-size: 1.2rem;
  font-weight: 800;
}

.ref-board-content :deep(h3) {
  margin: 0.25rem 0 0.45rem;
  color: #0f172a;
  font-size: 1.05rem;
  font-weight: 800;
}

.ref-board-content :deep(p) {
  margin: 0.35rem 0;
}

.ref-board-content :deep(ul),
.ref-board-content :deep(ol) {
  margin: 0.45rem 0;
  padding-left: 1.35rem;
}

.ref-board-content :deep(blockquote) {
  margin: 0.55rem 0;
  border-left: 3px solid #cbd5e1;
  color: #475569;
  padding-left: 0.8rem;
}

.ref-board-content :deep(pre) {
  overflow-x: auto;
  border-radius: 6px;
  background: #0f172a;
  color: #e2e8f0;
  padding: 0.75rem;
}

.ref-board-content :deep(code) {
  border-radius: 4px;
  background: #e2e8f0;
  padding: 0.1rem 0.25rem;
}

.ref-board-content :deep(pre code) {
  background: transparent;
  padding: 0;
}
</style>
