import type { ColDef } from "ag-grid-community";

// LineageView 전용 타입 영역
// 기준: 리니지 화면과 그래프에서만 사용하는 응답 타입은 화면 대응 .ts에서 관리한다.
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

// LineageView 전용 API 영역
// 기준: 리니지 화면에서만 필요한 데이터 호출은 store 없이 이 화면의 .ts에 둔다.
export async function fetchLineage() {
  const response = await fetch("/api/catalog/lineage");
  if (!response.ok) {
    throw new Error("데이터 리니지 조회에 실패했습니다.");
  }

  return await response.json() as LineageFlowItem[];
}

// LineageView 전용 그리드 기본 설정 영역
// 기준: 리니지 처리 단계 그리드에서만 쓰는 기본 동작은 대응 .ts에 둔다.
export const defaultColDef: ColDef = {
  sortable: true,
  filter: true,
  resizable: true,
};

// LineageView 전용 컬럼 정의 영역
// 기준: 그래프 옆 처리 단계 표의 컬럼 구성을 .vue 밖으로 빼서 화면 구조를 읽기 쉽게 한다.
export const lineageColumnDefs: ColDef<LineageFlowItem>[] = [
  { field: "sortOrder", headerName: "#", width: 70 },
  { field: "processName", headerName: "처리명", minWidth: 150, flex: 0.8 },
  { field: "transformType", headerName: "유형", width: 120 },
  { field: "description", headerName: "설명", minWidth: 260, flex: 1.2 },
];
