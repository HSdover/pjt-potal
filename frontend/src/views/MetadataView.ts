import type { ColDef, RowClickedEvent } from "ag-grid-community";

// MetadataView 전용 타입 영역
// 기준: 화면 간 공유가 필요 없는 응답 타입은 store가 아니라 해당 화면의 .ts에 둔다.
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

// MetadataView 전용 API 영역
// 기준: 이 화면에서만 필요한 데이터 호출은 해당 화면의 .ts에서 직접 처리한다.
export async function fetchMetadata() {
  const response = await fetch("/api/catalog/metadata");
  if (!response.ok) {
    throw new Error("메타데이터 조회에 실패했습니다.");
  }

  return await response.json() as MetadataCatalogItem[];
}

// MetadataView 전용 그리드 기본 설정 영역
// 기준: 이 화면의 AG Grid 동작 기본값은 대응 .ts에 두고 .vue에서는 바인딩만 한다.
export const defaultColDef: ColDef = {
  sortable: true,
  filter: true,
  resizable: true,
};

// MetadataView 전용 컬럼 정의 영역
// 기준: 컬럼 구성은 화면의 핵심 설정이지만 템플릿을 길게 만들지 않도록 .ts로 분리한다.
export const columnDefs: ColDef<MetadataCatalogItem>[] = [
  { field: "datasetName", headerName: "데이터명", minWidth: 220, flex: 1.2 },
  { field: "datasetType", headerName: "유형", width: 120 },
  { field: "sourceName", headerName: "출처", width: 140 },
  { field: "rowCount", headerName: "건수", width: 110, type: "rightAligned" },
  { field: "storageLocation", headerName: "위치", minWidth: 240, flex: 1 },
  { field: "columnsSummary", headerName: "컬럼 목록", minWidth: 260, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 220, flex: 1 },
];

// MetadataView 전용 선택 처리 영역
// 기준: 여러 화면에서 공유하지 않는 행 선택 규칙은 화면 대응 .ts에 helper로 둔다.
export function toggleSelectedMetadata(
  current: MetadataCatalogItem | null,
  event: RowClickedEvent<MetadataCatalogItem>,
) {
  if (!event.data) {
    return current;
  }

  return current?.metadataId === event.data.metadataId ? null : event.data;
}
