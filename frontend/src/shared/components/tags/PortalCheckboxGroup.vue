<script setup lang="ts">
// 공통 체크박스 옵션 구조: 다중 선택 가능한 코드/라벨 목록을 표현한다.
export type PortalCheckOption = {
  label: string;
  value: string;
  disabled?: boolean;
};

// 체크박스 그룹은 선택된 value 목록을 string[]로 관리한다.
const model = defineModel<string[]>({ default: () => [] });

// inline=false면 세로형 그룹으로 바뀌어 필터/폼 레이아웃에 모두 대응한다.
withDefaults(defineProps<{
  label?: string;
  options: PortalCheckOption[];
  disabled?: boolean;
  inline?: boolean;
}>(), {
  label: "",
  disabled: false,
  inline: true,
});

// 체크 시 중복을 제거하고, 해제 시 해당 value만 제외해 v-model 배열을 갱신한다.
function toggle(value: string, checked: boolean) {
  if (checked) {
    model.value = Array.from(new Set([...model.value, value]));
    return;
  }
  model.value = model.value.filter((item) => item !== value);
}
</script>

<template>
  <fieldset class="portal-field">
    <legend v-if="label" class="portal-field-label">{{ label }}</legend>
    <!-- fieldset/legend를 사용해 스크린리더가 그룹 라벨을 함께 읽을 수 있게 한다. -->
    <div class="portal-choice-group" :class="{ 'portal-choice-group-stacked': !inline }">
      <label v-for="option in options" :key="option.value" class="portal-choice">
        <input
          type="checkbox"
          :checked="model.includes(option.value)"
          :disabled="disabled || option.disabled"
          @change="toggle(option.value, ($event.target as HTMLInputElement).checked)"
        />
        <span>{{ option.label }}</span>
      </label>
    </div>
  </fieldset>
</template>
