# Governance Portal

삼성증권 AI/RAG 데이터 거버넌스 포털 구축을 위한 Vue + Spring Boot 프로젝트입니다. 현재는 운영 기반, 인증/권한 구조, 공통 API/로깅/첨부/엑셀 기능, 통합메타관리와 권한관리 화면, 개발 참고 화면이 포함되어 있으며 일부 업무 메뉴는 placeholder 상태로 남아 있습니다.

## 기술 구조

- Frontend: Vue 3, TypeScript, Vite, Vue Router, Pinia, Element Plus, AG Grid, Vuelidate, TipTap, Vue Flow, ECharts, Tailwind CSS, ESLint, Vitest, Playwright
- Backend: Java 21, Spring Boot 3.3.7, embedded Tomcat, Spring Security/SAML2, Spring Batch, JPA/MyBatis/QueryDSL, EasyExcel, Springdoc OpenAPI
- DB/Cache: H2 local DB, Oracle JDBC(ojdbc11), Redis cache
- Integration: RestClient + HTTP Interface 기반 외부 REST API 공통 클라이언트
- 운영 Web: Nginx
- 운영 실행: Spring Boot executable JAR + systemd

운영 구조는 다음을 기준으로 합니다.

```text
Client
  -> Nginx
     - /, /assets/*       : frontend/dist 정적 파일 서빙
     - /api/*             : Spring Boot API로 reverse proxy
     - /login/*, /saml2/* : SAML 로그인/ACS proxy
     - /api-docs/*        : Springdoc API 문서 proxy
     - /swagger-ui*       : Swagger UI proxy
     - /actuator/*        : Actuator proxy
  -> Spring Boot embedded Tomcat
     - 127.0.0.1:18080
```

## 사전 요구사항

| 도구 | 권장 버전 | 확인 방법 |
| --- | --- | --- |
| Java JDK | 21 | `java -version` |
| Node.js | 20.19 이상 또는 22.12 이상 | `node -v` |
| npm | 10 이상 | `npm -v` |
| Gradle | wrapper 사용 | `backend/gradlew.bat` 또는 `backend/gradlew` |

내부망/오프라인 작업에서는 시스템 PATH의 Node/npm/Gradle보다 프로젝트 하위 `offline/` 캐시와 도구를 우선 사용합니다.

## 개발 실행

### Backend

```powershell
cd backend
.\gradlew.bat bootRun
```

기본 포트는 `18080`입니다. 별도 프로파일을 지정하지 않으면 `local` 프로파일이 기본으로 적용되어 H2 로컬 DB를 사용합니다.

### IDE main 실행 시 프론트도 함께 실행

IntelliJ IDEA 같은 IDE에서 `GovernancePortalApplication.main()`을 직접 실행할 때 프론트 Vite 개발 서버도 함께 띄우려면 실행 설정에 아래 프로파일을 추가합니다.

```text
spring.profiles.active=local
```

IntelliJ 기준으로는 `Run/Debug Configurations`에서 아래 중 하나를 설정합니다.

```text
Active profiles: local
```

또는 VM options:

```text
-Dspring.profiles.active=local
```

또는 환경 변수로 설정합니다.

```text
SPRING_PROFILES_ACTIVE=local
```

`local` 프로파일은 H2 로컬 DB와 simple cache를 사용하고, 백엔드 시작 시 `frontend` 또는 `../frontend`에서 기존 5173 프론트 서버를 종료한 뒤 `npm.cmd run build`, `npm.cmd run dev`를 실행합니다.

운영 JAR에서는 기본값이 꺼져 있습니다.

```text
GOVERNANCE_FRONTEND_DEV_SERVER_ENABLED=false
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

프론트 개발 서버는 `http://127.0.0.1:5173`에서 실행되며, `/api` 요청은 기본적으로 `http://127.0.0.1:18080`으로 프록시됩니다.

백엔드 포트를 바꿔 실행하는 경우:

```powershell
$env:VITE_API_PROXY_TARGET="http://127.0.0.1:18080"
npm run dev
```

`vite.config.ts` 또는 `VITE_API_PROXY_TARGET` 값을 변경한 뒤에는 실행 중인 Vite 개발 서버를 재시작해야 합니다.

주의:

- `npm run build`는 `frontend/dist`만 새로 생성합니다.
- `local` profile로 백엔드를 재시작하면 기존 Vite 개발 서버를 종료하고 다시 띄웁니다.
- `VITE_API_PROXY_TARGET` 값을 바꾼 경우 백엔드를 재시작하면 프론트 개발 서버도 새 설정으로 다시 시작됩니다.

새 목록 화면 뼈대는 spec 파일 기반 생성기로 만들 수 있습니다.

spec 파일은 `tools/generator/pages/{name}.json`에 둡니다.
`tools/generator/generator.config.json`의 `spec` 값을 수정한 뒤 `tools/generator/generate-feature.cmd`를 실행합니다.

## 운영 빌드

인터넷 가능한 개발/빌드 PC에서는 일반 빌드를 사용합니다.

```powershell
.\scripts\build-production.ps1
```

산출물:

- Frontend 정적 파일: `frontend/dist`
- Backend 실행 JAR: `backend/build/libs/governance-portal-backend.jar`

운영 서버에서는 다음처럼 배치합니다.

```text
/var/www/governance-portal/              # frontend/dist 내용
/opt/governance-portal/
  governance-portal-backend.jar          # Spring Boot API 실행 JAR
```

Nginx 설정 예시는 `infra/nginx/governance-portal.conf`에 있습니다. SAML 운영용 HTTPS 템플릿은 `infra/nginx/governance-portal-https.conf`를 사용합니다.
systemd 서비스 예시는 `infra/systemd/governance-portal.service`에 있습니다.
운영 env 템플릿은 `infra/env/governance-portal.env.template`에 있습니다.
엑셀 업로드를 위해 Nginx 템플릿은 `client_max_body_size 25m`, Spring Boot는 `GOVERNANCE_MULTIPART_MAX_FILE_SIZE=20MB`, `GOVERNANCE_MULTIPART_MAX_REQUEST_SIZE=25MB`를 기준으로 합니다.

운영 Nginx는 `X-Request-Id`를 백엔드로 전달합니다. 백엔드는 같은 값을 MDC 로그와 응답 헤더에 남기므로 장애 대응 시 Nginx access log, Spring Boot 로그, 외부 API 로그를 같은 requestId로 조회합니다.

내부망처럼 인터넷 접근이 막힌 PC에서 빌드해야 하면 `build-production.ps1` 대신 오프라인 빌드 절차를 사용합니다. 먼저 인터넷 가능한 PC에서 `prepare-offline-bundle.ps1`로 `offline/` 캐시를 준비하고, 내부망 PC에서는 `build-offline-jar.ps1`을 실행합니다. 상세 절차는 `docs/offline_build_guide.txt`를 기준으로 합니다.

## 주요 URL

| URL | 용도 |
| --- | --- |
| http://127.0.0.1:5173 | 프론트엔드 개발 서버 |
| http://localhost:18080/api/samples/search | 백엔드 목록 API 예시 |
| http://localhost:18080/h2-console | H2 개발 DB 콘솔 |
| http://localhost:18080/swagger-ui.html | Swagger UI |
| http://localhost:18080/actuator/health | 헬스 체크 |

로컬 로그인 계정:

| ID | Password | 설명 |
| --- | --- | --- |
| `local-dev` | `local1234!` | 로컬 개발용 전체 권한 |
| `local-admin` | `local1234!` | 로컬 관리자 테스트 |

H2 콘솔 접속 정보:

- JDBC URL: `jdbc:h2:mem:governance;MODE=Oracle;DB_CLOSE_DELAY=-1`
- User Name: `sa`
- Password: 없음

## 인증/권한

- 로컬 개발은 `local` 프로파일의 임시 로그인 계정을 사용합니다.
- IAM/KNOX 연동 환경은 `saml` 프로파일을 함께 사용합니다.
- IAM에서 내려오는 기본 역할은 `관리자`, `AI 에이전트관리자`, `데이터 관리자`입니다.
- 권한관리 화면은 `시스템 관리 > 권한관리`에 있으며 `PERMISSION_MANAGE` 권한이 필요합니다.
- 권한 데이터는 `portal_permission`, `portal_iam_role`, `portal_permission_assignment` 테이블로 관리합니다.
- 프론트 메뉴/버튼은 권한 기준으로 숨기고, 백엔드 API는 `@PreAuthorize`와 `SecurityConfig`로 서버단 권한을 검사합니다.

상세 구조와 로컬 권한 제한 테스트 방법은 `docs/permission-management-guide.md`를 기준으로 합니다. 메뉴와 진입 권한을 DB로 관리하는 확장 설계는 `설계/메뉴권한매핑설계.md`를 기준으로 검토합니다.

## DB 프로파일

| 프로파일 | 용도 | DB 초기화 | 주요 env 템플릿 |
| --- | --- | --- | --- |
| `local` | 개인 PC 로컬 개발, H2 DB | `schema.sql`/`data.sql` 실행 | `infra/env/governance-portal.local.env.template` |
| `dev` | 개발서버 실제 Oracle/ADB DB | 자동 초기화 안 함 | `infra/env/governance-portal.dev.env.template` |
| `prod` | 운영서버 실제 Oracle/ADB DB | 자동 초기화 안 함 | `infra/env/governance-portal.env.template` |

실행 예:

```powershell
# 로컬 H2 DB
$env:SPRING_PROFILES_ACTIVE="local"
.\gradlew.bat bootRun

# 개발서버 DB
$env:SPRING_PROFILES_ACTIVE="saml,dev"
$env:GOVERNANCE_DEV_DATASOURCE_URL="jdbc:oracle:thin:@//dev-db-host:1521/service"
$env:GOVERNANCE_DEV_DATASOURCE_USERNAME="dev_appuser"
$env:GOVERNANCE_DEV_DATASOURCE_PASSWORD="change-me"
.\gradlew.bat bootRun

# 운영서버 DB
SPRING_PROFILES_ACTIVE=saml,prod
GOVERNANCE_PROD_DATASOURCE_URL=jdbc:oracle:thin:@//prod-db-host:1521/service
GOVERNANCE_PROD_DATASOURCE_USERNAME=appuser
GOVERNANCE_PROD_DATASOURCE_PASSWORD=change-me
```

`dev`/`prod` 프로파일에서는 `schema.sql`과 `data.sql`을 실행하지 않습니다. 현재 로컬 샘플 스키마는 `DROP TABLE`을 포함하므로 실제 개발/운영 DB에는 DBA 또는 마이그레이션 절차로 테이블을 준비해야 합니다.

## 환경 변수

- `SERVER_ADDRESS`: 백엔드 bind address. 기본값은 `0.0.0.0`, 운영은 `127.0.0.1` 권장.
- `SERVER_PORT`: 백엔드 HTTP 포트. 기본값은 `18080`.
- `GOVERNANCE_LOCAL_DATASOURCE_URL`: `local` 프로파일 DB JDBC URL. 기본값은 H2 in-memory.
- `GOVERNANCE_DEV_DATASOURCE_URL`: `dev` 프로파일 DB JDBC URL.
- `GOVERNANCE_PROD_DATASOURCE_URL`: `prod` 프로파일 DB JDBC URL.
- `GOVERNANCE_DATASOURCE_URL`: 공통 DB JDBC URL. `DEV/PROD/LOCAL` 전용 값이 없을 때 fallback으로 사용합니다.
- `GOVERNANCE_*_DATASOURCE_DRIVER`: 프로파일별 DB 드라이버 클래스. Oracle 기본값은 `oracle.jdbc.OracleDriver`.
- `GOVERNANCE_*_DATASOURCE_USERNAME`: 프로파일별 DB 계정.
- `GOVERNANCE_*_DATASOURCE_PASSWORD`: 프로파일별 DB 비밀번호.
- `GOVERNANCE_SQL_INIT_MODE`: SQL 초기화 모드. `local` 기본값은 `embedded`, `dev/prod` 기본값은 `never`.
- `GOVERNANCE_H2_CONSOLE_ENABLED`: H2 console 사용 여부. `local` 기본값은 `true`, 그 외 기본값은 `false`.
- `GOVERNANCE_DATASOURCE_MAX_POOL_SIZE`: Hikari connection pool 최대 크기.
- `GOVERNANCE_DATASOURCE_MIN_IDLE`: Hikari connection pool 최소 idle 수.
- `GOVERNANCE_DATASOURCE_CONNECTION_TIMEOUT`: Hikari connection 획득 timeout(ms).
- `GOVERNANCE_MULTIPART_MAX_FILE_SIZE`: Excel 등 multipart 단일 파일 최대 크기. 기본값은 `20MB`.
- `GOVERNANCE_MULTIPART_MAX_REQUEST_SIZE`: multipart 요청 전체 최대 크기. 기본값은 `25MB`.
- `GOVERNANCE_ATTACHMENT_STORAGE_ROOT`: 첨부파일 저장 루트. 미지정 시 Java 임시 디렉터리 아래 `governance-portal/attachments`를 사용합니다.
- `GOVERNANCE_CACHE_TYPE`: Spring cache 구현. 기본값은 `redis`. 로컬에서 Redis 없이 실행할 때는 `local` profile의 기본값 `simple`을 사용합니다.
- `GOVERNANCE_CACHE_REDIS_TTL`: Redis cache TTL. 기본값은 `300s`.
- `GOVERNANCE_CACHE_REDIS_KEY_PREFIX`: Redis cache key prefix. 기본값은 `governance:`.
- `GOVERNANCE_REDIS_HOST`: Redis host. 기본값은 `127.0.0.1`.
- `GOVERNANCE_REDIS_PORT`: Redis port. 기본값은 `6379`.
- `GOVERNANCE_REDIS_DATABASE`: Redis database index. 기본값은 `0`.
- `GOVERNANCE_REDIS_PASSWORD`: Redis password.
- `GOVERNANCE_REDIS_TIMEOUT`: Redis connection timeout. 기본값은 `2s`.
- `GOVERNANCE_REDIS_HEALTH_ENABLED`: Actuator Redis health check 사용 여부. 기본값은 `true`. `local` profile은 기본값 `false`.
- `GOVERNANCE_EXTERNAL_API_CONNECT_TIMEOUT`: 외부 REST API connect timeout. 기본값은 `3s`.
- `GOVERNANCE_EXTERNAL_API_READ_TIMEOUT`: 외부 REST API read timeout. 기본값은 `10s`.
- `GOVERNANCE_EXTERNAL_API_LOGGING_ENABLED`: 외부 REST API 요약 로그 사용 여부. 기본값은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_ENABLED`: 백엔드 시작 시 프론트 dev server 실행 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_RESTART`: 기존 프론트 dev server 종료 후 재시작 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_BEFORE_START`: 프론트 dev server 시작 전 `npm.cmd run build` 실행 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_DIR`: 프론트 dev server 작업 디렉터리. 기본값은 `../frontend`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_COMMAND`: 프론트 dev server 실행 명령. 기본값은 `npm.cmd`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_ARGS`: 프론트 dev server 실행 인자. 기본값은 `run,dev`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_COMMAND`: 프론트 빌드 명령. 기본값은 `npm.cmd`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_ARGS`: 프론트 빌드 인자. 기본값은 `run,build`.
- `VITE_API_PROXY_TARGET`: 프론트 개발 서버의 `/api` 프록시 대상. 기본값은 `http://127.0.0.1:18080`.

Oracle DB 사용 시에는 런타임에 `ojdbc11` 드라이버가 포함됩니다. 개발/운영 환경에서는 프로파일에 맞는 datasource 값을 외부 환경변수 또는 env 파일로 주입합니다.

```bash
SPRING_PROFILES_ACTIVE=saml,prod
GOVERNANCE_PROD_DATASOURCE_URL=jdbc:oracle:thin:@//db-host:1521/service
GOVERNANCE_PROD_DATASOURCE_DRIVER=oracle.jdbc.OracleDriver
GOVERNANCE_PROD_DATASOURCE_USERNAME=appuser
GOVERNANCE_PROD_DATASOURCE_PASSWORD=change-me
GOVERNANCE_SQL_INIT_MODE=never
GOVERNANCE_BATCH_SCHEMA_INITIALIZE=never
```

Oracle Autonomous Database처럼 Wallet 기반 접속이 필요하면 Wallet 파일 위치와 추가 보안 companion JAR 필요 여부를 DBA/OCI 담당자와 확인합니다.

## 공통 예외/로깅/외부 API

- 백엔드는 `GlobalExceptionHandler`로 API 오류 응답을 `timestamp`, `status`, `code`, `message`, `path`, `requestId`, `fieldErrors` 형태로 통일합니다.
- `RequestIdFilter`는 `X-Request-Id`를 MDC와 응답 헤더에 반영합니다.
- `ApplicationLoggingAspect`는 Controller/Service 실행 시간과 실패를 공통 로깅합니다.
- 프론트는 `shared/api/http.ts`에서 오류 응답을 `ApiError`로 변환하고, 화면에서는 `handleApiError`를 사용합니다.
- 외부 REST API는 공통 `RestClient` 또는 `ExternalApiClientFactory`를 통해 호출하며, body 전체가 아닌 externalSystem/api/status/elapsedMs/reason/requestId만 요약 로그로 남깁니다.

## 오프라인 빌드

오프라인 빌드는 내부망 PC의 `PATH`에 설치된 Node/npm/Gradle에 의존하지 않습니다.
프론트 빌드는 `offline/nodejs/node-*-win-x64/npm.cmd`를 사용하고, 백엔드 빌드는 `backend/gradlew.bat`와 `offline/gradle-home`을 사용합니다.
상세 절차는 `docs/offline_build_guide.txt`를 기준으로 관리합니다.

인터넷이 되는 PC에서 캐시를 준비합니다.

```powershell
.\scripts\prepare-offline-bundle.ps1
```

이 단계는 npm/Gradle 의존성을 캐시에 내려받은 뒤, 같은 캐시로 오프라인 빌드가 가능한지 한 번 더 검증합니다.
EasyExcel처럼 Gradle 의존성이 추가되면 내부망 반입 전 반드시 이 단계를 다시 실행해야 합니다.

프로젝트 전체를 내부망으로 옮긴 뒤 실행합니다.

```powershell
.\scripts\build-offline-jar.ps1
```

내부망으로 옮길 때는 `offline/gradle-home`, `offline/npm-cache`, `offline/nodejs`, `frontend`, `backend`를 함께 복사해야 합니다. 개발 PC까지 같이 준비하려면 `offline/jdk`, `offline/vscode-extensions`도 함께 전달합니다.
`offline` 하위 캐시는 `.gitignore` 대상이므로 git clone만으로는 전달되지 않습니다.

## 참고 문서

- `docs/frontend/`: 프론트엔드 로컬 개발 온보딩과 화면 작성 기준
- `docs/development-screen-to-db-guide.md`: 화면에서 API, 서비스, Repository, DB까지 따라가는 개발 순회 가이드
- `docs/governance/screen-domain-template.md`: 기능목록 엑셀 기준 화면 도메인/템플릿 분류
- `docs/governance/project-start-strategy.md`: 프로젝트 투입 후 내부망 형상관리와 Oracle Linux 운영 배포 전략
- `설계/메뉴권한매핑설계.md`: 메뉴관리와 권한관리 연동 설계
- `docs/permission-management-guide.md`: IAM/AD 연동 권한관리 화면, DB, 서버단 권한 검증 기준
- `docs/excel-attachment-feature-guide.md`: 엑셀 다운로드/업로드와 첨부파일 처리 흐름
- `docs/frontend/06-eslint-guide.md`: ESLint 역할과 VSCode 설정 기준
- `docs/deployment_nginx_springboot.txt`: Nginx + Spring Boot JAR 운영 구성
- `docs/offline_build_guide.txt`: 내부망 오프라인 빌드 절차
- `개선사항/작업방향.md`: 현재 문서/작업 정리 기준
- `개선사항/frontend-si-template-completed.md`: 프론트엔드 템플릿 기준 요약
