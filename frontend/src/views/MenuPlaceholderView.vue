<script setup lang="ts">
import { computed } from "vue";
import { useRoute } from "vue-router";

const route = useRoute();
const title = computed(() => String(route.meta.title ?? "준비중"));
</script>

<template>
  <main class="portal-page">
    <section class="portal-page-header">
      <h1 class="portal-page-title">{{ title }}</h1>
      <p class="portal-page-description">메뉴 구성을 위한 준비중 화면입니다.</p>
    </section>
  </main>
</template>
