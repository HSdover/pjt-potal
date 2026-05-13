# SI 프로젝트 프론트엔드 템플릿화 기준

작성일: 2026-05-11
갱신일: 2026-05-12

이 문서는 앞으로 유지할 템플릿 기준과 남은 후보 작업만 기록한다. 완료된 작업과 검증 기록은 `frontend-si-template-completed.md`에서 관리한다.

## 현재 진행 방향

1. 로컬 개발 환경에서 신규 업무 화면을 일관된 방식으로 만들 수 있는 상태를 먼저 완성한다.
2. 운영 프로젝트 분리, 보안, 배포 설정은 사용자가 다시 요청하기 전까지 우선순위에서 제외한다.
3. 새 작업은 생성기, 온보딩 문서, 기준 샘플 화면을 우선 확인한 뒤 진행한다.

## 로컬 개발 기준

로컬 단계의 목표는 신규 개발자가 같은 방식으로 화면을 만들고 검증할 수 있게 하는 것이다.

기준 구성:

- `features/{feature}/api.ts`: 업무 API 함수
- `features/{feature}/types.ts`: 요청, 응답, 화면 타입
- `features/{feature}/columns.ts`: AG Grid 컬럼
- `features/{feature}/pages/*.vue`: 화면
- `shared/api/http.ts`: 공통 HTTP 클라이언트
- `shared/types/list.ts`: 목록 조회 표준 타입
- `shared/components/grid/BaseGrid.vue`: AG Grid 공통 래퍼
- `shared/components/search/SearchPanel.vue`: 검색 영역 공통 배치
- `shared/components/auth/AuthButton.vue`: 샘플 권한 버튼
- `tools/generator/generate-feature.cmd`: 표준 목록 화면과 기본 백엔드 패키지 통합 실행 파일
- `tools/generator/page-generator.mjs`: 표준 목록 화면 생성 소스
- `docs/frontend/`: 프론트엔드 온보딩 문서

## 표준 목록 조회 계약

목록 화면은 검색 조건, 페이징, 정렬을 `ListRequest<TFilter>` 하나로 관리한다.

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

서버 응답은 `ListResponse<TRow>`를 기준으로 한다.

```ts
export type ListResponse<TRow> = {
  rows: TRow[];
  totalCount: number;
  pageNo: number;
  pageSize: number;
};
```

규칙:

- 조회 버튼은 `pageNo`를 1로 초기화한 뒤 호출한다.
- 페이지, 페이지 크기, 정렬 변경은 서버 API를 다시 호출한다.
- 목록 API는 `POST /search`와 JSON body를 우선 사용한다.
- 화면에서 `fetch()`를 직접 호출하지 않고 `shared/api/http.ts`를 사용한다.

## 화면 생성기

표준 목록 화면은 페이지 spec 파일로 시작한다.

```text
tools/generator/pages/user-list.json
```

```json
{
  "feature": "user-list",
  "type": "search-grid",
  "title": "사용자 목록",
  "description": "사용자를 검색하고 조회합니다.",
  "auth": "USER_READ",
  "apiPath": "/api/users/search"
}
```

생성 전 확인은 `generate-feature.cmd --dry-run`으로 한다.

생성:

```text
tools/generator/generate-feature.cmd
```

실행 파일은 `tools/generator/generator.config.json`의 `spec` 값을 읽는다.

현재 지원 유형:

- `search-grid`: 검색 영역과 AG Grid 목록을 가진 기본 목록 화면

생성 후 처리:

- `src/router/index.ts`에 라우트를 등록한다.
- `api.ts`의 API 경로를 실제 백엔드 경로로 수정한다.
- `types.ts`, `columns.ts`의 샘플 필드를 업무 필드로 교체한다.
- 백엔드에 `POST /search` 목록 API를 추가하거나 기존 API와 연결한다.

## 온보딩 문서

프론트엔드 로컬 개발 기준은 `docs/frontend` 아래 문서로 관리한다.

- `01-quick-start.md`: 실행, 구조, 첫 화면 생성
- `02-page-patterns.md`: 화면 유형
- `03-api-and-list-contract.md`: API와 목록 계약
- `04-grid-convention.md`: AG Grid 기준
- `05-permission-convention.md`: 라우터와 버튼 권한
- `06-coding-rules.md`: 파일 배치와 작성 규칙
- `07-quality-gate.md`: 빌드, 테스트, 화면 확인 기준

## 로컬 검증 기준

프론트 변경 후:

```powershell
cd frontend
npm.cmd run build
```

프론트 테스트 관련 변경 후:

```powershell
cd frontend
npm.cmd run test:unit -- --run
```

백엔드 변경 후:

```powershell
cd backend
.\gradlew.bat test --rerun-tasks
```

화면 확인 대상:

- `/`
- `/metadata`
- `/source-sample`
- `/lineage`
- `/forbidden`

확인 항목:

- 검색, 초기화, 페이지 변경, 페이지 크기 변경, 정렬 변경
- API 실패 시 오류 메시지 표시
- 권한 없는 라우트 접근 시 `/forbidden` 이동
- 목록+상세 화면에서 검색/페이지/정렬 변경 시 선택 상태 초기화

## 현재 남은 로컬 우선순위

현재 정리된 기준에서는 로컬 개발 우선순위 1차 작업이 완료된 상태다.

다음 후보는 필요성이 다시 확인될 때 별도 우선순위로 잡는다.

| 후보 | 판단 기준 |
|---|---|
| 공통 코드 콤보 | 두 개 이상 화면에서 같은 코드 목록 조회 UI가 반복될 때 |
| 엑셀 다운로드 | 실제 목록 화면에서 다운로드 요구가 생기고 서버/클라이언트 방식이 정해질 때 |
| `app/` 구조 분리 | layout, provider 책임이 현재 구조에서 감당하기 어려워질 때 |
| Pinia auth store | 로그인, 사용자 정보, 서버 권한 API가 도입될 때 |

## 운영 분리 보류 항목

아래 항목은 운영 프로젝트 분리 단계에서 다시 다룬다.

- `application-local.yml`, `application-prod.yml` profile 분리
- 운영 H2 console 비활성화
- 운영 sample data initializer 비활성화
- 운영 datasource 환경변수 분리
- systemd `EnvironmentFile` 적용
- Spring Boot 네트워크 바인딩과 SecurityConfig 운영 정책 확정
- Nginx cache, 차단, TLS 정책 확정
- 운영 DB 전환 전략
- Swagger, Actuator 운영 노출 정책 확정
