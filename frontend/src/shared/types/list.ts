// [7. 목록 조회 표준 계약] SI 목록 화면의 서버 페이징/정렬/필터 공통 요청 타입이다.
export type SortDirection = "asc" | "desc";

export type ListSort = {
  field: string;
  direction: SortDirection;
};

export type ListRequest<TFilter = Record<string, unknown>> = {
  pageNo: number;
  pageSize: number;
  sort?: ListSort[];
  filters: TFilter;
};

// [7. 목록 조회 표준 계약] 백엔드 응답이 달라도 화면에는 이 형태로 전달한다.
export type ListResponse<TRow> = {
  rows: TRow[];
  totalCount: number;
  pageNo: number;
  pageSize: number;
};
