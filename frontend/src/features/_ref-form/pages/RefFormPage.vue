<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import useVuelidate from "@vuelidate/core";
import { ElCard, ElDatePicker, ElForm, ElFormItem, ElInput, ElMessage, ElOption, ElSelect } from "element-plus";
import AuthButton from "@/shared/components/auth/AuthButton.vue";
import { fieldError, maxLengthText, requiredText } from "@/shared/validation/vuelidate";
import { createForm, fetchFormList, updateForm } from "../api";
import type { RefFormItem, RefFormSaveRequest } from "../types";

// [참고 화면] request-form 템플릿 견본이다. 풀페이지 폼 + vuelidate + 저장/취소 액션.
const items = ref<RefFormItem[]>([]);
const mode = ref<"create" | "update">("create");
const editingId = ref<number | null>(null);
const saving = ref(false);
const loading = ref(false);

const form = reactive<RefFormSaveRequest>({
  name: "",
  category: "",
  description: "",
  targetDate: null,
  priority: "NORMAL",
});

const categories = ["기준정보", "정책", "보안", "운영"];
const priorities = ["HIGH", "NORMAL", "LOW"];

const rules = computed(() => ({
  name: {
    required: requiredText("제목"),
    maxLength: maxLengthText("제목", 100),
  },
  category: {
    required: requiredText("분류"),
  },
  description: {
    maxLength: maxLengthText("설명", 1000),
  },
}));

const v$ = useVuelidate(rules, form, { $autoDirty: true });

const pageTitle = computed(() => (mode.value === "create" ? "신규 등록" : `수정: #${editingId.value ?? ""}`));

function resetForm() {
  form.name = "";
  form.category = "";
  form.description = "";
  form.targetDate = null;
  form.priority = "NORMAL";
  v$.value.$reset();
  mode.value = "create";
  editingId.value = null;
}

function loadForEdit(item: RefFormItem) {
  mode.value = "update";
  editingId.value = item.id;
  form.name = item.name;
  form.category = item.category;
  form.description = item.description ?? "";
  form.targetDate = item.targetDate;
  form.priority = item.priority;
  v$.value.$reset();
}

async function loadList() {
  loading.value = true;
  try {
    items.value = await fetchFormList();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "참고 폼 목록 조회에 실패했습니다.");
  } finally {
    loading.value = false;
  }
}

async function save() {
  const valid = await v$.value.$validate();
  if (!valid) {
    ElMessage.warning("입력값을 확인하세요.");
    return;
  }

  saving.value = true;
  try {
    const payload: RefFormSaveRequest = {
      name: form.name.trim(),
      category: form.category,
      description: form.description?.trim() ?? "",
      targetDate: form.targetDate ?? null,
      priority: form.priority,
    };

    if (mode.value === "create") {
      await createForm(payload);
      ElMessage.success("등록되었습니다.");
    } else if (editingId.value !== null) {
      await updateForm(editingId.value, payload);
      ElMessage.success("수정되었습니다.");
    }

    resetForm();
    await loadList();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : "저장에 실패했습니다.");
  } finally {
    saving.value = false;
  }
}

onMounted(loadList);
</script>

<template>
  <main class="portal-page">
    <header class="portal-page-header">
      <h2 class="portal-page-title">참고: 신청/등록 폼 (request-form)</h2>
      <p class="portal-page-description">풀페이지 폼과 검증, 저장/취소 액션을 보여주는 견본이다.</p>
    </header>

    <section class="grid gap-4 xl:grid-cols-[1fr_320px]">
      <ElCard class="portal-card" shadow="never">
        <template #header>{{ pageTitle }}</template>

        <ElForm label-position="top" :model="form">
          <div class="grid gap-4 md:grid-cols-2">
            <ElFormItem label="제목" required :error="fieldError(v$.name)">
              <ElInput v-model="form.name" maxlength="100" show-word-limit @blur="v$.name.$touch()" />
            </ElFormItem>
            <ElFormItem label="분류" required :error="fieldError(v$.category)">
              <ElSelect v-model="form.category" placeholder="선택" class="!w-full" @change="v$.category.$touch()">
                <ElOption v-for="opt in categories" :key="opt" :label="opt" :value="opt" />
              </ElSelect>
            </ElFormItem>
            <ElFormItem label="목표일">
              <ElDatePicker v-model="form.targetDate" type="date" value-format="YYYY-MM-DD" placeholder="선택" class="!w-full" />
            </ElFormItem>
            <ElFormItem label="우선순위">
              <ElSelect v-model="form.priority" class="!w-full">
                <ElOption v-for="opt in priorities" :key="opt" :label="opt" :value="opt" />
              </ElSelect>
            </ElFormItem>
          </div>
          <ElFormItem label="설명" :error="fieldError(v$.description)">
            <ElInput
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="1000"
              show-word-limit
              @blur="v$.description.$touch()"
            />
          </ElFormItem>
        </ElForm>

        <div class="mt-4 flex justify-end gap-2">
          <AuthButton :disabled="saving" @click="resetForm">취소</AuthButton>
          <AuthButton type="primary" :disabled="saving" @click="save">저장</AuthButton>
        </div>
      </ElCard>

      <ElCard class="portal-card" shadow="never">
        <template #header>등록된 항목 ({{ items.length }})</template>
        <ul class="space-y-2" v-loading="loading">
          <li
            v-for="item in items"
            :key="item.id"
            class="cursor-pointer rounded-md border border-slate-200 px-3 py-2 text-sm hover:bg-slate-50"
            @click="loadForEdit(item)"
          >
            <div class="font-semibold text-slate-900">{{ item.name }}</div>
            <div class="mt-1 text-xs text-slate-500">{{ item.category }} · {{ item.priority }}</div>
            <div v-if="item.targetDate" class="mt-1 text-xs text-slate-400">목표일: {{ item.targetDate }}</div>
          </li>
          <li v-if="items.length === 0" class="text-center text-sm text-slate-400">등록된 항목이 없습니다.</li>
        </ul>
      </ElCard>
    </section>
  </main>
</template>
