import type { ColDef } from "ag-grid-community";
import type { SampleListItem } from "./types";

export const columns: ColDef<SampleListItem>[] = [
  { field: "id", headerName: "ID", width: 100 },
  { field: "name", headerName: "명칭", minWidth: 180, flex: 1 },
  { field: "description", headerName: "설명", minWidth: 240, flex: 1 },
];
