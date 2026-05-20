<script setup lang="ts">
import { computed } from "vue";

// 공통 상태 태그: 처리 상태, 증감 표시, SLA/DLQ 같은 작은 배지에 사용한다.
const props = withDefaults(defineProps<{
  type?: "primary" | "success" | "warning" | "danger" | "info" | "neutral" | string;
  size?: "default" | "small";
  effect?: "plain" | "dark" | "light";
}>(), {
  type: "info",
  size: "default",
  effect: "plain",
});

// type/effect/size를 CSS 클래스 조합으로 변환해 화면에서는 의미만 넘기도록 한다.
const classes = computed(() => [
  `portal-tag-${props.type || "info"}`,
  `portal-tag-${props.effect}`,
  props.size === "small" ? "portal-tag-small" : "",
]);
</script>

<template>
  <!-- slot으로 표시 문구를 받아 상태값, 건수, 추세 등 다양한 짧은 텍스트를 담는다. -->
  <span class="portal-tag" :class="classes">
    <slot />
  </span>
</template>
