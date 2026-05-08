import type { ColDef } from "ag-grid-community";

// SourceSampleView 전용 타입 영역
// 기준: 원천 샘플 화면에서만 사용하는 응답 타입은 화면 대응 .ts에 둔다.
export type SourceSampleItem = {
  sampleId: number;
  institutionName: string;
  institutionType: string;
  sidoName: string;
  sigunguName: string;
  specialtyName: string;
  openedDate: string;
};

// SourceSampleView 전용 API 영역
// 기준: 화면 전용 데이터는 store를 거치지 않고 이 화면의 .ts에서 직접 조회한다.
export async function fetchSourceSample() {
  const response = await fetch("/api/catalog/source-sample");
  if (!response.ok) {
    throw new Error("원천 샘플 데이터 조회에 실패했습니다.");
  }

  return await response.json() as SourceSampleItem[];
}

// SourceSampleView 전용 그리드 기본 설정 영역
// 기준: 화면별 그리드 설정은 화면명과 같은 .ts에 두어 복사 템플릿으로 쓰기 쉽게 한다.
export const defaultColDef: ColDef = {
  sortable: true,
  filter: true,
  resizable: true,
};

// SourceSampleView 전용 컬럼 정의 영역
// 기준: 원천 샘플 데이터 컬럼은 화면 전용 설정이므로 대응 .ts에서 관리한다.
export const columnDefs: ColDef<SourceSampleItem>[] = [
  { field: "sampleId", headerName: "#", width: 80 },
  { field: "institutionName", headerName: "요양기관명", minWidth: 190, flex: 1 },
  { field: "institutionType", headerName: "요양종별", width: 130 },
  { field: "sidoName", headerName: "시도", width: 110 },
  { field: "sigunguName", headerName: "시군구", width: 120 },
  { field: "specialtyName", headerName: "표시과목", width: 130 },
  { field: "openedDate", headerName: "개설일자", width: 130 },
];
