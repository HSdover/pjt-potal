<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { ElMessage, ElSwitch, ElTable, ElTableColumn, ElTag } from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import GridPageLayout from "@/components/GridPageLayout.vue";
import SearchPanel from "@/shared/components/search/SearchPanel.vue";
import { PortalSelect, PortalTextInput } from "@/shared/components/tags";
import { handleApiError } from "@/shared/api/error-handler";
import { confirmUpdate } from "@/shared/feedback/confirm-dialog";
import { fetchPermissionManagementData, updatePermissionAssignment } from "../api";
import type {
  PermissionSubjectType,
  PortalPermissionAssignment,
  PortalPermissionDefinition,
  PortalPermissionManagementData,
} from "../types";

type SubjectOption = {
  label: string;
  value: string;
};

const SUBJECT_LABELS: Record<PermissionSubjectType, string> = {
  IAM_ROLE: "IAM 역할",
  GROUP: "그룹",
  USER: "사용자",
};

const TYPE_LABELS: Record<string, string> = {
  SCREEN: "화면",
  CRUD: "CRUD",
  DOWNLOAD: "다운로드",
  UPLOAD: "업로드",
  ADMIN: "관리",
};

const data = ref<PortalPermissionManagementData>({
  permissions: [],
  subjects: [],
  assignments: [],
});
const loading = ref(false);
const saving = ref(false);
const activeSubjectType = ref<PermissionSubjectType>("IAM_ROLE");
const selectedSubjectId = ref("");
const keyword = ref("");
const selectedPermissionCodes = ref<Set<string>>(new Set());
const newSubjectId = ref("");
const newSubjectName = ref("");

const subjectTypeOptions = computed(() =>
  (Object.entries(SUBJECT_LABELS) as [PermissionSubjectType, string][]).map(([value, label]) => ({ label, value })),
);

const subjects = computed(() =>
  data.value.subjects
    .filter((subject) => subject.subjectType === activeSubjectType.value)
    .sort((left, right) => left.subjectName.localeCompare(right.subjectName)),
);

const subjectOptions = computed<SubjectOption[]>(() =>
  subjects.value.map((subject) => ({
    label: `${subject.subjectName} (${subject.subjectId})`,
    value: subject.subjectId,
  })),
);

const selectedSubject = computed(() =>
  subjects.value.find((subject) => subject.subjectId === selectedSubjectId.value) ?? null,
);

const filteredPermissions = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase();
  if (!normalizedKeyword) {
    return data.value.permissions;
  }

  return data.value.permissions.filter((permission) =>
    [
      permission.permissionCode,
      permission.permissionName,
      permission.permissionType,
      permission.targetKey,
      permission.actionCode,
      permission.description ?? "",
    ].some((value) => value.toLowerCase().includes(normalizedKeyword)),
  );
});

const selectedCount = computed(() => selectedPermissionCodes.value.size);
const canAddSubject = computed(() =>
  activeSubjectType.value !== "IAM_ROLE"
    && newSubjectId.value.trim().length > 0
    && newSubjectName.value.trim().length > 0,
);

watch(activeSubjectType, () => {
  selectFirstSubject();
  newSubjectId.value = "";
  newSubjectName.value = "";
});

watch(selectedSubjectId, () => {
  loadSelectedPermissions();
});

async function load() {
  loading.value = true;
  try {
    data.value = await fetchPermissionManagementData();
    selectFirstSubject();
  } catch (error) {
    handleApiError(error, "권한 정보를 조회하지 못했습니다.");
  } finally {
    loading.value = false;
  }
}

function selectFirstSubject() {
  selectedSubjectId.value = subjects.value[0]?.subjectId ?? "";
  loadSelectedPermissions();
}

function loadSelectedPermissions() {
  const assignment = findAssignment(activeSubjectType.value, selectedSubjectId.value);
  selectedPermissionCodes.value = new Set(assignment?.permissionCodes ?? []);
}

function findAssignment(subjectType: PermissionSubjectType, subjectId: string) {
  return data.value.assignments.find(
    (assignment) => assignment.subjectType === subjectType && assignment.subjectId === subjectId,
  );
}

function isPermissionEnabled(permissionCode: string) {
  return selectedPermissionCodes.value.has(permissionCode);
}

function togglePermission(permissionCode: string, enabled: boolean) {
  const next = new Set(selectedPermissionCodes.value);
  if (enabled) {
    next.add(permissionCode);
  } else {
    next.delete(permissionCode);
  }
  selectedPermissionCodes.value = next;
}

function onPermissionSwitchChange(permissionCode: string, value: string | number | boolean) {
  togglePermission(permissionCode, value === true);
}

function addSubject() {
  if (!canAddSubject.value) {
    ElMessage.warning("대상 ID와 이름을 입력하세요.");
    return;
  }

  const subjectId = newSubjectId.value.trim();
  const subjectName = newSubjectName.value.trim();
  const exists = data.value.subjects.some(
    (subject) => subject.subjectType === activeSubjectType.value && subject.subjectId === subjectId,
  );
  if (exists) {
    selectedSubjectId.value = subjectId;
    ElMessage.warning("이미 등록된 대상입니다.");
    return;
  }

  data.value.subjects = [
    ...data.value.subjects,
    {
      subjectType: activeSubjectType.value,
      subjectId,
      subjectName,
      description: "",
    },
  ];
  selectedSubjectId.value = subjectId;
  selectedPermissionCodes.value = new Set();
  newSubjectId.value = "";
  newSubjectName.value = "";
}

async function save() {
  if (!selectedSubject.value) {
    ElMessage.warning("권한 대상을 선택하세요.");
    return;
  }

  const confirmed = await confirmUpdate(`${selectedSubject.value.subjectName} 권한을 저장하시겠습니까?`);
  if (!confirmed) {
    return;
  }

  saving.value = true;
  try {
    const updated = await updatePermissionAssignment({
      subjectType: selectedSubject.value.subjectType,
      subjectId: selectedSubject.value.subjectId,
      subjectName: selectedSubject.value.subjectName,
      permissionCodes: Array.from(selectedPermissionCodes.value).sort(),
    });
    replaceAssignment(updated);
    ElMessage.success("권한이 저장되었습니다.");
  } catch (error) {
    handleApiError(error, "권한 저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

function replaceAssignment(updated: PortalPermissionAssignment) {
  data.value.assignments = [
    ...data.value.assignments.filter(
      (assignment) => !(assignment.subjectType === updated.subjectType && assignment.subjectId === updated.subjectId),
    ),
    updated,
  ];
}

function permissionTypeLabel(type: string) {
  return TYPE_LABELS[type] ?? type;
}

function permissionTypeTag(type: string) {
  return type === "ADMIN"
    ? "danger"
    : type === "CRUD"
      ? "warning"
      : type === "DOWNLOAD" || type === "UPLOAD"
        ? "success"
        : "info";
}

function rowKey(row: PortalPermissionDefinition) {
  return row.permissionCode;
}

onMounted(load);
</script>

<template>
  <GridPageLayout title="권한관리" description="IAM 역할, 그룹, 사용자별 포털 권한을 관리합니다.">
    <template #toolbar>
      <SearchPanel>
        <PortalSelect v-model="activeSubjectType" :options="subjectTypeOptions" class="w-full md:!w-44" />
        <PortalSelect v-model="selectedSubjectId" :options="subjectOptions" class="w-full md:!w-80" />
        <PortalTextInput
          v-model="keyword"
          clearable
          placeholder="권한 검색"
          class="w-full md:!w-64"
        />

        <template v-if="activeSubjectType !== 'IAM_ROLE'">
          <PortalTextInput v-model="newSubjectId" placeholder="대상 ID" class="w-full md:!w-44" />
          <PortalTextInput v-model="newSubjectName" placeholder="대상 이름" class="w-full md:!w-48" />
          <AuthButton auth="PERMISSION_MANAGE" :icon="Plus" :disabled="!canAddSubject" @click="addSubject">
            대상 추가
          </AuthButton>
        </template>

        <template #actions>
          <AuthButton auth="PERMISSION_MANAGE" type="primary" :disabled="saving || !selectedSubject" @click="save">
            저장
          </AuthButton>
        </template>

        <template #summary>
          전체 권한 {{ data.permissions.length.toLocaleString() }}개
          <span class="ml-2 text-slate-400">선택 {{ selectedCount.toLocaleString() }}개</span>
        </template>
      </SearchPanel>
    </template>

    <section class="portal-section">
      <div class="mb-3 flex flex-wrap items-center gap-2">
        <ElTag type="info" effect="plain">{{ SUBJECT_LABELS[activeSubjectType] }}</ElTag>
        <strong class="text-sm text-slate-700">{{ selectedSubject?.subjectName ?? "대상 없음" }}</strong>
        <span v-if="selectedSubject" class="text-xs text-slate-500">{{ selectedSubject.subjectId }}</span>
      </div>

      <ElTable
        v-loading="loading"
        :data="filteredPermissions"
        :row-key="rowKey"
        border
        height="560"
        class="permission-table"
      >
        <ElTableColumn label="허용" width="90" align="center" fixed>
          <template #default="{ row }: { row: PortalPermissionDefinition }">
            <ElSwitch
              :model-value="isPermissionEnabled(row.permissionCode)"
              size="small"
              @change="(value) => onPermissionSwitchChange(row.permissionCode, value)"
            />
          </template>
        </ElTableColumn>
        <ElTableColumn label="유형" width="110">
          <template #default="{ row }: { row: PortalPermissionDefinition }">
            <ElTag :type="permissionTypeTag(row.permissionType)" effect="plain" size="small">
              {{ permissionTypeLabel(row.permissionType) }}
            </ElTag>
          </template>
        </ElTableColumn>
        <ElTableColumn prop="permissionName" label="권한명" min-width="180" />
        <ElTableColumn prop="permissionCode" label="권한코드" min-width="180" />
        <ElTableColumn prop="targetKey" label="대상" min-width="150" />
        <ElTableColumn prop="actionCode" label="동작" width="120" />
        <ElTableColumn prop="description" label="설명" min-width="320" show-overflow-tooltip />
      </ElTable>
    </section>
  </GridPageLayout>
</template>

<style scoped>
.permission-table :deep(.el-table__cell) {
  vertical-align: middle;
}
</style>
