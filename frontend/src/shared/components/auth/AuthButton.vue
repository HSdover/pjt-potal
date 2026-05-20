<script setup lang="ts">
import { computed } from "vue";
import type { Component } from "vue";
import { hasPermission } from "@/shared/auth/permissions";
import PortalButton from "@/shared/components/tags/PortalButton.vue";

const props = defineProps<{
  auth?: string | string[];
  type?: "primary" | "success" | "warning" | "danger" | "info" | "";
  disabled?: boolean;
  icon?: Component;
}>();

const visible = computed(() => hasPermission(props.auth));
</script>

<template>
  <!-- [12. 메뉴, 라우터, 권한 표준] 버튼 권한 제어의 기준 샘플이다. -->
  <PortalButton v-if="visible" :variant="type" :disabled="disabled" :icon="icon">
    <slot />
  </PortalButton>
</template>
