# Frontend Quick Start

작성일: 2026-05-12

## 실행

```powershell
cd backend
.\gradlew.bat bootRun
```

```powershell
cd frontend
npm install
npm run dev
```

프론트 개발 서버는 `http://127.0.0.1:5173`을 사용한다. `/api` 요청은 기본값으로 `http://127.0.0.1:18080` 백엔드에 프록시된다.

## 기본 구조

```text
src/
  features/         업무 기능별 화면, API, 타입, 컬럼
  shared/           업무에 종속되지 않는 공통 컴포넌트와 유틸
  components/       현재 공통 레이아웃 컴포넌트
  router/           라우트, 메뉴, 화면 권한 메타
  views/            대시보드와 공통 fallback 화면
```

새 업무 목록 화면은 `features/{feature-name}` 아래에 만든다.

```text
features/{feature-name}/
  api.ts
  columns.ts
  types.ts
  pages/
    {FeatureName}ListPage.vue
```

## 새 화면 생성

권장 방식은 페이지 spec 파일을 먼저 만드는 것이다.

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

위 실행 파일은 `tools/generator/generator.config.json`의 `spec` 값을 읽는다.

생성기는 `SearchGridPage` 유형만 지원한다. 생성 후에는 라우터에 화면을 등록하고 백엔드 API 경로를 실제 업무 API에 맞춘다.

## 확인 기준

```powershell
cd frontend
npm.cmd run build
```

백엔드 변경이 있다면 다음도 실행한다.

```powershell
cd backend
.\gradlew.bat test --rerun-tasks
```
