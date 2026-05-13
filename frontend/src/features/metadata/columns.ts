import type { ColDef } from "ag-grid-community";
import type { MetadataItem } from "./types";

// [6. 업무 화면 작성 공식] 그리드 컬럼은 화면 SFC에서 분리해 업무별 columns.ts에 둔다.
export const metadataColumns: ColDef<MetadataItem>[] = [
  { field: "datasetName", headerName: "데이터명", minWidth: 220, flex: 1.2 },
  { field: "datasetType", headerName: "유형", width: 140 },
  { field: "sourceName", headerName: "출처", width: 150 },
  { field: "rowCount", headerName: "건수", width: 120, type: "rightAligned" },
  { field: "storageLocation", headerName: "위치", minWidth: 240, flex: 1 },
  { field: "columnsSummary", headerName: "컬럼 목록", minWidth: 260, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 220, flex: 1 },
];
