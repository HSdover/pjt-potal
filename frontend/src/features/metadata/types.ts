export type MetadataCatalogItem = {
  metadataId: number;
  datasetName: string;
  datasetType: string;
  sourceName: string;
  storageLocation: string;
  rowCount: number;
  columnsSummary: string;
  description: string;
};

// [7. 목록 조회 표준 계약] 메타데이터 목록 검색조건 타입이다.
export type MetadataSearchFilter = {
  datasetType?: string;
  keyword?: string;
};
