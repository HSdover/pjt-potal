<script setup lang="ts">
// 공통 외부 링크: 새 창으로 열고 opener 접근을 막아 보안 기본값을 맞춘다.
withDefaults(defineProps<{
  href: string;
  label?: string;
}>(), {
  label: "",
});
</script>

<template>
  <!-- slot이 있으면 화면별 문구를 쓰고, 없으면 label 또는 href를 기본 표시한다. -->
  <a class="portal-inline-link" :href="href" target="_blank" rel="noopener noreferrer">
    <slot>{{ label || href }}</slot>
    <span aria-hidden="true">↗</span>
  </a>
</template>
