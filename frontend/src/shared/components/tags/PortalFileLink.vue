<script setup lang="ts">
withDefaults(defineProps<{
  fileName: string;
  href?: string;
  removable?: boolean;
}>(), {
  href: "#",
  removable: true,
});

defineEmits<{
  remove: [];
}>();
</script>

<template>
  <span class="portal-file-link">
    <a :href="href" download>{{ fileName }}</a>
    <button v-if="removable" type="button" aria-label="첨부파일 삭제" @click="$emit('remove')">×</button>
  </span>
</template>
