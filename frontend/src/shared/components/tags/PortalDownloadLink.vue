<script setup lang="ts">
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
  <a class="portal-download-link" :href="href" :download="fileName">
    <span>{{ label }}</span>
    <span aria-hidden="true">⇩</span>
  </a>
</template>
