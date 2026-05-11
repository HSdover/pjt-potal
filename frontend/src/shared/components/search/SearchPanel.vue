<template>
  <!-- [10. 검색 영역 표준화] 조회/초기화/총 건수 위치를 화면마다 동일하게 유지한다. -->
  <section class="mb-3 rounded-md border border-slate-200 bg-white p-3">
    <div class="flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
      <div class="flex flex-col gap-2 md:flex-row md:flex-wrap md:items-end">
        <slot />
      </div>
      <div class="flex items-center justify-end gap-2">
        <slot name="actions" />
      </div>
    </div>
    <div v-if="$slots.summary" class="mt-2 text-xs font-semibold text-slate-500">
      <slot name="summary" />
    </div>
  </section>
</template>
