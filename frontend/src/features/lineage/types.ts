export type LineageFlowItem = {
  flowId: number;
  sourceName: string;
  sourceType: string;
  targetName: string;
  targetType: string;
  processName: string;
  transformType: string;
  description: string;
  sortOrder: number;
};
