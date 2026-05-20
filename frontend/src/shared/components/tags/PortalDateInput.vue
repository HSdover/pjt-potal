<script setup lang="ts">
// 공통 날짜 입력: YYYY-MM-DD 문자열을 그대로 주고받아 REST payload와 맞춘다.
const model = defineModel<string>({ default: "" });

// blur/change를 모두 노출해 검증과 값 변경 후처리를 분리해서 쓸 수 있게 한다.
defineEmits<{
  blur: [event: FocusEvent];
  change: [value: string];
}>();

// min/max는 화면별 업무 기간 제한이 필요한 경우만 선택적으로 넘긴다.
withDefaults(defineProps<{
  id?: string;
  label?: string;
  disabled?: boolean;
  min?: string;
  max?: string;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  disabled: false,
  min: undefined,
  max: undefined,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <!-- 네이티브 date input을 사용해 브라우저 기본 달력과 접근성을 그대로 활용한다. -->
    <input
      :id="id"
      v-model="model"
      class="portal-control portal-date-control"
      type="date"
      :disabled="disabled"
      :min="min"
      :max="max"
      @blur="$emit('blur', $event)"
      @change="$emit('change', model)"
    />
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
