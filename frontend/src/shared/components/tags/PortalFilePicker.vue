<script setup lang="ts">
import { ref } from "vue";
import PortalButton from "./PortalButton.vue";

const model = defineModel<File[]>({ default: () => [] });

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

function openPicker() {
  inputRef.value?.click();
}

function onChange(event: Event) {
  const input = event.target as HTMLInputElement;
  model.value = Array.from(input.files ?? []);
}

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
      <input
        ref="inputRef"
        type="file"
        class="sr-only"
        :accept="accept"
        :multiple="multiple"
        :disabled="disabled"
        @change="onChange"
      />
      <div class="portal-file-picker-name">
        {{ model.length > 0 ? model.map((file) => file.name).join(", ") : "선택된 파일 없음" }}
      </div>
      <PortalButton variant="primary" :disabled="disabled" @click="openPicker">{{ buttonText }}</PortalButton>
      <PortalButton v-if="model.length > 0" variant="ghost" :disabled="disabled" @click="clear">초기화</PortalButton>
    </div>
  </div>
</template>
