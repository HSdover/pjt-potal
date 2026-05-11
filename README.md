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

`local` 프로파일은 백엔드 시작 시 `frontend` 또는 `../frontend`에서 `npm.cmd run dev`를 실행합니다. 이미 5173 포트에 프론트 서버가 떠 있으면 새로 실행하지 않습니다.

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
- 이미 떠 있는 `npm run dev` 개발 서버의 프록시 설정은 `npm run build`로 갱신되지 않습니다.
- 5173 포트에서 API가 예전 대상으로 붙는다면 기존 개발 서버를 `Ctrl+C`로 종료한 뒤 `npm run dev`를 다시 실행해야 합니다.

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
| http://localhost:18080/api/catalog/metadata | 백엔드 API 예시 |
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
- `GOVERNANCE_DATASOURCE_USERNAME`: DB 계정.
- `GOVERNANCE_DATASOURCE_PASSWORD`: DB 비밀번호.
- `GOVERNANCE_SQL_INIT_MODE`: SQL 초기화 모드. 기본값은 `embedded`.
- `GOVERNANCE_H2_CONSOLE_ENABLED`: H2 console 사용 여부. 기본값은 `true`.
- `GOVERNANCE_SAMPLE_DATA_ENABLED`: HIRA 샘플 CSV 자동 적재 여부. 기본값은 `true`.
- `GOVERNANCE_HIRA_SAMPLE_CSV`: 샘플 CSV 파일 경로.
- `VITE_API_PROXY_TARGET`: 프론트 개발 서버의 `/api` 프록시 대상. 기본값은 `http://127.0.0.1:18080`.

## 오프라인 빌드

인터넷이 되는 PC에서 캐시를 준비합니다.

```powershell
.\scripts\prepare-offline-bundle.ps1
```

프로젝트 전체를 내부망으로 옮긴 뒤 실행합니다.

```powershell
.\scripts\build-offline-jar.ps1
```

## 참고 문서

- `docs/project_inventory.txt`: 프로젝트 구조와 주요 파일 정리
- `docs/deployment_nginx_springboot.txt`: Nginx + Spring Boot JAR 운영 구성
- `docs/offline_build_guide.txt`: 내부망 오프라인 빌드 절차
- `개선사항/frontend-si-template.md`: SI 프로젝트용 프론트엔드 템플릿화 기획
