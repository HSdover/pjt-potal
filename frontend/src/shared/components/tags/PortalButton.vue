<script setup lang="ts">
import type { Component } from "vue";

// 공통 버튼 태그: 일반 버튼, 권한 버튼, 파일 선택 버튼이 같은 스타일/상태 규칙을 공유한다.
withDefaults(defineProps<{
  type?: "button" | "submit" | "reset";
  variant?: "primary" | "secondary" | "ghost" | "danger" | "success" | "warning" | "info" | "";
  disabled?: boolean;
  icon?: Component;
}>(), {
  type: "button",
  variant: "secondary",
  disabled: false,
  icon: undefined,
});

// 상위 화면에서는 네이티브 click 대신 이 이벤트만 바라보면 된다.
defineEmits<{
  click: [event: MouseEvent];
}>();
</script>

<template>
  <!-- variant는 Element Plus type을 대체하는 공통 버튼 색상 체계다. -->
  <button
    class="portal-tag-button"
    :class="variant ? `portal-tag-button-${variant}` : 'portal-tag-button-secondary'"
    :type="type"
    :disabled="disabled"
    @click="$emit('click', $event)"
  >
    <!-- lucide/Element Plus 아이콘 컴포넌트를 버튼 앞에 재사용한다. -->
    <component :is="icon" v-if="icon" class="portal-tag-button-icon" />
    <slot />
  </button>
</template>
