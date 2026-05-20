<script setup lang="ts">
// 공통 다운로드 링크: 브라우저 download 속성으로 파일 저장 동작을 유도한다.
withDefaults(defineProps<{
  href: string;
  fileName?: string;
  label?: string;
}>(), {
  fileName: undefined,
  label: "다운로드",
});
</script>

<template>
  <!-- fileName이 있으면 저장 파일명을 지정하고, 없으면 서버/브라우저 기본 이름을 사용한다. -->
  <a class="portal-download-link" :href="href" :download="fileName">
    <span>{{ label }}</span>
    <span aria-hidden="true">⇩</span>
  </a>
</template>
