<script setup lang="ts">
import { ref } from "vue";
import PortalButton from "./PortalButton.vue";

// 공통 파일 선택: 브라우저 File 객체 배열을 v-model로 상위 폼에 전달한다.
const model = defineModel<File[]>({ default: () => [] });

// accept/multiple은 업무별 업로드 정책에 맞춰 화면에서 제한한다.
withDefaults(defineProps<{
  label?: string;
  buttonText?: string;
  accept?: string;
  multiple?: boolean;
  disabled?: boolean;
}>(), {
  label: "",
  buttonText: "찾아보기",
  accept: undefined,
  multiple: false,
  disabled: false,
});

const inputRef = ref<HTMLInputElement | null>(null);

// 숨겨진 file input을 공통 버튼으로 열어 디자인을 통일한다.
function openPicker() {
  inputRef.value?.click();
}

// 선택된 FileList는 배열로 변환해 화면에서 map/filter 등으로 다루기 쉽게 만든다.
function onChange(event: Event) {
  const input = event.target as HTMLInputElement;
  model.value = Array.from(input.files ?? []);
}

// 같은 파일을 다시 선택할 수 있도록 input value까지 같이 초기화한다.
function clear() {
  model.value = [];
  if (inputRef.value) {
    inputRef.value.value = "";
  }
}
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
        {{ model.length > 0 ? model.map((file) => file.name).join(", ") : "선택된 파일 없음" }}
      </div>
      <PortalButton variant="primary" :disabled="disabled" @click="openPicker">{{ buttonText }}</PortalButton>
      <PortalButton v-if="model.length > 0" variant="ghost" :disabled="disabled" @click="clear">초기화</PortalButton>
    </div>
  </div>
</template>
