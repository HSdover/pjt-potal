# SI 프로젝트 프론트엔드 템플릿화 완료 요약

작성일: 2026-05-12
갱신일: 2026-05-18

이 문서는 프론트엔드 템플릿화 과정에서 완료된 큰 흐름과 유지할 결정을 보관한다.
앞으로 할 일과 후보 우선순위는 `frontend-si-template.md`에서 관리한다.

## 완료된 기반

- 공통 API 클라이언트와 API 오류 타입을 추가했다.
  - `frontend/src/shared/api/http.ts`
  - `frontend/src/shared/api/api-error.ts`
- 목록 조회 표준 타입과 응답 어댑터를 추가했다.
  - `frontend/src/shared/types/list.ts`
  - `frontend/src/shared/utils/list-adapter.ts`
- 공통 UI 단위를 추가했다.
  - `frontend/src/shared/components/grid/BaseGrid.vue`
  - `frontend/src/shared/components/search/SearchPanel.vue`
  - `frontend/src/shared/components/auth/AuthButton.vue`
- 주요 화면을 feature 구조로 정리했다.
  - `frontend/src/features/metadata`
  - `frontend/src/features/source-sample`
  - `frontend/src/features/lineage`
- 라우터 meta 기준으로 메뉴, 화면 제목, 권한 코드를 관리하도록 정리했다.
- 메타데이터와 원천 샘플 목록을 서버 목록 API 계약으로 전환했다.
  - `POST /api/metadata/search`
  - `POST /api/source-sample/search`
- 대시보드와 목록 화면의 API 실패 피드백을 정리했다.
- 표준 템플릿에서 제외할 커스텀 CSS 샘플 화면을 제거했다.
- 통합 생성기를 추가했다.
  - `tools/generator/page-generator.mjs`
  - `tools/generator/backend-generator.mjs`
  - `tools/generator/generate-feature-from-config.mjs`
  - `tools/generator/generate-feature.cmd`
  - `tools/generator/generator.config.json`
  - `tools/generator/pages/sample-list.json`
- 프론트엔드 온보딩 문서를 추가했다.
  - `docs/frontend/01-quick-start.md`
  - `docs/frontend/02-page-patterns.md`
  - `docs/frontend/03-api-and-list-contract.md`
  - `docs/frontend/04-grid-convention.md`
  - `docs/frontend/05-permission-convention.md`
  - `docs/frontend/06-coding-rules.md`
  - `docs/frontend/07-quality-gate.md`
- 백엔드 패키지와 API 경로를 기능명 기준으로 정리했다.
  - `metadata`
  - `sourcesample`
  - `lineage`
  - `common/list`
  - `sampledata`
  - `localdev`

## 유지할 결정

- 새 업무 화면은 `features/{feature}` 아래에 `api.ts`, `types.ts`, `columns.ts`, `pages/*.vue` 구조로 둔다.
- 목록 API는 검색 조건, 페이징, 정렬을 `ListRequest`로 전달하고 `ListResponse`로 받는다.
- 화면에서는 `fetch()`를 직접 호출하지 않고 공통 HTTP 클라이언트를 사용한다.
- 표준 목록 화면은 통합 생성기를 먼저 사용하고, 생성 후 업무 필드와 라우터를 조정한다.
- 라우트 권한은 `router`의 `meta.auth`를 기준으로 관리한다.
- 로그인과 사용자별 권한 API가 생기기 전까지 권한 제어는 샘플 권한 목록 기준으로 유지한다.

## 검증 이력

2026-05-12 기준으로 아래 검증을 통과한 상태에서 완료 처리했다.

- `npm.cmd run build`
- `npm.cmd run test:unit -- --run`
- `.\gradlew.bat test --rerun-tasks`
- `tools\generator\generate-feature.cmd --dry-run`

## 남은 주의사항

- 라우트 동적 import 적용 후 화면별 청크는 분리됐다.
- Element Plus, AG Grid 등 공통 라이브러리 청크가 커서 Vite 청크 크기 경고는 남아 있다.
