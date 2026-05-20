<script setup lang="ts">
// 공통 멀티라인 입력: 설명, 검토 의견, 반려 사유처럼 긴 텍스트 입력에 사용한다.
const model = defineModel<string>({ default: "" });

// blur 이벤트는 폼 검증 타이밍을 화면에서 제어할 수 있게 전달한다.
defineEmits<{
  blur: [event: FocusEvent];
}>();

// rows/maxlength/showCount로 textarea의 높이와 입력 제한 표시를 표준화한다.
withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  rows?: number;
  maxlength?: number;
  showCount?: boolean;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "",
  disabled: false,
  rows: 4,
  maxlength: undefined,
  showCount: false,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <!-- textarea도 단일 model string을 사용해 폼 payload 생성 시 trim/검증을 단순화한다. -->
    <textarea
      :id="id"
      v-model="model"
      class="portal-control portal-textarea"
      :placeholder="placeholder"
      :disabled="disabled"
      :rows="rows"
      :maxlength="maxlength"
      @blur="$emit('blur', $event)"
    />
    <!-- showCount는 maxlength가 있을 때만 의미가 있으므로 두 조건을 같이 확인한다. -->
    <span v-if="showCount && maxlength" class="portal-field-help">{{ model.length }} / {{ maxlength }}</span>
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
