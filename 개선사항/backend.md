# Backend 개선사항

## 2026-04-30

### CSVREAD 경로 안정화
- 대상: `backend/src/main/resources/data.sql`
- 내용: 현재 H2 초기 데이터 적재가 `../data/processed/hira/...` 상대 경로에 의존함.
- 개선 방향: 실행 위치가 바뀌는 JEUS/WAR 환경에서는 경로가 깨질 수 있으므로, 개발용 H2 프로파일과 운영용 DB 초기화 방식을 분리 검토.
- 우선순위: 중간

### API 응답 에러 처리 강화
- 대상: `CatalogController`
- 내용: 현재 샘플 조회 API는 정상 조회 중심으로 구성되어 있음.
- 개선 방향: 데이터 미존재, CSV 적재 실패, DB 초기화 실패 상황에 대한 응답 형식과 로그 정책 추가.
- 우선순위: 낮음

### 메타데이터/리니지 조회 계층 분리
- 대상: `CatalogController`
- 내용: 현재 컨트롤러에서 `JdbcClient`로 직접 SQL을 실행함.
- 개선 방향: 화면/API가 늘어나면 `Service` 또는 `Repository` 계층으로 분리해 SQL과 HTTP 책임을 나누기.
- 우선순위: 낮음

### 운영 DB 전환 준비
- 대상: H2 기반 샘플 DB 구성
- 내용: 현재는 H2 샘플 구조이며, 향후 Oracle/ADB로 전환할 경우 DDL/SQL 문법 차이를 확인해야 함.
- 개선 방향: Oracle 호환 SQL 기준 정리, 프로파일별 datasource 설정, 초기 데이터 적재 방식 분리.
- 우선순위: 중간

### Gradle 프론트 빌드 동시 실행 충돌 방지
- 대상: `backend/build.gradle`
- 내용: 프론트 `npm run build`와 Gradle `nodeSetup`이 동시에 실행되면 `node.exe` 파일 잠금 충돌이 발생한 적 있음.
- 개선 방향: 로컬 작업 가이드에 동시 실행 금지 명시 또는 Gradle Node 설정에서 재설치/삭제 동작 최소화 검토.
- 우선순위: 낮음
