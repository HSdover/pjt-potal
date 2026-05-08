<script setup lang="ts">
/**
 * GridPageLayout
 * -------------------------------------------------------
 * 그리드가 중심인 화면의 공통 레이아웃 컴포넌트.
 * 새로운 그리드 화면을 추가할 때 이 컴포넌트를 기반으로 제작한다.
 *
 * [슬롯 구성]
 *   toolbar (선택) — 그리드 상단 도구 영역. 검색창·필터·버튼 등을 주입한다.
 *   default (필수) — 그리드 본체 영역. AgGridVue 컴포넌트를 직접 배치한다.
 *   detail  (선택) — 그리드 하단 상세 패널. 행 선택 시 표시할 정보를 주입한다.
 *
 * [사용 예]
 *   <GridPageLayout title="화면 제목" description="부제목">
 *     <template #toolbar> ... </template>
 *     <div class="ag-theme-quartz h-[480px] w-full">
 *       <AgGridVue ... />
 *     </div>
 *     <template #detail> ... </template>
 *   </GridPageLayout>
 */
withDefaults(defineProps<{
  /** 페이지 상단에 표시할 제목 */
  title: string;
  /** 제목 아래에 표시할 부제목 또는 설명 (생략 가능) */
  description?: string;
}>(), {
  description: "",
});
</script>

<template>
  <!-- 전체 페이지 래퍼: 화면 최대 너비 제한 및 기본 여백 -->
  <main class="mx-auto max-w-screen-2xl px-6 py-6">

    <!-- ① 페이지 헤더: 제목과 부제목 표시 영역 -->
    <header class="mb-4">
      <h2 class="text-xl font-bold text-slate-800">{{ title }}</h2>
      <p v-if="description" class="mt-1 text-sm text-slate-500">{{ description }}</p>
    </header>

    <!-- ② 툴바 슬롯: 검색창·필터·엑셀 내보내기 등 그리드 조작 도구 -->
    <!--    슬롯이 주입된 경우에만 렌더링 -->
    <div v-if="$slots.toolbar" class="mb-3">
      <slot name="toolbar" />
    </div>

    <!-- ③ 그리드 카드: 화면의 핵심 영역 -->
    <!--    기본 슬롯(default)에 AgGridVue 컴포넌트를 배치한다 -->
    <ElCard shadow="never">
      <slot />
    </ElCard>

    <!-- ④ 상세 패널 슬롯: 행 클릭 시 그리드 아래에 표시할 상세 정보 -->
    <!--    슬롯이 주입된 경우에만 렌더링 -->
    <div v-if="$slots.detail" class="mt-4">
      <slot name="detail" />
    </div>

  </main>
</template>
