<script setup lang="ts">
withDefaults(defineProps<{
  href: string;
  label?: string;
}>(), {
  label: "",
});
</script>

<template>
  <a class="portal-inline-link" :href="href" target="_blank" rel="noopener noreferrer">
    <slot>{{ label || href }}</slot>
    <span aria-hidden="true">↗</span>
  </a>
</template>
