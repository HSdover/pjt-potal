# Governance Portal

공공 의료기관 데이터의 메타데이터 카탈로그, 원천 샘플 조회, 데이터 리니지 시각화를 제공하는 데이터 거버넌스 포털 샘플 프로젝트입니다.

## 기술 구조

- Frontend: Vue 3, TypeScript, Vite, Element Plus, AG Grid, Vue Flow
- Backend: Java 21, Spring Boot 3.3, embedded Tomcat, H2 sample DB
- 운영 Web: Nginx
- 운영 실행: Spring Boot executable JAR + systemd

운영 구조는 다음을 기준으로 합니다.

```text
Client
  -> Nginx
     - /, /assets/*       : frontend/dist 정적 파일 서빙
     - /api/*             : Spring Boot API로 reverse proxy
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

## 개발 실행

### Backend

```powershell
cd backend
.\gradlew.bat bootRun
```

기본 포트는 `18080`입니다.

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

`local` 프로파일은 백엔드 시작 시 `frontend` 또는 `../frontend`에서 기존 5173 프론트 서버를 종료하고, `npm.cmd run build`를 실행한 뒤 `npm.cmd run dev`를 다시 실행합니다.

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

Nginx 설정 예시는 `infra/nginx/governance-portal.conf`에 있습니다.
systemd 서비스 예시는 `infra/systemd/governance-portal.service`에 있습니다.

## 주요 URL

| URL | 용도 |
| --- | --- |
| http://127.0.0.1:5173 | 프론트엔드 개발 서버 |
| http://localhost:18080/api/metadata | 백엔드 API 예시 |
| http://localhost:18080/h2-console | H2 개발 DB 콘솔 |
| http://localhost:18080/swagger-ui.html | Swagger UI |
| http://localhost:18080/actuator/health | 헬스 체크 |

H2 콘솔 접속 정보:

- JDBC URL: `jdbc:h2:mem:governance`
- User Name: `sa`
- Password: 없음

## 환경 변수

- `SERVER_PORT`: 백엔드 HTTP 포트. 기본값은 `18080`.
- `GOVERNANCE_DATASOURCE_URL`: DB JDBC URL. 기본값은 H2 in-memory.
- `GOVERNANCE_DATASOURCE_DRIVER`: DB JDBC 드라이버 클래스. 기본값은 `org.h2.Driver`.
- `GOVERNANCE_DATASOURCE_USERNAME`: DB 계정.
- `GOVERNANCE_DATASOURCE_PASSWORD`: DB 비밀번호.
- `GOVERNANCE_SQL_INIT_MODE`: SQL 초기화 모드. 기본값은 `embedded`.
- `GOVERNANCE_H2_CONSOLE_ENABLED`: H2 console 사용 여부. 기본값은 `true`.
- `GOVERNANCE_CACHE_TYPE`: Spring cache 구현. 기본값은 `simple`, Redis 사용 시 `redis`.
- `GOVERNANCE_CACHE_REDIS_TTL`: Redis cache TTL. 기본값은 `300s`.
- `GOVERNANCE_CACHE_REDIS_KEY_PREFIX`: Redis cache key prefix. 기본값은 `governance:`.
- `GOVERNANCE_REDIS_HOST`: Redis host. 기본값은 `127.0.0.1`.
- `GOVERNANCE_REDIS_PORT`: Redis port. 기본값은 `6379`.
- `GOVERNANCE_REDIS_DATABASE`: Redis database index. 기본값은 `0`.
- `GOVERNANCE_REDIS_PASSWORD`: Redis password.
- `GOVERNANCE_REDIS_TIMEOUT`: Redis connection timeout. 기본값은 `2s`.
- `GOVERNANCE_REDIS_HEALTH_ENABLED`: Actuator Redis health check 사용 여부. 기본값은 `false`.
- `GOVERNANCE_SAMPLE_DATA_ENABLED`: HIRA 샘플 CSV 자동 적재 여부. 기본값은 `true`.
- `GOVERNANCE_HIRA_SAMPLE_CSV`: 샘플 CSV 파일 경로.
- `GOVERNANCE_FRONTEND_DEV_SERVER_ENABLED`: 백엔드 시작 시 프론트 dev server 실행 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_RESTART`: 기존 프론트 dev server 종료 후 재시작 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_BEFORE_START`: 프론트 dev server 시작 전 `npm.cmd run build` 실행 여부. 기본값은 `false`, `local` profile은 `true`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_DIR`: 프론트 dev server 작업 디렉터리. 기본값은 `../frontend`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_COMMAND`: 프론트 dev server 실행 명령. 기본값은 `npm.cmd`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_ARGS`: 프론트 dev server 실행 인자. 기본값은 `run,dev`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_COMMAND`: 프론트 빌드 명령. 기본값은 `npm.cmd`.
- `GOVERNANCE_FRONTEND_DEV_SERVER_BUILD_ARGS`: 프론트 빌드 인자. 기본값은 `run,build`.
- `VITE_API_PROXY_TARGET`: 프론트 개발 서버의 `/api` 프록시 대상. 기본값은 `http://127.0.0.1:18080`.

## 오프라인 빌드

오프라인 빌드는 내부망 PC의 `PATH`에 설치된 Node/npm/Gradle에 의존하지 않습니다.
프론트 빌드는 `offline/nodejs/node-*-win-x64/npm.cmd`를 사용하고, 백엔드 빌드는 `backend/gradlew.bat`와 `offline/gradle-home`을 사용합니다.

인터넷이 되는 PC에서 캐시를 준비합니다.

```powershell
.\scripts\prepare-offline-bundle.ps1
```

이 단계는 npm/Gradle 의존성을 캐시에 내려받은 뒤, 같은 캐시로 오프라인 빌드가 가능한지 한 번 더 검증합니다.

프로젝트 전체를 내부망으로 옮긴 뒤 실행합니다.

```powershell
.\scripts\build-offline-jar.ps1
```

내부망으로 옮길 때는 `offline/gradle-home`, `offline/npm-cache`, `offline/nodejs`, `frontend`, `backend`, `data`를 함께 복사해야 합니다.
`offline` 하위 캐시는 `.gitignore` 대상이므로 git clone만으로는 전달되지 않습니다.

## 참고 문서

- `docs/project_inventory.txt`: 프로젝트 구조와 주요 파일 정리
- `docs/frontend/`: 프론트엔드 로컬 개발 온보딩과 화면 작성 기준
- `docs/deployment_nginx_springboot.txt`: Nginx + Spring Boot JAR 운영 구성
- `docs/offline_build_guide.txt`: 내부망 오프라인 빌드 절차
- `개선사항/frontend-si-template.md`: SI 프로젝트용 프론트엔드 템플릿화 기획
- `개선사항/frontend-si-template-completed.md`: 프론트엔드 템플릿화 완료 내역
