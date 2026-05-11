# SI 프로젝트용 프론트엔드 템플릿화 기획

작성일: 2026-05-11

## 1. 배경

이 프로젝트는 공공 의료기관 데이터의 메타데이터 카탈로그, 원천 샘플 조회, 데이터 리니지 시각화를 제공하는 데이터 거버넌스 포털 샘플이다.

SI 프로젝트에서는 투입 개발자가 짧은 시간 안에 구조를 이해하고 같은 방식으로 업무 화면을 개발해야 한다. 따라서 프론트엔드는 단순 샘플 화면이 아니라, 복사하고 확장할 수 있는 표준 업무 화면 템플릿으로 정리되어야 한다.

## 2. 목표

- 신규 개발자가 1~2일 안에 화면 개발 구조를 이해할 수 있게 한다.
- API 호출, 검색조건, 그리드, 상세영역, 로딩, 에러 처리를 화면마다 같은 방식으로 구현하게 한다.
- 업무 화면 개발 시 개발자마다 다른 구조와 UI 패턴이 생기지 않게 한다.
- 현재 샘플 화면을 실무 화면 개발의 기준 구현체로 만든다.
- 운영 환경, 백엔드 연동, 배포 방식이 바뀌어도 프론트엔드 구조가 흔들리지 않게 한다.

## 3. 기본 방향

프론트엔드는 "예쁜 샘플"보다 "업무 화면을 빠르게 양산하는 표준"을 우선한다.

현재 Vue 3, TypeScript, Vite, Element Plus, AG Grid, Vue Router 기반은 유지한다. 다만 화면별로 흩어진 API 호출, 타입, 컬럼 정의, 레이아웃을 공통 규칙으로 재배치한다.

핵심 방향은 다음과 같다.

- 공통 영역과 업무 영역을 명확히 분리한다.
- 반복되는 화면 유형을 템플릿화한다.
- 업무 화면은 정해진 파일 구조만 따라 만들게 한다.
- 공통 API 클라이언트와 공통 그리드 래퍼를 둔다.
- 메뉴와 라우터는 설정 기반으로 관리한다.
- 문서보다 생성기와 기준 샘플 화면을 우선한다.

## 4. 권장 디렉터리 구조

```text
src/
  app/
    router/
    layouts/
    providers/
  shared/
    api/
    components/
    composables/
    constants/
    styles/
    types/
    utils/
  features/
    metadata/
      api.ts
      types.ts
      columns.ts
      pages/
      components/
    source-sample/
    lineage/
  tools/
    generator/
      templates/
        search-grid-page/
        search-grid-detail-page/
        form-page/
```

`app`은 애플리케이션 구동 영역, `shared`는 업무 도메인에 종속되지 않는 공통 영역, `features`는 실제 업무 기능 영역으로 사용한다.

런타임에서 재사용하는 화면 컴포넌트는 `shared/components/page` 아래에 둔다. 화면 생성기가 복사하는 원본 파일은 `tools/generator/templates` 아래에 둔다. 두 위치를 분리해야 공통 컴포넌트와 생성기용 샘플 파일의 책임이 섞이지 않는다.

## 5. 표준 화면 유형

SI 프로젝트의 업무 화면은 대부분 반복 패턴이다. 우선 아래 화면 유형을 표준 템플릿으로 만든다.

### 5.1 SearchGridPage

검색조건 + 조회 버튼 + AG Grid 목록 화면이다.

적용 예: 메타데이터 조회, 공통코드 조회, 사용자 조회, 이력 조회

### 5.2 SearchGridDetailPage

검색조건 + 목록 + 상세 패널 화면이다.

적용 예: 데이터셋 목록 클릭 시 컬럼 상세 표시, 사용자 목록 클릭 시 권한 상세 표시

### 5.3 FormPage

등록/수정 폼 화면이다.

적용 예: 메타데이터 등록, 코드 등록, 사용자 수정

### 5.4 ReadonlyDetailPage

상세 조회 전용 화면이다.

적용 예: 데이터셋 상세, 승인 상세, 처리 이력 상세

### 5.5 DashboardPage

요약 카드, 차트, 진행상태, 주요 지표를 보여주는 화면이다.

### 5.6 LineagePage

그래프와 단계 목록을 함께 제공하는 데이터 흐름 화면이다.

## 6. 업무 화면 작성 공식

신규 업무 화면은 아래 파일을 기본 단위로 만든다.

```text
features/{feature-name}/
  api.ts
  types.ts
  columns.ts
  pages/
    {FeatureName}ListPage.vue
```

각 파일의 역할은 다음과 같다.

- `types.ts`: 요청/응답 DTO와 화면 상태 타입 정의
- `api.ts`: 백엔드 API 호출 함수 정의
- `columns.ts`: AG Grid 컬럼 정의
- `pages/*.vue`: 공통 템플릿과 업무 설정을 조립하는 화면

개발자는 `types.ts`, `api.ts`, `columns.ts`, `Page.vue` 순서로 화면을 만든다. `.vue` 파일에 API URL, 복잡한 데이터 가공, 공통 에러 처리, 공통 그리드 설정이 직접 들어가지 않도록 한다.

## 7. 목록 조회 표준 계약

SI 업무 화면은 데이터가 커지는 경우가 많으므로 목록 화면은 기본적으로 서버 페이징, 서버 정렬, 서버 필터링을 전제로 설계한다. 현재 샘플처럼 소량 데이터를 받아 AG Grid `quickFilter`로 처리하는 방식은 샘플 또는 임시 화면에만 허용한다.

표준 목록 요청 타입은 다음 형태를 기준으로 한다.

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

표준 목록 응답 타입은 다음 형태를 기준으로 한다.

```ts
export type ListResponse<TRow> = {
  rows: TRow[];
  totalCount: number;
  pageNo: number;
  pageSize: number;
};
```

목록 화면 공통 규칙:

- 조회 버튼을 누르면 `pageNo`를 1로 초기화하고 서버에 다시 요청한다.
- 페이지 이동, 페이지 크기 변경, 정렬 변경은 서버 요청으로 처리한다.
- 총 건수는 서버 응답의 `totalCount`를 기준으로 표시한다.
- AG Grid 내장 필터는 보조 UI로만 사용하고, 표준 검색조건은 서버 요청의 `filters`에 담는다.
- 대용량 화면은 전체 데이터를 한 번에 내려받지 않는다.
- 백엔드가 Spring `Page`처럼 다른 응답 포맷을 사용하더라도 `api.ts`에서 `ListResponse<T>`로 변환한 뒤 화면에 전달한다.

> **개선사항 — 현재 백엔드와 계약 간 간격**
>
> 현재 이 프로젝트의 백엔드(`CatalogController`)는 `List<T>`를 그대로 반환하며 `ListResponse` 구조가 없다. 구현 전에 아래 두 방향 중 하나를 결정해야 한다.
>
> - **백엔드 수정**: 백엔드 응답을 `{ rows, totalCount, pageNo, pageSize }` 구조로 맞춘다. 새로 작성하는 API부터 적용하고 기존 API는 순차 전환한다.
> - **프론트 래핑**: 백엔드를 건드리지 않고 `api.ts`에서 `List<T>` 응답을 `ListResponse<T>`로 변환한다. 일시적으로 유효하지만 장기적으로 두 포맷이 혼재할 수 있다.
>
> 신규 API는 백엔드 수정을 권장한다. 레거시 연동처럼 백엔드 수정이 불가한 경우에만 프론트 래핑을 허용한다.

## 8. 공통 API 클라이언트

현재 화면별로 `fetch("/api/...")`를 직접 호출하는 구조는 화면 수가 늘어나면 유지보수가 어렵다.

공통 API 클라이언트를 도입한다.

```text
shared/api/http.ts
shared/api/api-error.ts
shared/api/types.ts
```

필수 기능:

- base URL 처리
- JSON 요청/응답 처리
- 공통 에러 메시지 변환
- 401/403 공통 처리
- 로딩 상태와 연계 가능한 구조
- 파일 다운로드 대응
- 백엔드 표준 응답 포맷 대응

예상 사용 방식:

```ts
import { http } from "@/shared/api/http";

export function fetchMetadataList() {
  return http.get<MetadataCatalogItem[]>("/api/catalog/metadata");
}
```

목록 API는 가능하면 `ListRequest<TFilter>`와 `ListResponse<TRow>`를 사용한다.

```ts
export function fetchMetadataList(request: ListRequest<MetadataSearchFilter>) {
  return http.get<ListResponse<MetadataCatalogItem>>("/api/catalog/metadata", {
    params: request,
  });
}
```

> **개선사항 — GET 쿼리 파라미터 직렬화**
>
> `ListRequest`의 `sort`는 배열이고 `filters`는 중첩 객체가 될 수 있다. 브라우저 기본 `URLSearchParams`와 `fetch`는 이를 올바르게 직렬화하지 않으므로 `http.ts` 구현 시 직렬화 방식을 미리 정해야 한다.
>
> 권장 방향:
>
> - `sort`, `filters`처럼 구조가 복잡한 목록 조회는 GET 대신 POST를 사용한다.
> - GET을 유지해야 한다면 `http.ts`에서 배열을 `sort[0][field]=...` 형태로 직렬화하는 `paramsSerializer`를 구현하고 모든 화면에 일관되게 적용한다.
>
> 방식을 정하지 않으면 화면마다 직렬화가 달라져 백엔드 파싱 오류가 발생할 수 있다.

## 9. AG Grid 표준 래퍼

AG Grid는 대부분의 SI 화면에서 반복 사용된다. 화면마다 직접 설정하지 않고 `BaseGrid.vue`로 감싼다.

```text
shared/components/grid/BaseGrid.vue
shared/components/grid/grid-defaults.ts
shared/components/grid/grid-formatters.ts
```

공통 처리 대상:

- 기본 컬럼 옵션
- 행 선택 방식
- 높이 정책
- 빈 데이터 표시
- 로딩 오버레이
- 컬럼 자동 크기
- 행 클릭 이벤트
- 공통 formatter
- 공통 cell renderer
- 엑셀 다운로드 연계

예상 사용 방식:

```vue
<BaseGrid
  :rows="rows"
  :columns="columns"
  row-key="metadataId"
  @row-click="selectRow"
/>
```

`BaseGrid`는 페이징 상태와 정렬 변경 이벤트를 화면으로 전달해야 한다. 실제 서버 요청은 화면 또는 composable에서 수행하고, 그리드 컴포넌트는 API URL을 직접 알지 않게 한다.

## 10. 검색 영역 표준화

검색조건은 업무 화면마다 조금씩 다르므로 처음부터 과도한 설정 기반 컴포넌트로 만들지 않는다. 우선 slot 기반의 `SearchPanel`을 둔다.

```text
shared/components/search/SearchPanel.vue
shared/components/search/SearchActions.vue
```

공통 지원 기능:

- 조회
- 초기화
- Enter 조회
- 검색조건 접기/펼치기
- 총 건수 표시
- 주요 버튼 영역

검색 필드 자체는 Element Plus 컴포넌트를 그대로 쓰되, 배치와 버튼 영역은 표준화한다.

## 11. 레이아웃 및 화면 스타일 표준

현재 `App.vue`에 메뉴가 직접 구성되어 있다. SI 템플릿에서는 레이아웃을 분리한다.

```text
app/layouts/AppShell.vue
app/layouts/DefaultLayout.vue
app/layouts/BlankLayout.vue
shared/components/navigation/TopNav.vue
shared/components/navigation/SideNav.vue
shared/components/page/PageHeader.vue
```

권장 기본 레이아웃:

- 상단: 시스템명, 사용자명, 로그아웃, 환경 표시
- 좌측: 1~2 depth 메뉴
- 본문: breadcrumb, page title, actions, content

업무 포털 성격상 좌측 메뉴 + 상단 헤더 구조가 신규 개발자와 업무 사용자 모두에게 익숙하다.

화면 스타일 기본 규칙:

- 검색 영역은 본문 상단에 고정된 패턴으로 배치한다.
- 조회, 초기화, 저장, 삭제, 엑셀 다운로드 같은 주요 버튼 위치를 화면마다 통일한다.
- 목록 화면은 총 건수, 페이지 크기, 조회 결과 없음 상태를 같은 위치와 문구로 표시한다.
- 상세 패널은 우측 또는 하단 중 화면 유형별 표준 위치를 정하고 임의 배치를 줄인다.
- 색상, 간격, 폰트 크기, 카드 반경은 Tailwind/Element Plus 토큰 또는 공통 CSS 변수로 관리한다.
- 업무 화면 내부에는 사용법 설명 문구를 과도하게 넣지 않고, 필요한 안내는 도움말 또는 문서로 분리한다.

## 12. 메뉴, 라우터, 권한 표준

라우터와 메뉴를 별도로 수정하면 누락이 발생하기 쉽다. 메뉴 설정 하나로 라우터와 메뉴를 함께 관리한다.

```ts
export const menuRoutes = [
  {
    path: "/metadata",
    title: "메타데이터 조회",
    component: () => import("@/features/metadata/pages/MetadataListPage.vue"),
    menu: true,
    auth: "METADATA_READ",
  },
];
```

초기에는 정적 설정으로 충분하다. 나중에 백엔드 메뉴/권한 API로 전환할 수 있도록 `auth`, `menu`, `children` 필드는 미리 고려한다.

권한 표준은 템플릿 초기부터 포함한다.

- 라우트에는 `meta.auth` 또는 메뉴 설정의 `auth`를 둔다.
- 라우터 가드는 권한이 없는 화면 접근을 차단한다.
- 버튼은 `AuthButton` 컴포넌트 또는 `v-permission` 디렉티브로 제어한다.
- 프론트엔드 권한 제어는 사용자 경험을 위한 것이며, 실제 보안은 백엔드 API 권한 검증으로 보장한다.
- 401은 로그인 만료 또는 인증 필요 상태로, 403은 권한 없음 상태로 공통 처리한다.

예상 사용 방식:

```vue
<AuthButton auth="METADATA_SAVE" type="primary" @click="save">
  저장
</AuthButton>
```

> **개선사항 — 전역 상태 관리(Pinia) 도입**
>
> `AuthButton`과 `v-permission`이 현재 로그인 사용자의 권한 목록을 알아야 하므로 전역 상태가 필요하다. 현재 문서는 화면별 `ref`만 언급하지만, 인증·권한 정보는 전역으로 공유해야 한다.
>
> 권장 방향:
>
> - **Pinia 도입**: `shared/stores/auth.ts`에 사용자 정보와 권한 목록을 관리한다. 로그인 성공 시 store에 저장하고, 라우터 가드와 `AuthButton`이 이를 참조한다.
> - 화면별 데이터는 기존처럼 화면 내부 `ref`로 관리하고, 전역이 필요한 것(auth, 공통코드, 알림)만 store로 올린다.
>
> `package.json`에 `pinia` 의존성 추가와 `app/providers/`에 `createPinia()` 등록이 필요하다.

## 13. 화면 생성 스크립트

SI 프로젝트에서는 문서보다 생성기가 더 효과적이다.

예상 명령:

```powershell
npm run gen:page metadata-list
```

생성 결과:

```text
features/metadata-list/
  api.ts
  types.ts
  columns.ts
  pages/
    MetadataListPage.vue
```

생성된 화면은 `SearchGridPage` 기준 샘플을 포함하고, 개발자는 API URL, 타입, 컬럼, 검색조건만 수정한다.

초기 구현은 `scripts/generate-page.mjs` 정도의 단순 스크립트로 충분하다.

생성기 템플릿 원본은 `tools/generator/templates`에 둔다. 런타임 공통 컴포넌트와 생성기용 복사 원본을 같은 폴더에 두지 않는다.

> **개선사항 — 화면 유형 지정 방식**
>
> 5절에서 화면 유형을 6가지로 정의했지만 현재 생성 명령에는 유형 지정 방법이 없다. 기본값이 `SearchGridPage`라도 폼이나 상세 화면을 만들 때는 결국 수동 복사가 필요해진다.
>
> 권장 방향:
>
> ```powershell
> npm run gen:page metadata-list                 # 기본값: SearchGridPage
> npm run gen:page user-form --type form         # FormPage
> npm run gen:page dataset-detail --type detail  # ReadonlyDetailPage
> ```
>
> 초기 구현은 `SearchGridPage`만 지원하는 것으로 범위를 제한하고, 나머지 유형은 2순위에서 추가한다. 지원 유형과 기본값은 `tools/generator/README.md`에 명시한다.

## 14. 개발자 온보딩 문서

문서는 길게 만들기보다 업무 개발에 바로 필요한 항목만 둔다.

```text
docs/frontend/
  01-quick-start.md
  02-page-patterns.md
  03-api-and-list-contract.md
  04-grid-convention.md
  05-permission-convention.md
  06-coding-rules.md
  07-quality-gate.md
```

각 문서의 목적:

- `01-quick-start.md`: 실행, 빌드, 폴더 구조, 첫 화면 수정
- `02-page-patterns.md`: 목록, 목록+상세, 폼, 대시보드 화면 만드는 법
- `03-api-and-list-contract.md`: API 클라이언트, 에러 처리, 목록 조회 요청/응답 규칙
- `04-grid-convention.md`: AG Grid 컬럼, formatter, 선택, 다운로드 규칙
- `05-permission-convention.md`: 라우트, 메뉴, 버튼 권한 제어 규칙
- `06-coding-rules.md`: 파일명, import, 컴포넌트, composable 작성 규칙
- `07-quality-gate.md`: 빌드, 테스트, 화면 확인 기준

## 15. 기준 샘플 화면

가장 중요한 기준 샘플은 `MetadataListPage`로 둔다.

이 화면에는 SI 업무 화면에서 반복되는 패턴을 모두 포함한다.

- 검색조건
- 조회
- 초기화
- AG Grid 목록
- 서버 페이징
- 서버 정렬
- 행 클릭
- 상세 패널
- 로딩
- 에러 메시지
- 총 건수
- 버튼 영역
- 권한 버튼 샘플
- API 호출
- 타입 정의
- 컬럼 분리

신규 개발자는 이 화면을 기준으로 다른 업무 화면을 만든다.

## 16. 구현 순서와 완료 기준

1. `src/app`, `src/shared`, `src/features` 구조를 만든다.
2. 현재 `metadata`, `source-sample`, `lineage` 화면을 feature 구조로 이전한다.
3. `shared/api/http.ts` 공통 API 클라이언트를 추가한다.
4. `BaseGrid.vue`, `PageHeader.vue`, `SearchPanel.vue`를 추가한다.
5. `GridPageLayout`을 SI 표준 레이아웃 기준으로 정리한다.
6. `SearchGridPage`와 `SearchGridDetailPage` 템플릿을 구현한다.
7. 메타데이터 화면을 첫 번째 기준 샘플로 리팩터링한다.
8. 원천 샘플 화면을 두 번째 샘플로 리팩터링한다.
9. 리니지 화면은 그래프 전용 템플릿으로 정리한다.
10. 화면 생성 스크립트를 추가한다.
11. `docs/frontend` 온보딩 문서를 작성한다.

단계별 완료 기준:

- 구조 이관 완료: 기존 라우트 URL이 유지되고 `npm run build`가 통과한다.
- 공통 API 클라이언트 완료: 화면에서 직접 `fetch`를 호출하지 않고 `http` 래퍼를 사용한다.
- 목록 조회 표준 완료: 기준 목록 화면이 `ListRequest`와 `ListResponse` 구조로 조회, 정렬, 페이징을 처리한다.
- 공통 그리드 완료: 메타데이터 화면과 원천 샘플 화면이 `BaseGrid`를 사용한다.
- 검색 패널 완료: 조회, 초기화, 총 건수 표시가 동일한 컴포넌트 패턴으로 동작한다.
- 권한 표준 완료: 메뉴 접근 제한과 버튼 권한 제어 샘플이 하나 이상 구현되어 있다.
- 기준 샘플 완료: `MetadataListPage`가 검색, 페이징, 행 선택, 상세 패널, 로딩, 에러 메시지를 모두 포함한다.
- 생성기 완료: 생성된 신규 화면이 별도 수정 없이 타입 검사와 빌드를 통과한다.
- 온보딩 문서 완료: 신규 개발자가 문서만 보고 목록 화면 하나를 추가할 수 있다.

## 17. 품질 게이트

템플릿 구조 변경이나 신규 화면 추가 시 아래 검증을 기본으로 수행한다.

```powershell
npm.cmd run build
```

권장 검증 항목:

- TypeScript 타입 검사 통과
- Vite 프로덕션 빌드 통과
- 기준 화면 라우트 진입 확인
- 목록 조회, 검색, 초기화, 페이징, 행 선택 동작 확인
- API 실패 시 공통 에러 메시지 표시 확인
- 권한 없는 메뉴와 버튼 제어 확인
- 생성기로 만든 샘플 화면의 빌드 통과 확인

테스트 코드가 준비되면 아래 검증도 품질 게이트에 포함한다.

```powershell
npm.cmd run test:unit
npm.cmd run test:e2e
```

## 18. 우선순위

### 1순위

- 공통 API 클라이언트
- 목록 조회 표준 계약
- 공통 그리드 래퍼
- 메타데이터 기준 샘플 화면
- 권한/라우트/버튼 제어 기본 골격
- 화면 작성 규칙 문서

### 2순위

- 검색 패널 표준화
- 메뉴/라우터 설정화
- 원천 샘플 화면 리팩터링
- 리니지 화면 템플릿화
- 품질 게이트 자동화

### 3순위

- 화면 생성 스크립트
- 공통 코드 콤보
- 엑셀 다운로드
- 고급 에러 처리

## 19. 기대 효과

- 신규 개발자의 적응 시간이 줄어든다.
- 화면마다 다른 구조가 생기는 것을 줄일 수 있다.
- 리뷰 기준이 명확해진다.
- 공통 수정의 영향 범위가 예측 가능해진다.
- 프로젝트 후반에 화면 수가 늘어나도 품질 편차를 줄일 수 있다.

결론적으로 이 프론트엔드 템플릿화의 핵심은 컴포넌트를 많이 만드는 것이 아니라, SI 개발자가 같은 방식으로 업무 화면을 빠르게 만들 수 있는 기준 구현과 개발 규칙을 제공하는 것이다.
