<script setup lang="ts">
// 공통 첨부파일 링크: 업로드된 파일명 표시, 다운로드, 제거 버튼을 함께 제공한다.
withDefaults(defineProps<{
  fileName: string;
  href?: string;
  removable?: boolean;
}>(), {
  href: "#",
  removable: true,
});

// remove는 실제 삭제 API를 호출하지 않고, 상위 화면에 삭제 의도만 전달한다.
defineEmits<{
  remove: [];
}>();
</script>

<template>
  <span class="portal-file-link">
    <!-- href가 없으면 기본값 #으로 표시만 하고, 실제 다운로드 경로는 화면에서 주입한다. -->
    <a :href="href" download>{{ fileName }}</a>
    <button v-if="removable" type="button" aria-label="첨부파일 삭제" @click="$emit('remove')">×</button>
  </span>
</template>
