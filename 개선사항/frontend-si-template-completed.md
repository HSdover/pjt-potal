# SI 프로젝트 프론트엔드 템플릿화 완료 내역

작성일: 2026-05-12

이 문서는 `frontend-si-template.md`에서 완료된 항목을 분리해 보관한다. 앞으로 할 일과 우선순위는 `frontend-si-template.md`에서 관리한다.

## 완료된 기반 작업

- 공통 API 클라이언트 추가
  - `frontend/src/shared/api/http.ts`
  - `frontend/src/shared/api/api-error.ts`
- 목록 조회 타입 골격 추가
  - `frontend/src/shared/types/list.ts`
- 기존 배열 응답 호환 어댑터 추가
  - `frontend/src/shared/utils/list-adapter.ts`
- 공통 그리드 추가
  - `frontend/src/shared/components/grid/BaseGrid.vue`
- 공통 검색 패널 추가
  - `frontend/src/shared/components/search/SearchPanel.vue`
- 권한 버튼 골격 추가
  - `frontend/src/shared/components/auth/AuthButton.vue`
- 메타데이터 기준 샘플 화면 feature 구조 적용
  - `frontend/src/features/metadata/pages/MetadataListPage.vue`
- 원천 샘플 목록 화면 feature 구조 적용
  - `frontend/src/features/source-sample/pages/SourceSampleListPage.vue`
- 리니지 화면 feature 구조 이전
  - `frontend/src/features/lineage/pages/LineagePage.vue`
- 라우터 권한 메타와 접근 가드 골격 추가
  - `frontend/src/router/index.ts`
- feature 이전이 완료된 기존 `views` 고아 파일 정리
  - `MetadataView.vue` / `MetadataView.ts`
  - `SourceSampleView.vue` / `SourceSampleView.ts`
  - `LineageView.vue` / `LineageView.ts`
- 메타데이터 데이터 유형 필터를 실제 샘플 DB 값 기준으로 정리
- 리니지 검색 조건을 그래프와 처리 단계 그리드에 동일하게 적용
- `BaseGrid.vue`의 `ElPagination` 명시 import 추가
- systemd 운영 DB 환경변수 예시 주석 추가
- `GOVERNANCE_DATASOURCE_DRIVER` 환경변수 설정 추가
- 원천 샘플 목록을 서버 목록 API로 전환
  - `POST /api/source-sample/search`
  - `{ rows, totalCount, pageNo, pageSize }` 응답
  - 지역/키워드 필터, 정렬, 페이징 처리
- 메타데이터 목록을 서버 목록 API로 전환
  - `POST /api/metadata/search`
  - `{ rows, totalCount, pageNo, pageSize }` 응답
  - 데이터 유형/키워드 필터, 정렬, 페이징 처리
- 프론트 공통 HTTP 클라이언트에 JSON `POST` 지원 추가
- 원천 샘플 목록 화면이 서버 `ListResponse`를 사용하도록 변경
- 메타데이터 목록 화면이 서버 `ListResponse`를 사용하도록 변경
- 대시보드 API 실패 처리 추가
  - `ElMessage.error()` 표시
  - 실패 시 상태 태그를 `오류`로 표시
- 라우터와 메뉴를 route meta 기준으로 일원화
  - `meta.title`, `meta.menu`, `meta.order`, `meta.auth`
  - `App.vue` 메뉴를 라우터 설정에서 생성
- 라우트 컴포넌트 동적 import 적용
- 권한 fallback 화면 추가
  - `frontend/src/views/ForbiddenView.vue`
- 라우터 가드가 권한 없음 상태에서 `/forbidden`으로 이동하도록 변경
- `MetadataListPage`에서 검색/페이지/정렬 변경 시 선택 상세 상태 초기화
- `BaseGrid.vue` 타입 안정성 개선
  - `<script setup>` 제네릭으로 row 타입 전달
  - `rows`, `columns`, `rowClick` 이벤트 타입을 같은 `TRow`로 연결
  - `MetadataListPage`의 `row as MetadataItem` 캐스팅 제거
- `list-adapter.ts` 검색 필드 지정 옵션 추가
  - `toListResponse(sourceRows, request, { searchFields })` 형태 지원
  - 옵션이 없으면 기존처럼 전체 필드 검색을 유지
  - 옵션이 있으면 업무상 검색 가능한 필드만 키워드 검색 대상으로 사용
- `list-adapter.ts` 단위 테스트 추가
  - 기본 전체 필드 검색 동작
  - 검색 필드 제한 시 ID 같은 내부 필드 제외
  - 지정 필드 내 키워드 매칭
- 표준 템플릿에서 제외할 커스텀 CSS 샘플 화면 제거
  - `/table-detail-sample` 라우트 제거
  - `TABLE_DETAIL_READ` 샘플 권한 제거
  - `frontend/src/views/TableDetailSampleView.vue` 제거
  - `frontend/src/views/TableDetailSampleView.ts` 제거
  - `frontend/public/styles/common-style.css` 제거
  - 전용 `/styles/` Nginx 캐시 설정 제거
- 표준 목록 화면 생성기 추가
  - `tools/generator/page-generator.mjs` 프론트엔드 생성 로직 추가
  - `tools/generator/backend-generator.mjs` 백엔드 생성 로직 추가
  - `tools/generator/generate-feature-from-config.mjs` config 기반 통합 생성기 추가
  - `tools/generator/generator.config.json` 추가
  - `tools/generator/generate-feature.cmd` 실행 파일 추가
  - `tools/generator/README.md` 추가
  - `tools/generator/pages/sample-list.json` spec 예시 추가
  - `tools/generator/pages/README.md` 추가
  - 현재 지원 범위는 `search-grid` 유형으로 제한
- 프론트엔드 로컬 온보딩 문서 추가
  - `docs/frontend/01-quick-start.md`
  - `docs/frontend/02-page-patterns.md`
  - `docs/frontend/03-api-and-list-contract.md`
  - `docs/frontend/04-grid-convention.md`
  - `docs/frontend/05-permission-convention.md`
  - `docs/frontend/06-coding-rules.md`
  - `docs/frontend/07-quality-gate.md`
- 문서 드리프트 정리
  - `README.md`에 화면 생성기와 프론트 온보딩 문서 안내 추가
  - `docs/project_inventory.txt`를 현재 feature/API/문서 구조에 맞게 갱신
  - `frontend-si-template.md`에서 완료 항목을 제거하고 남은 후보만 유지

## 완료된 검증

- 2026-05-12 `npm.cmd run build` 통과
- 2026-05-12 `.\gradlew.bat test --rerun-tasks` 통과
- 2026-05-12 원천 샘플 서버 목록 API 테스트 추가 및 통과
- 2026-05-12 메타데이터 서버 목록 API 테스트 추가 및 통과
- 2026-05-12 `BaseGrid` 제네릭 타입 적용 후 `npm.cmd run build` 통과
- 2026-05-12 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 `list-adapter` 검색 필드 지정 적용 후 `npm.cmd run build` 통과
- 2026-05-12 `TableDetailSampleView`와 `common-style.css` 제거 후 `npm.cmd run build` 통과
- 2026-05-12 임시 생성 feature 포함 상태에서 `npm.cmd run build` 통과
- 2026-05-12 임시 생성 파일 제거 후 최종 `npm.cmd run build` 통과
- 2026-05-12 최종 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 최종 `.\gradlew.bat test --rerun-tasks` 통과
- 2026-05-12 파일 기반 생성기 추가 후 `npm.cmd run build` 통과
- 2026-05-12 파일 기반 생성기 추가 후 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 `tools\generator\generate-feature.cmd --dry-run` 실행 파일 기반 생성기 확인
- 2026-05-12 실행 파일 기반 생성기 추가 후 `npm.cmd run build` 통과
- 2026-05-12 실행 파일 기반 생성기 추가 후 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 `.cmd` 외 생성 진입점 제거 후 `tools\generator\generate-feature.cmd --dry-run` 통과
- 2026-05-12 `.cmd` 외 생성 진입점 제거 후 `npm.cmd run build` 통과
- 2026-05-12 `.cmd` 외 생성 진입점 제거 후 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 `local` profile 백엔드 재시작 시 기존 프론트 dev server 종료, `npm.cmd run build`, `npm.cmd run dev` 순서로 실행되도록 변경
- 2026-05-12 존재하지 않는 `SampleListPage` 라우트 경로를 실제 `features/sample-list` 경로로 수정
- 2026-05-12 프론트 dev server 재시작 변경 후 `npm.cmd run build` 통과
- 2026-05-12 프론트 dev server 재시작 변경 후 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 프론트 dev server 재시작 변경 후 `.\gradlew.bat test --rerun-tasks` 통과
- 2026-05-12 백엔드 통합 샘플 패키지를 `metadata`, `sourcesample`, `lineage`, `common/list`, `sampledata`, `localdev`로 분리
- 2026-05-12 API 경로를 기능명 기준인 `/api/metadata`, `/api/source-sample`, `/api/lineage`로 변경
- 2026-05-12 백엔드 패키지/API 이름 정리 후 `npm.cmd run build` 통과
- 2026-05-12 백엔드 패키지/API 이름 정리 후 `npm.cmd run test:unit -- --run` 통과
- 2026-05-12 백엔드 패키지/API 이름 정리 후 `.\gradlew.bat test --rerun-tasks` 통과

## 확인된 주의사항

- 라우트 동적 import 적용 후 화면별 청크는 분리됐다.
- Element Plus, AG Grid 등 공통 라이브러리 청크가 여전히 커서 Vite 청크 크기 경고는 남아 있다.

## 검토 중 정상 동작으로 확인한 항목

- `ElPagination`은 `main.ts`에서 `use(ElementPlus)`로 전역 등록되어 명시 import 없이도 런타임 동작은 가능하다. 명시 import는 일관성 개선이다.
- `list-adapter.ts`의 `reverse().forEach()` 정렬은 JS 안정 정렬을 이용한 다중 컬럼 정렬 방식이다.
- `void load()`는 floating promise를 명시적으로 무시하는 TypeScript 관례다.

## 2026-05-12 백엔드 생성기 완료

- `tools/generator/backend-generator.mjs`를 추가했습니다.
- `tools/generator/generator.config.json`와 선택된 페이지 spec을 읽는 백엔드 생성기를 추가했습니다.
- `tools/generator/pages/sample-list.json`에 `backend` 메타데이터를 추가했습니다.
- `search-grid` 백엔드 엔드포인트에 필요한 Controller, Service, Repository, DTO 파일을 생성하도록 구성했습니다.
- `node tools\generator\generate-feature-from-config.mjs --backend-only --dry-run`으로 검증했습니다.
- `tools\generator\generate-feature.cmd --backend-only --dry-run`으로 `.cmd` 실행 파일을 검증했습니다.

## 2026-05-13 통합 생성기 완료

- `tools/generator/generate-feature.cmd`를 추가했습니다.
- `tools/generator/generate-feature-from-config.mjs`를 추가했습니다.
- 하나의 페이지 spec으로 프론트엔드 화면 파일과 백엔드 기본 패키지를 함께 생성하도록 구성했습니다.
- 실제 생성 전에 프론트엔드와 백엔드 생성 대상을 먼저 검사하도록 구성했습니다.
- `--frontend-only`, `--backend-only`, `--dry-run`, `--force` 옵션을 지원하도록 구성했습니다.
- `node tools\generator\generate-feature-from-config.mjs --dry-run --force`로 검증했습니다.
- `tools\generator\generate-feature.cmd --dry-run --force`로 `.cmd` 실행 파일을 검증했습니다.
