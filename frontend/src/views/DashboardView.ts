import type { LineageFlowItem } from "./LineageView";
import type { MetadataCatalogItem } from "./MetadataView";
import type { SourceSampleItem } from "./SourceSampleView";
import { fetchLineage } from "./LineageView";
import { fetchMetadata } from "./MetadataView";
import { fetchSourceSample } from "./SourceSampleView";

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
