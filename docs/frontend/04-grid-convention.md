# Grid Convention

작성일: 2026-05-12

## 기본 사용

목록 화면은 `BaseGrid.vue`를 사용한다.

```vue
<BaseGrid
  :rows="rows"
  :columns="columns"
  :loading="loading"
  :total-count="totalCount"
  :page-no="request.pageNo"
  :page-size="request.pageSize"
  @page-change="onPageChange"
  @page-size-change="onPageSizeChange"
  @sort-change="onSortChange"
/>
```

## 컬럼

컬럼은 feature의 `columns.ts`에 둔다.

```ts
import type { ColDef } from "ag-grid-community";
import type { MetadataItem } from "./types";

export const metadataColumns: ColDef<MetadataItem>[] = [
  { field: "datasetName", headerName: "데이터셋명", minWidth: 220, flex: 1 },
];
```

## 규칙

- 그리드는 API 경로를 알지 않는다.
- 정렬 이벤트는 `ListSort[]`로 변환해 화면에 전달한다.
- 행 클릭이 필요한 화면만 `@row-click`을 사용한다.
- 업무별 formatter나 renderer가 반복되면 `shared/components/grid`로 올린다.
