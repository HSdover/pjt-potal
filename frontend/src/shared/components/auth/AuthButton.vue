<script setup lang="ts">
import { computed } from "vue";
import { ElButton } from "element-plus";
import { hasPermission } from "@/shared/auth/permissions";

const props = defineProps<{
  auth?: string | string[];
  type?: "primary" | "success" | "warning" | "danger" | "info" | "";
  disabled?: boolean;
}>();

const visible = computed(() => hasPermission(props.auth));
</script>

<template>
  <!-- [12. 메뉴, 라우터, 권한 표준] 버튼 권한 제어의 기준 샘플이다. -->
  <ElButton v-if="visible" :type="type" :disabled="disabled">
    <slot />
  </ElButton>
</template>
