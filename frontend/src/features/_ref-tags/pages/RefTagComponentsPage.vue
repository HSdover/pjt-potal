<script setup lang="ts">
import { computed, ref } from "vue";
import { ElMessage } from "element-plus";
import { handleApiError } from "@/shared/api/error-handler";
import { confirmDelete, confirmSave } from "@/shared/feedback/confirm-dialog";
import {
  PortalButton,
  PortalCheckboxGroup,
  PortalDateInput,
  PortalDownloadLink,
  PortalExternalLink,
  PortalFileLink,
  PortalFilePicker,
  PortalPagination,
  PortalRadioGroup,
  PortalSelect,
  PortalTextarea,
  PortalTextInput,
  type PortalCheckOption,
  type PortalRadioOption,
  type PortalSelectOption,
} from "@/shared/components/tags";
import { uploadReferenceAttachment, type ReferenceAttachment } from "../api";

const textValue = ref("");
const selectValue = ref("");
const buttonCount = ref(0);
const checkedValues = ref<string[]>(["email"]);
const radioValue = ref("personal");
const dateValue = ref("2026-05-19");
const uploadedAttachment = ref<ReferenceAttachment | null>({
  attachmentId: "sample",
  fileName: "attachment-sample.txt",
  size: 0,
  contentType: "text/plain",
  downloadUrl: "/api/_ref/attachments/sample",
});
const pickedFiles = ref<File[]>([]);
const filePickerRef = ref<{ commit: () => File[] } | null>(null);
const savingAttachment = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const memo = ref("");
const downloaded = ref(false);

const selectOptions: PortalSelectOption[] = [
  { label: "전체", value: "all" },
  { label: "신청", value: "request" },
  { label: "승인", value: "approval" },
  { label: "완료", value: "done" },
];

const checkboxOptions: PortalCheckOption[] = [
  { label: "이메일 알림", value: "email" },
  { label: "화면 표시", value: "display" },
];

const radioOptions: PortalRadioOption[] = [
  { label: "개인", value: "personal" },
  { label: "부서", value: "team" },
];

const pageSummary = computed(() => `현재 ${currentPage.value}페이지 / ${pageSize.value}개씩 보기`);

function onDownloadClick() {
  downloaded.value = true;
}

async function savePickedFiles() {
  const files = filePickerRef.value?.commit() ?? pickedFiles.value;
  const file = files[0];
  if (!file) {
    ElMessage.warning("저장할 첨부파일을 선택하세요.");
    return;
  }

  const confirmed = await confirmSave("선택한 첨부파일을 저장하시겠습니까?");
  if (!confirmed) {
    return;
  }

  savingAttachment.value = true;
  try {
    uploadedAttachment.value = await uploadReferenceAttachment(file);
    ElMessage.success("첨부파일을 저장했습니다.");
  } catch (error) {
    handleApiError(error, "첨부파일 저장에 실패했습니다.");
  } finally {
    savingAttachment.value = false;
  }
}

async function removeUploadedAttachment() {
  const confirmed = await confirmDelete("화면에서 첨부파일 링크를 제거하시겠습니까?");
  if (confirmed) {
    uploadedAttachment.value = null;
  }
}
</script>

<template>
  <main class="portal-page">
    <header class="portal-page-header">
      <h2 class="portal-page-title">참고: 공통 태그 컴포넌트</h2>
      <p class="portal-page-description">
        공통 컴포넌트 정의 이미지에서 지도 API를 제외한 입력, 선택, 파일, 링크, 페이지 컴포넌트 견본이다.
      </p>
    </header>

    <section class="portal-card overflow-hidden">
      <table class="portal-component-table">
        <thead>
          <tr>
            <th>No.</th>
            <th>컴포넌트</th>
            <th>명칭</th>
            <th>동작 확인</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>1</td>
            <td><PortalTextInput v-model="textValue" placeholder="Text input" /></td>
            <td>텍스트박스</td>
            <td>{{ textValue || "텍스트 입력 대기" }}</td>
          </tr>
          <tr>
            <td>2</td>
            <td><PortalSelect v-model="selectValue" :options="selectOptions" placeholder="선택" /></td>
            <td>드롭다운메뉴</td>
            <td>{{ selectValue || "항목 선택 대기" }}</td>
          </tr>
          <tr>
            <td>3</td>
            <td><PortalButton variant="secondary" @click="buttonCount += 1">Button</PortalButton></td>
            <td>버튼</td>
            <td>{{ buttonCount }}회 클릭</td>
          </tr>
          <tr>
            <td>4</td>
            <td><PortalCheckboxGroup v-model="checkedValues" :options="checkboxOptions" /></td>
            <td>체크박스</td>
            <td>{{ checkedValues.join(", ") || "선택 없음" }}</td>
          </tr>
          <tr>
            <td>5</td>
            <td><PortalRadioGroup v-model="radioValue" name="ref-radio" :options="radioOptions" /></td>
            <td>라디오버튼</td>
            <td>{{ radioValue }}</td>
          </tr>
          <tr>
            <td>6</td>
            <td><PortalDateInput v-model="dateValue" /></td>
            <td>달력 버튼</td>
            <td>{{ dateValue }}</td>
          </tr>
          <tr>
            <td>7</td>
            <td>
              <PortalFileLink
                v-if="uploadedAttachment"
                :file-name="uploadedAttachment.fileName"
                :href="uploadedAttachment.downloadUrl"
                @remove="removeUploadedAttachment"
              />
              <span v-else class="text-sm text-slate-400">삭제됨</span>
            </td>
            <td>업로드 파일 링크</td>
            <td>{{ uploadedAttachment?.fileName || "파일 링크 제거" }}</td>
          </tr>
          <tr>
            <td>8</td>
            <td>
              <div class="flex flex-wrap items-center gap-2">
                <PortalFilePicker ref="filePickerRef" v-model="pickedFiles" accept=".zip,.pdf,.xlsx" defer />
                <PortalButton variant="primary" :disabled="savingAttachment" @click="savePickedFiles">저장</PortalButton>
              </div>
            </td>
            <td>파일 업로드 버튼</td>
            <td>{{ uploadedAttachment ? `${uploadedAttachment.fileName} 저장됨` : "저장된 파일 없음" }}</td>
          </tr>
          <tr>
            <td>9</td>
            <td><PortalPagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="96" /></td>
            <td>게시판 목록 페이지</td>
            <td>{{ pageSummary }}</td>
          </tr>
          <tr>
            <td>10</td>
            <td><PortalTextarea v-model="memo" placeholder="Text input" :rows="3" /></td>
            <td>텍스트 에어리어</td>
            <td>{{ memo ? `${memo.length}자 입력` : "2 ~ 3줄 사이 텍스트 입력" }}</td>
          </tr>
          <tr>
            <td>11</td>
            <td><PortalExternalLink href="https://www.samsungpop.com/">바로가기</PortalExternalLink></td>
            <td>링크(새창)</td>
            <td>새 창 열림</td>
          </tr>
          <tr>
            <td>12</td>
            <td>
              <PortalDownloadLink
                href="data:text/plain;charset=utf-8,download-sample"
                file-name="sample.txt"
                label="다운로드"
                @click="onDownloadClick"
              />
            </td>
            <td>파일 다운로드 링크</td>
            <td>{{ downloaded ? "다운로드 클릭됨" : "다운로드 대기" }}</td>
          </tr>
        </tbody>
      </table>
    </section>
  </main>
</template>
