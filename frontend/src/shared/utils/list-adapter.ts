import type { ListRequest, ListResponse } from "@/shared/types/list";

export type ListAdapterOptions<TRow extends Record<string, unknown>> = {
  searchFields?: Array<keyof TRow & string>;
};

function normalize(value: unknown) {
  return String(value ?? "").toLowerCase();
}

function matchesKeyword<TRow extends Record<string, unknown>>(
  row: TRow,
  keyword: string,
  searchFields?: Array<keyof TRow & string>,
) {
  if (!keyword) {
    return true;
  }

  const lowered = keyword.toLowerCase();
  const values = searchFields && searchFields.length > 0
    ? searchFields.map((field) => row[field])
    : Object.values(row);

  return values.some((value) => normalize(value).includes(lowered));
}

// [7. 목록 조회 표준 계약] 기존 샘플 전체조회 API를 표준 목록 응답으로 변환한다.
export function toListResponse<TRow extends Record<string, unknown>, TFilter extends { keyword?: string }>(
  sourceRows: TRow[],
  request: ListRequest<TFilter>,
  options: ListAdapterOptions<TRow> = {},
): ListResponse<TRow> {
  const keyword = request.filters.keyword?.trim() ?? "";
  const filtered = sourceRows.filter((row) => matchesKeyword(row, keyword, options.searchFields));
  const sorted = [...filtered];

  request.sort?.slice().reverse().forEach((sort) => {
    sorted.sort((left, right) => {
      const leftValue = normalize(left[sort.field]);
      const rightValue = normalize(right[sort.field]);
      const result = leftValue.localeCompare(rightValue, "ko-KR", { numeric: true });
      return sort.direction === "asc" ? result : -result;
    });
  });

  const start = Math.max(0, (request.pageNo - 1) * request.pageSize);
  const end = start + request.pageSize;

  return {
    rows: sorted.slice(start, end),
    totalCount: sorted.length,
    pageNo: request.pageNo,
    pageSize: request.pageSize,
  };
}
