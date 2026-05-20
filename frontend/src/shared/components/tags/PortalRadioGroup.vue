<script setup lang="ts">
// 공통 라디오 옵션 구조: 한 그룹 안에서 하나의 value만 선택한다.
export type PortalRadioOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

// 라디오 그룹은 선택된 단일 value를 string으로 관리한다.
const model = defineModel<string>({ default: "" });

// name은 브라우저가 같은 라디오 그룹으로 인식하기 위한 필수 값이다.
withDefaults(defineProps<{
  label?: string;
  name: string;
  options: PortalRadioOption[];
  disabled?: boolean;
  inline?: boolean;
}>(), {
  label: "",
  disabled: false,
  inline: true,
});
</script>

<template>
  <fieldset class="portal-field">
    <legend v-if="label" class="portal-field-label">{{ label }}</legend>
    <!-- inline 옵션으로 가로형/세로형 라디오 배치를 화면에서 선택한다. -->
    <div class="portal-choice-group" :class="{ 'portal-choice-group-stacked': !inline }">
      <label v-for="option in options" :key="option.value" class="portal-choice">
        <input
          v-model="model"
          type="radio"
          :name="name"
          :value="option.value"
          :disabled="disabled || option.disabled"
        />
        <span>{{ option.label }}</span>
      </label>
    </div>
  </fieldset>
</template>
