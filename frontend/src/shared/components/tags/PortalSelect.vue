<script setup lang="ts">
// 공통 셀렉트 옵션 구조: 화면은 label/value만 넘기고 렌더링 방식은 컴포넌트가 담당한다.
export type PortalSelectOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

// 선택값은 문자열 코드 기준으로 관리한다. 필요하면 화면에서 number/date로 변환한다.
const model = defineModel<string>({ default: "" });

// change는 Vuelidate $touch나 즉시 조회 같은 화면별 후처리를 연결하기 위한 이벤트다.
defineEmits<{
  change: [value: string];
}>();

withDefaults(defineProps<{
  id?: string;
  label?: string;
  placeholder?: string;
  disabled?: boolean;
  options: PortalSelectOption[];
  error?: string;
  helpText?: string;
}>(), {
  id: undefined,
  label: "",
  placeholder: "선택",
  disabled: false,
  error: "",
  helpText: "",
});
</script>

<template>
  <label class="portal-field">
    <span v-if="label" class="portal-field-label">{{ label }}</span>
    <!-- placeholder는 빈 값 선택을 막는 안내 옵션으로만 사용한다. -->
    <select
      :id="id"
      v-model="model"
      class="portal-control portal-select"
      :disabled="disabled"
      @change="$emit('change', model)"
    >
      <option value="" disabled>{{ placeholder }}</option>
      <!-- disabled 옵션까지 같은 구조로 내려받아 화면별 권한/상태 제어에 대응한다. -->
      <option v-for="option in options" :key="option.value" :value="option.value" :disabled="option.disabled">
        {{ option.label }}
      </option>
    </select>
    <span v-if="error" class="portal-field-error">{{ error }}</span>
    <span v-else-if="helpText" class="portal-field-help">{{ helpText }}</span>
  </label>
</template>
