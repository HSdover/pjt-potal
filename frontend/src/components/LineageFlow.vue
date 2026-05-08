<script setup lang="ts">
import { computed } from "vue";
import { MarkerType, Position, VueFlow } from "@vue-flow/core";
import type { Edge, Node } from "@vue-flow/core";

type LineageLayout = "compact" | "expanded";

type LineageFlowItem = {
  flowId: number;
  sourceName: string;
  sourceType: string;
  targetName: string;
  targetType: string;
  processName: string;
  transformType?: string;
  sortOrder?: number;
};

const props = withDefaults(defineProps<{
  rows: LineageFlowItem[];
  heightClass?: string;
  layout?: LineageLayout;
}>(), {
  layout: "compact",
});

const TYPE_STYLE: Record<string, { background: string; borderColor: string; textColor: string; label: string }> = {
  SOURCE: { background: "#dbeafe", borderColor: "#3b82f6", textColor: "#1e3a8a", label: "외부 출처" },
  FILE: { background: "#fef3c7", borderColor: "#f59e0b", textColor: "#78350f", label: "파일" },
  TABLE: { background: "#dcfce7", borderColor: "#22c55e", textColor: "#14532d", label: "테이블" },
  SCREEN: { background: "#f1f5f9", borderColor: "#64748b", textColor: "#334155", label: "화면" },
};

const orderedRows = computed(() =>
  [...props.rows].sort((a, b) => (a.sortOrder ?? a.flowId) - (b.sortOrder ?? b.flowId)),
);

const fitViewOptions = computed(() => ({
  padding: props.layout === "expanded" ? 0.16 : 0.22,
}));

function nodeY(index: number, total: number) {
  if (props.layout === "compact") {
    return 70;
  }

  if (total <= 3) {
    return 150;
  }

  const expandedPattern = [190, 70, 310, 70, 190, 310];
  return expandedPattern[index % expandedPattern.length];
}

const nodes = computed<Node[]>(() => {
  const typeMap = new Map<string, string>();
  const seen = new Set<string>();
  const ordered: string[] = [];

  for (const row of orderedRows.value) {
    typeMap.set(row.sourceName, row.sourceType);
    typeMap.set(row.targetName, row.targetType);

    if (!seen.has(row.sourceName)) {
      seen.add(row.sourceName);
      ordered.push(row.sourceName);
    }

    if (!seen.has(row.targetName)) {
      seen.add(row.targetName);
      ordered.push(row.targetName);
    }
  }

  const columnGap = props.layout === "expanded" ? 340 : 260;
  const nodeWidth = props.layout === "expanded" ? 250 : 200;

  return ordered.map((name, index) => {
    const type = typeMap.get(name) ?? "SOURCE";
    const s = TYPE_STYLE[type] ?? TYPE_STYLE.SOURCE;

    return {
      id: name,
      sourcePosition: Position.Right,
      targetPosition: Position.Left,
      position: { x: index * columnGap, y: nodeY(index, ordered.length) },
      data: { label: name },
      style: {
        width: `${nodeWidth}px`,
        minHeight: props.layout === "expanded" ? "74px" : "52px",
        background: s.background,
        border: `2px solid ${s.borderColor}`,
        borderRadius: "8px",
        boxShadow: "0 10px 22px rgba(15, 23, 42, 0.08)",
        color: s.textColor,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: props.layout === "expanded" ? "12px 14px" : "10px 8px",
        fontSize: props.layout === "expanded" ? "13px" : "11px",
        fontWeight: 700,
        lineHeight: 1.45,
        textAlign: "center",
      },
    };
  });
});

const edges = computed<Edge[]>(() =>
  orderedRows.value.map((row) => ({
    id: String(row.flowId),
    source: row.sourceName,
    target: row.targetName,
    type: "smoothstep",
    label: row.processName,
    animated: true,
    markerEnd: {
      type: MarkerType.ArrowClosed,
      color: "#64748b",
      width: 18,
      height: 18,
    },
    style: {
      stroke: "#64748b",
      strokeWidth: 2.5,
    },
    labelShowBg: true,
    labelBgStyle: {
      fill: "#ffffff",
      stroke: "#cbd5e1",
      strokeWidth: 1,
    },
    labelBgPadding: [8, 5],
    labelBgBorderRadius: 6,
    labelStyle: {
      fill: "#334155",
      fontSize: props.layout === "expanded" ? "12px" : "10px",
      fontWeight: 700,
    },
  })),
);
</script>

<template>
  <div
    :class="[
      heightClass ?? 'h-96',
      'overflow-hidden rounded-md border border-slate-200 bg-slate-50',
    ]"
  >
    <VueFlow
      :nodes="nodes"
      :edges="edges"
      :fit-view-options="fitViewOptions"
      :min-zoom="0.35"
      :max-zoom="1.3"
      fit-view-on-init
      class="lineage-flow"
    />
  </div>
  <div class="mt-3 flex flex-wrap gap-x-5 gap-y-2 text-xs text-slate-600">
    <span v-for="(s, type) in TYPE_STYLE" :key="type" class="flex items-center gap-2">
      <span
        class="inline-block h-3 w-6 rounded-sm"
        :style="{ background: s.background, border: `2px solid ${s.borderColor}` }"
      />
      {{ s.label }}
    </span>
  </div>
</template>

<style>
.lineage-flow .vue-flow__node-default {
  white-space: normal !important;
  word-break: keep-all;
}

.lineage-flow .vue-flow__edge-textbg {
  filter: drop-shadow(0 4px 10px rgba(15, 23, 42, 0.08));
}

.lineage-flow {
  background-image: radial-gradient(circle, #cbd5e1 1px, transparent 1px);
  background-size: 24px 24px;
}
</style>
