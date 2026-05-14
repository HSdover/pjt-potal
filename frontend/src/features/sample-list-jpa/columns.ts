import type { ColDef } from "ag-grid-community";
import type { SampleListJpaItem } from "./types";

export const columns: ColDef<SampleListJpaItem>[] = [
  { field: "id", headerName: "ID", width: 100 },
  { field: "name", headerName: "이름", minWidth: 180, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 240, flex: 1 },
];
