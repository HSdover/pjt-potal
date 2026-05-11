import { fetchLineage } from "@/features/lineage/api";
import type { LineageFlowItem } from "@/features/lineage/types";
import { fetchMetadata } from "@/features/metadata/api";
import type { MetadataCatalogItem } from "@/features/metadata/types";
import { fetchSourceSample } from "@/features/source-sample/api";
import type { SourceSampleItem } from "@/features/source-sample/types";

export type DashboardData = {
  metadata: MetadataCatalogItem[];
  lineage: LineageFlowItem[];
  sourceSample: SourceSampleItem[];
};

export async function fetchDashboardData(): Promise<DashboardData> {
  const [metadata, lineage, sourceSample] = await Promise.all([
    fetchMetadata(),
    fetchLineage(),
    fetchSourceSample(),
  ]);

  return { metadata, lineage, sourceSample };
}

export function formatNumber(value: number) {
  return new Intl.NumberFormat("ko-KR").format(value);
}

export function getDatasetTypeCounts(metadata: MetadataCatalogItem[]) {
  return metadata.reduce<Record<string, number>>((acc, item) => {
    acc[item.datasetType] = (acc[item.datasetType] ?? 0) + 1;
    return acc;
  }, {});
}
