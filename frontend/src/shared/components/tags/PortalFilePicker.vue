<script setup lang="ts">
import { computed, ref, watch } from "vue";
import PortalButton from "./PortalButton.vue";

// 공통 파일 선택: 브라우저 File 객체 배열을 v-model로 상위 폼에 전달한다.
const model = defineModel<File[]>({ default: () => [] });

const emit = defineEmits<{
  change: [files: File[]];
}>();

// accept/multiple은 업무별 업로드 정책에 맞춰 화면에서 제한한다.
const props = withDefaults(defineProps<{
  label?: string;
  buttonText?: string;
  accept?: string;
  multiple?: boolean;
  disabled?: boolean;
  defer?: boolean;
}>(), {
  label: "",
  buttonText: "찾아보기",
  accept: undefined,
  multiple: false,
  disabled: false,
  defer: false,
});

const inputRef = ref<HTMLInputElement | null>(null);
const pendingFiles = ref<File[]>([]);
const displayFiles = computed(() => (props.defer ? pendingFiles.value : model.value));

watch(
  model,
  (files) => {
    if (props.defer) {
      pendingFiles.value = [...files];
    }
  },
  { immediate: true },
);

// 숨겨진 file input을 공통 버튼으로 열어 디자인을 통일한다.
function openPicker() {
  inputRef.value?.click();
}

// 선택된 FileList는 배열로 변환해 화면에서 map/filter 등으로 다루기 쉽게 만든다.
function onChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const files = Array.from(input.files ?? []);

  if (props.defer) {
    pendingFiles.value = files;
    emit("change", files);
    return;
  }

  model.value = files;
  emit("change", files);
}

// defer 모드에서는 저장 버튼에서 이 메서드를 호출할 때만 v-model에 최종 반영한다.
function commit() {
  if (props.defer) {
    model.value = [...pendingFiles.value];
  }

  return model.value;
}

function resetDraft() {
  pendingFiles.value = [...model.value];
  if (inputRef.value) {
    inputRef.value.value = "";
  }
}

// 같은 파일을 다시 선택할 수 있도록 input value까지 같이 초기화한다.
function clear() {
  if (props.defer) {
    pendingFiles.value = [];
  } else {
    model.value = [];
  }

  emit("change", []);

  if (inputRef.value) {
    inputRef.value.value = "";
  }
}

defineExpose({
  commit,
  resetDraft,
  clear,
});
</script>

<template>
  <div class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <div class="portal-file-picker">
      <!-- 실제 파일 선택 기능은 네이티브 input이 담당하고 화면에는 공통 버튼만 노출한다. -->
      <input
        ref="inputRef"
        type="file"
        class="sr-only"
        :accept="accept"
        :multiple="multiple"
        :disabled="disabled"
        @change="onChange"
      />
      <!-- 파일명 표시 영역은 선택 전/후 상태를 한 곳에서 보여준다. -->
      <div class="portal-file-picker-name">
        {{ displayFiles.length > 0 ? displayFiles.map((file) => file.name).join(", ") : "선택된 파일 없음" }}
      </div>
      <PortalButton variant="primary" :disabled="disabled" @click="openPicker">{{ buttonText }}</PortalButton>
      <PortalButton v-if="displayFiles.length > 0" variant="ghost" :disabled="disabled" @click="clear">초기화</PortalButton>
    </div>
  </div>
</template>
