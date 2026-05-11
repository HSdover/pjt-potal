import type { ColDef } from "ag-grid-community";
import type { LineageFlowItem } from "./types";

// [6. 업무 화면 작성 공식] 리니지 단계 그리드 컬럼 정의를 분리한다.
export const lineageColumns: ColDef<LineageFlowItem>[] = [
  { field: "sortOrder", headerName: "#", width: 70 },
  { field: "processName", headerName: "처리명", minWidth: 150, flex: 0.8 },
  { field: "transformType", headerName: "유형", width: 120 },
  { field: "description", headerName: "설명", minWidth: 260, flex: 1.2 },
];
