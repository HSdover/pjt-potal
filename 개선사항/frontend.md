# Frontend 개선사항

## 2026-04-30

### GridPageLayout 주석 정리
- 대상: `frontend/src/components/GridPageLayout.vue`
- 내용: 현재 컴포넌트 상단과 템플릿에 설명 주석이 많아 코드보다 설명 비중이 큼.
- 개선 방향: 학습용 설명은 별도 문서로 옮기고, 컴포넌트 내부에는 슬롯 의도를 알 수 있는 최소 주석만 유지.
- 우선순위: 낮음

### 공통 AG Grid 설정 분리
- 대상: `MetadataView.vue`, `SourceSampleView.vue`, `LineageView.vue`
- 내용: `defaultColDef`가 각 화면에 반복되어 있음.
- 개선 방향: `frontend/src/grid/defaults.ts` 같은 공통 파일로 분리해 정렬, 필터, 리사이즈 기본값을 일관되게 관리.
- 우선순위: 중간

### 페이지 레이아웃 공통화 검토
- 대상: `LineageView.vue`
- 내용: `MetadataView.vue`, `SourceSampleView.vue`는 `GridPageLayout`을 사용하지만 `LineageView.vue`는 별도 레이아웃을 사용함.
- 개선 방향: 리니지 화면은 그래프+그리드 혼합 화면이므로 `GridPageLayout`을 그대로 쓸지, 별도 `PageLayout`을 만들지 검토.
- 우선순위: 낮음

### Element Plus 전역 컴포넌트 사용 규칙 정리
- 대상: 전역 UI 컴포넌트 사용 방식
- 내용: `GridPageLayout.vue`에서 `ElCard`를 명시 import 없이 사용하고 있음. Element Plus 전역 등록 덕분에 동작하지만 코드 스타일 기준이 필요함.
- 개선 방향: 전역 등록을 팀 규칙으로 둘지, 사용하는 컴포넌트마다 명시 import할지 결정.
- 우선순위: 낮음

### 번들 크기 최적화 검토
- 대상: Vite build output
- 내용: AG Grid, Element Plus, Vue Flow 사용으로 JS/CSS 번들이 큼. 현재 샘플 단계에서는 문제 없지만 운영 화면 확장 시 초기 로딩 비용이 커질 수 있음.
- 개선 방향: 라우트 단위 동적 import, Element Plus 자동 import, AG Grid 사용 화면 분리 로딩 검토.
- 우선순위: 낮음

### 화면 전용 데이터 호출 구조 유지
- 대상: `MetadataView.vue/.ts`, `SourceSampleView.vue/.ts`, `LineageView.vue/.ts`
- 내용: 현재 화면 간 데이터 공유가 필요하지 않아 기존 `catalog store`는 제거됨. 단, Pinia 자체는 향후 전역 상태를 위해 유지하기로 함.
- 개선 방향: 화면 전용 API 호출은 각 화면의 `.ts`에 유지하고, 로그인 사용자/권한/공통 알림처럼 전역 상태가 생길 때만 Pinia store를 추가.
- 우선순위: 낮음

### fetch 에러 미처리
- 대상: `MetadataView.vue`, `SourceSampleView.vue`, `LineageView.vue`
- 내용: `onMounted`의 fetch 호출이 `try/finally`만 있고 `catch`가 없음. 각 `.ts`의 fetch 함수가 `throw new Error()`를 하도록 설계되어 있으나 `.vue`에서 받지 않아, API 실패 시 loading만 꺼지고 사용자에게 아무 피드백이 없음.
- 개선 방향: `catch` 블록 추가 후 Element Plus `ElMessage.error()` 또는 화면 내 에러 상태 표시로 사용자에게 실패 안내.
- 우선순위: 중간
