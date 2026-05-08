# Governance Portal

거버넌스포털 사업수행계획서 1.3, 1.4의 구현 기술 스펙을 기준으로 구성한 로컬 개발용 프로젝트입니다.

## 사전 요구사항

| 도구 | 필수 버전 | 확인 방법 |
|------|-----------|-----------|
| Java JDK | **21** | `java -version` → `21.x.x` 출력 확인 |
| Node.js | **20.19 이상 또는 22.12 이상** | `node -v` 확인. 프론트 독립 실행 시 필요 |
| Gradle | 불필요 | `gradlew.bat` / `gradlew` Wrapper 사용 |

> Java 21이 없으면 `.\gradlew.bat bootRun` 실행 시 빌드 오류가 납니다.  
> [Adoptium Temurin 21](https://adoptium.net/) 또는 Oracle JDK 21을 설치하세요.

## 작업 방식

- 서버: IntelliJ IDEA에서 `backend` 폴더를 Gradle 프로젝트로 열어 작업합니다.
- 프론트: VS Code에서 `frontend` 폴더를 열어 작업합니다.
- 운영 WAS: Jakarta EE 지원 WAS인 JEUS 9 기준 WAR 배포를 사용합니다.
- 운영 Web: WebtoB는 JEUS로 요청을 전달하고, Spring Boot WAR가 Vue 화면과 `/api/*`를 함께 제공합니다.

## 개발 실행

### Backend

```powershell
cd backend
.\gradlew.bat bootRun
```

### Frontend

```powershell
cd frontend
npm install
npm run dev
```

다른 장비에서 개발 서버에 접근해야 할 때만 다음처럼 전체 인터페이스 바인딩을 사용합니다.

```powershell
npm run dev -- --host 0.0.0.0
```

백엔드 포트를 바꿔 실행하는 경우 프론트엔드 개발 서버는 다음처럼 프록시 대상을 지정합니다.

```powershell
$env:VITE_API_PROXY_TARGET="http://localhost:18080"
npm run dev
```

## 운영 빌드

```powershell
cd backend
.\gradlew.bat bootWar
```

생성 산출물은 `backend/build/libs/governance-portal-backend.war`입니다. Gradle이 프론트 의존성 설치와 `npm run build`를 자동 수행한 뒤 Vue 빌드 파일을 `backend/build/generated/frontend/static`에 복사하고, 이를 WAR 내부 정적 리소스로 포함합니다.

## 로컬 접속 URL

| URL | 용도 |
|-----|------|
| http://localhost:8080 | 메인 앱 (통합 실행 시) |
| http://localhost:5173 | 메인 앱 (프론트 독립 실행 시) |
| http://localhost:8080/h2-console | H2 인메모리 DB 콘솔 |
| http://localhost:8080/swagger-ui.html | REST API 문서 |

**H2 콘솔 접속 정보:**
- JDBC URL: `jdbc:h2:mem:governance`
- User Name: `sa`
- Password: (없음, 공백)

## IntelliJ IDEA에서 백엔드 실행

IntelliJ에서 `GovernancePortalApplication.main()`을 직접 실행하면 대부분은 자동으로 동작합니다.  
샘플 CSV 탐색 순서: `GOVERNANCE_HIRA_SAMPLE_CSV` 환경변수 → `data/processed/hira/` → `../data/processed/hira/` → classpath

CSV를 못 찾는다는 오류가 발생하면 Run Configuration에 환경변수를 추가합니다:

```
GOVERNANCE_HIRA_SAMPLE_CSV=C:\<프로젝트 경로>\data\processed\hira\hira_institution_open_status_sample_1000_h2.csv
```

또는 `.\gradlew.bat bootRun`을 사용하면 Gradle이 CSV를 classpath에 자동 복사하므로 항상 동작합니다.

## 환경 변수

- `SERVER_PORT`: 백엔드 HTTP 포트. 기본값은 `8080`.
- `GOVERNANCE_DATASOURCE_URL`: 외부 DB 또는 파일 H2를 사용할 때 지정하는 JDBC URL. 기본값은 메모리 H2입니다.
- `GOVERNANCE_DATASOURCE_USERNAME`, `GOVERNANCE_DATASOURCE_PASSWORD`: DB 계정.
- `GOVERNANCE_SQL_INIT_MODE`: SQL 초기화 모드. 기본값은 `embedded`이며 H2 같은 내장 DB에서만 샘플 스키마를 초기화합니다.
- `GOVERNANCE_H2_CONSOLE_ENABLED`: H2 console 사용 여부. 기본값은 `true`.
- `GOVERNANCE_SAMPLE_DATA_ENABLED`: HIRA 샘플 CSV 자동 적재 여부. 기본값은 `true`.
- `GOVERNANCE_HIRA_SAMPLE_CSV`: 샘플 CSV를 기본 위치가 아닌 곳에서 읽어야 할 때 지정하는 파일 경로.
- `VITE_API_PROXY_TARGET`: 프론트엔드 개발 서버의 `/api` 프록시 대상. 기본값은 `http://localhost:8080`.

## 내부망 오프라인 빌드

외부망 PC에서 먼저 패키지 캐시를 준비합니다.

```powershell
.\scripts\prepare-offline-bundle.ps1
```

그 뒤 프로젝트 폴더 전체를 내부망으로 옮기고 다음 명령으로 빌드합니다.

```powershell
.\scripts\build-offline-war.ps1
```

## 참고 문서

- `docs/project_inventory.txt`: 패키지, 파일 경로, 역할 정리
- `docs/deployment_jeus_webtob.txt`: WebtoB + JEUS 운영 구성 정리
- `docs/offline_build_guide.txt`: 내부망 오프라인 빌드 절차
