<script setup lang="ts">
// 공통 텍스트 입력: 검색어, 폼 단일 라인 입력, 코드/명칭 입력에 사용한다.
const model = defineModel<string>({ default: "" });

// blur는 Vuelidate의 $touch 같은 검증 트리거와 연결하기 위한 이벤트다.
defineEmits<{
  blur: [event: FocusEvent];
}>();

// clearable/showCount는 Element Plus 입력에서 자주 쓰던 기능을 공통 컴포넌트로 옮긴 옵션이다.
withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  maxlength?: number;
  clearable?: boolean;
  showCount?: boolean;
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "",
  disabled: false,
  maxlength: undefined,
  clearable: false,
  showCount: false,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <span class="portal-input-wrap">
      <!-- model은 항상 string으로 유지해서 화면별 폼 상태와 양방향 연결한다. -->
      <input
        :id="id"
        v-model="model"
        class="portal-control"
        :class="{ 'portal-control-clearable': clearable }"
        type="text"
        :placeholder="placeholder"
        :disabled="disabled"
        :maxlength="maxlength"
        @blur="$emit('blur', $event)"
      />
      <!-- clearable이 켜져 있고 값이 있을 때만 입력값 삭제 버튼을 노출한다. -->
      <button
        v-if="clearable && model"
        type="button"
        class="portal-input-clear"
        :disabled="disabled"
        aria-label="입력값 삭제"
        @click="model = ''"
      >
        ×
      </button>
    </span>
    <!-- maxlength가 있는 필드만 현재 글자수를 표시한다. -->
    <span v-if="showCount && maxlength" class="portal-field-help">{{ model.length }} / {{ maxlength }}</span>
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
