# API And List Contract

작성일: 2026-05-12

## 공통 HTTP 클라이언트

프론트 화면은 `fetch()`를 직접 호출하지 않고 `shared/api/http.ts`를 사용한다.

```ts
import { http } from "@/shared/api/http";
```

목록 조회처럼 검색 조건, 정렬, 페이징이 함께 이동하는 API는 `POST /search`와 JSON body를 기준으로 한다.

## 목록 요청

```ts
export type ListRequest<TFilter = Record<string, unknown>> = {
  pageNo: number;
  pageSize: number;
  sort?: Array<{
    field: string;
    direction: "asc" | "desc";
  }>;
  filters: TFilter;
};
```

## 목록 응답

```ts
export type ListResponse<TRow> = {
  rows: TRow[];
  totalCount: number;
  pageNo: number;
  pageSize: number;
};
```

## 화면 규칙

- 조회 버튼은 `pageNo`를 1로 되돌린 뒤 호출한다.
- 페이지, 페이지 크기, 정렬 변경은 서버 API를 다시 호출한다.
- `totalCount`, `pageNo`, `pageSize`는 서버 응답을 기준으로 보정한다.
- API 실패는 화면에서 `handleApiError(error, fallbackMessage)`로 처리한다.
- 백엔드 표준 오류 응답은 `shared/api/http.ts`에서 `ApiError`로 변환되며, `requestId`가 있으면 사용자 메시지에 요청ID가 포함된다.
