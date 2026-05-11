import type { ColDef } from "ag-grid-community";
import type { SourceSampleItem } from "./types";

// [6. 업무 화면 작성 공식] 업무별 컬럼 정의를 화면에서 분리한다.
export const sourceSampleColumns: ColDef<SourceSampleItem>[] = [
  { field: "sampleId", headerName: "#", width: 90 },
  { field: "institutionName", headerName: "요양기관명", minWidth: 190, flex: 1 },
  { field: "institutionType", headerName: "요양종별", width: 140 },
  { field: "sidoName", headerName: "시도", width: 120 },
  { field: "sigunguName", headerName: "시군구", width: 130 },
  { field: "specialtyName", headerName: "표시과목", width: 140 },
  { field: "openedDate", headerName: "개설일자", width: 140 },
];
