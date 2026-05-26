# OCI Oracle Linux 운영 구조 설명

## 현재 환경 이해

현재 운영 환경은 로컬 PC 안에 Oracle Linux VM을 띄우는 구조가 아니다.

구조는 아래와 같다.

```text
Windows PC
  |
  | 인터넷, 회사망, VPN, Bastion 중 하나
  |
OCI Oracle Cloud
  |
  |-- VCN/Subnet
      |
      |-- Oracle Linux Compute VM
            - Java, Nginx, systemd 설치
            - frontend dist, backend JAR 배포
            - SSH/SFTP로 접속해서 운영 작업
```

즉, Oracle Linux 서버는 Windows PC 안에 있는 것이 아니라 OCI 클라우드 안에 떠 있는 원격 서버다. Windows PC는 소스 빌드, 배포 파일 준비, SSH/SFTP 접속을 수행하는 작업용 PC 역할이다.

## 구성요소

| 구분 | 의미 | 이 프로젝트에서 하는 일 |
|---|---|---|
| Windows PC | 작업자 PC | 소스 수정, 빌드, 배포 zip 생성, SSH/SFTP 접속 |
| OCI | Oracle Cloud Infrastructure | 서버, 네트워크, 방화벽, 공인 IP, 디스크 제공 |
| VCN | OCI 안의 가상 네트워크 | Compute VM이 속한 클라우드 네트워크 |
| Subnet | VCN 안의 서버 배치 구역 | Public 또는 Private 접근 구조 결정 |
| Compute Instance | OCI에 떠 있는 Oracle Linux 서버 | Nginx, Spring Boot 실행 |
| Public IP | 외부에서 접속 가능한 IP | SSH, HTTP/HTTPS 접근에 사용 |
| Private IP | OCI 내부 통신용 IP | 내부 시스템 연계, LB 연계에 사용 |
| Security List/NSG | OCI 레벨 방화벽 | 22, 80, 443 등 인바운드 허용 제어 |
| firewalld | Oracle Linux 내부 방화벽 | 서버 OS 내부 포트 허용 제어 |
| Nginx | 웹 서버/reverse proxy | 정적 프론트 서빙, API/SAML 요청 프록시 |
| Spring Boot | 백엔드 애플리케이션 | API, SAML ACS, 세션, 권한 처리 |
| Redis | 캐시 저장소 | 운영 캐시, 조회성 샘플 API 캐시 |
| Oracle DB/ADB | 운영 DB | 업무 데이터, Spring Batch 메타테이블 저장 |
| 외부 REST API | OCI/외부 모듈 연계 | 포털 백엔드가 BFF로 호출 |
| systemd | Linux 서비스 관리자 | Spring Boot JAR 실행/재시작/상태관리 |

## 네트워크 구조

운영 접근 흐름은 아래와 같다.

```text
사용자 브라우저
  |
  | HTTPS 443 또는 HTTP 80
  v
OCI Public IP 또는 포털 도메인
  |
  v
OCI Security List / NSG
  |
  v
Oracle Linux VM
  |
  |-- firewalld
  |
  |-- Nginx :80/443
  |     |
  |     | /                 -> /var/www/governance-portal/index.html
  |     | /assets/*         -> /var/www/governance-portal/assets/*
  |     | /api/*            -> 127.0.0.1:18080
  |     | /saml2/*          -> 127.0.0.1:18080
  |     | /login/*          -> 127.0.0.1:18080
  |
  |-- Spring Boot :18080
        |
        |-- /api/*
        |-- /api/me
        |-- /saml2/authenticate/knox
        |-- /login/saml2/sso/knox
        |-- /actuator/health
        |
        |-- Oracle DB/ADB
        |-- Redis cache
        |-- OCI/외부 모듈 REST API
```

중요한 기준은 Spring Boot 포트 `18080`을 외부에 직접 열지 않는 것이다. 외부 사용자는 Nginx의 `80/443`으로만 접근하고, Nginx가 내부의 Spring Boot로 요청을 넘긴다.

Nginx는 `X-Request-Id`를 백엔드에 전달하고, 백엔드는 같은 값을 MDC와 응답 헤더에 남긴다. 장애 대응 시에는 이 requestId로 Nginx access log, Spring Boot 로그, 외부 REST API 로그를 함께 조회한다.

## 포트 기준

| 포트 | 위치 | 용도 | 외부 공개 여부 |
|---|---|---|---|
| 22 | Oracle Linux SSH | 서버 접속 | 작업 PC/VPN/Bastion에서만 허용 |
| 80 | Nginx | HTTP | 필요 시 허용. 운영은 443 권장 |
| 443 | Nginx | HTTPS | 운영 사용자 접근 |
| 18080 | Spring Boot | 백엔드 API | 외부 공개 금지. `127.0.0.1` 내부 접근만 |

접속이 되려면 OCI Security List/NSG와 Oracle Linux `firewalld` 양쪽에서 허용되어야 한다.

## Windows PC의 역할

Windows PC에서는 아래 작업을 한다.

1. 소스 수정
2. 프론트/백엔드 빌드
3. 배포 zip 생성
4. FileZilla 또는 MobaXterm으로 SFTP 업로드
5. MobaXterm 또는 SSH로 서버 접속
6. 서버 명령 실행
7. 브라우저/Postman/curl로 검증

Windows PC에 필요한 유틸은 `C:\workspace\governance-portal-operational-kit\01-client-tools`에 용도별로 정리한다.

## Oracle Linux 서버의 역할

Oracle Linux 서버에서는 아래 작업을 한다.

1. Java 21 설치
2. Nginx 설치
3. firewalld/chrony/logrotate 설정
4. `/var/www/governance-portal`에 프론트 정적 파일 배치
5. `/opt/governance-portal`에 백엔드 JAR와 설정 파일 배치
6. systemd로 백엔드 서비스 등록
7. Nginx reverse proxy 설정
8. 로그와 health check 확인

운영 서버에는 전체 소스를 올리는 것이 아니라 산출물과 설정 파일만 올리는 것을 기본으로 한다.

## 배포 파일 흐름

```text
Windows PC
  |
  | build-production.ps1
  v
frontend/dist
backend/build/libs/governance-portal-backend.jar
  |
  | release zip 생성
  v
SFTP 업로드
  |
  v
Oracle Linux /tmp 또는 releases 디렉터리
  |
  | 반영
  v
/var/www/governance-portal
/opt/governance-portal/governance-portal-backend.jar
```

필수 업로드 대상:

```text
frontend/dist/**
backend/build/libs/governance-portal-backend.jar
infra/nginx/governance-portal.conf
infra/systemd/governance-portal.service
```

운영 전용 보안/설정 파일은 별도로 준비한다.

```text
/opt/governance-portal/config/governance-portal.env
TLS 인증서/키
SAML IdP metadata 또는 metadata URL
DB 접속 정보
Oracle Wallet, 필요 시
```

Oracle DB를 사용할 경우 백엔드 JAR에는 `ojdbc11` JDBC 드라이버가 포함되어야 한다. 현재 프로젝트는 Oracle DB 전환을 고려해 `com.oracle.database.jdbc:ojdbc11` 런타임 의존성을 사용한다. 운영 env에는 `GOVERNANCE_DATASOURCE_URL`, `GOVERNANCE_DATASOURCE_DRIVER=oracle.jdbc.OracleDriver`, DB 계정/비밀번호를 주입한다.

Oracle Autonomous Database처럼 Wallet 기반 접속을 사용할 때는 Wallet 파일과 Oracle security companion JAR 필요 여부를 DBA/OCI 담당자와 별도로 확인한다.

Spring Batch를 Oracle DB에서 사용할 경우 `BATCH_*` 메타테이블이 필요하다. 운영에서는 DBA가 Spring Batch 공식 Oracle schema로 선생성하고 `GOVERNANCE_BATCH_SCHEMA_INITIALIZE=never`를 유지하는 방식을 우선한다. 최초 1회 자동 생성이 필요한 경우에만 임시로 `always`를 쓰고, 생성 후 반드시 `never`로 되돌린다.

Redis cache를 사용할 경우 운영 env에는 아래 값이 필요하다.

```text
GOVERNANCE_CACHE_TYPE=redis
GOVERNANCE_REDIS_HOST=redis-host
GOVERNANCE_REDIS_PORT=6379
GOVERNANCE_REDIS_DATABASE=0
GOVERNANCE_REDIS_PASSWORD=
GOVERNANCE_REDIS_HEALTH_ENABLED=true
```

로컬 개발(`local` profile)은 Redis 없이도 실행되도록 기본 캐시가 `simple`이다. 운영(`saml,adb`)은 Redis 접속이 실패하면 health check 또는 캐시 사용 구간에서 문제가 드러날 수 있으므로, 서비스 기동 전에 `nc -zv redis-host 6379`로 도달성을 확인한다.

## 외부 REST API 연계 구조

이 프로젝트는 OCI와 외부 모듈 데이터를 REST API로 받아올 가능성이 높다. 운영 구조에서는 브라우저가 외부 시스템을 직접 호출하지 않고 Spring Boot가 BFF 역할로 호출한다.

```text
Frontend
-> Spring Boot Backend
-> OCI / 외부 모듈 REST API
```

현재 백엔드에는 공통 `RestClient + HTTP Interface` 기반 외부 API 호출 구조가 들어가 있다.

- `ExternalApiClientFactory`: 선언형 HTTP client 생성
- `ExternalApiLoggingInterceptor`: 외부 호출 요약 로그, requestId 전파
- `ExternalApiException`: 외부 API 실패를 공통 오류 응답으로 변환

운영 env에서 기본 timeout과 로깅 여부를 설정한다.

```text
GOVERNANCE_EXTERNAL_API_CONNECT_TIMEOUT=3s
GOVERNANCE_EXTERNAL_API_READ_TIMEOUT=10s
GOVERNANCE_EXTERNAL_API_LOGGING_ENABLED=true
```

외부 API 로그는 externalSystem, api, status, elapsedMs, reason, requestId만 남기고 request/response body 전체는 남기지 않는다.

## 개발 DB 전환 방식

개발 환경에서는 H2와 ADB를 profile로 전환한다. `application.yml`을 직접 고쳐가며 바꾸지 않는다.

기본값은 H2다.

```text
spring.datasource.url=jdbc:h2:mem:governance;MODE=Oracle;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
```

H2 개발 실행:

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
cd C:\workspace\governance-portal\backend
.\gradlew.bat bootRun
```

ADB 개발 실행은 `adb` profile을 추가한다.

```powershell
$env:SPRING_PROFILES_ACTIVE="local,adb"
$env:GOVERNANCE_DATASOURCE_URL="jdbc:oracle:thin:@db_high?TNS_ADMIN=C:/workspace/secrets/adb-wallet"
$env:GOVERNANCE_DATASOURCE_USERNAME="appuser"
$env:GOVERNANCE_DATASOURCE_PASSWORD="password"

cd C:\workspace\governance-portal\backend
.\gradlew.bat bootRun
```

일반 Oracle DB에 직접 접속하는 경우:

```powershell
$env:SPRING_PROFILES_ACTIVE="local,adb"
$env:GOVERNANCE_DATASOURCE_URL="jdbc:oracle:thin:@//db-host:1521/service"
$env:GOVERNANCE_DATASOURCE_USERNAME="appuser"
$env:GOVERNANCE_DATASOURCE_PASSWORD="password"
```

H2로 되돌릴 때는 `adb` profile과 DB 환경변수를 제거한다.

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
Remove-Item Env:\GOVERNANCE_DATASOURCE_URL -ErrorAction SilentlyContinue
Remove-Item Env:\GOVERNANCE_DATASOURCE_USERNAME -ErrorAction SilentlyContinue
Remove-Item Env:\GOVERNANCE_DATASOURCE_PASSWORD -ErrorAction SilentlyContinue
```

ADB profile이 켜지면 H2 console은 꺼지고 SQL init은 `never`로 고정된다. 현재 `ddl-auto`는 `none`이므로 ADB에 테이블이 없으면 sample API가 실패할 수 있다.

## SAML 흐름

SAML SSO가 적용되면 로그인 흐름은 아래와 같다.

```text
사용자
-> 포털 접속
-> 로그인 필요
-> /saml2/authenticate/knox
-> IAM/KNOX 로그인 화면
-> 인증 성공
-> /login/saml2/sso/knox 로 SAMLResponse 전달
-> Spring Boot가 SAML 검증 후 세션 생성
-> 프론트가 /api/me로 사용자/권한 조회
```

IAM/KNOX에 등록할 운영 주소 예시:

```text
SP Metadata:
https://포털도메인/saml2/service-provider-metadata/knox

ACS URL:
https://포털도메인/login/saml2/sso/knox

Login Entry:
https://포털도메인/saml2/authenticate/knox
```

이 경로들이 동작하려면 Nginx가 `/saml2/`, `/login/` 요청을 Spring Boot로 프록시해야 한다.

## 작업 순서 요약

1. Windows PC에 운영 유틸 설치
2. OCI 접속 정보 확인: IP, SSH key, 계정, Bastion/VPN 여부
3. OCI Security List/NSG 확인: 22, 80, 443
4. Oracle Linux 접속
5. Java 21, Nginx, firewalld, chrony, logrotate 설치
6. 운영 계정과 디렉터리 생성
7. Windows PC에서 프로젝트 빌드
8. release zip 생성
9. SFTP로 서버 업로드
10. 프론트 dist와 백엔드 JAR 반영
11. env, Nginx, systemd 설정 반영
12. 서비스 재시작
13. health check 확인
14. 브라우저 접속 확인
15. SAML, DB, TLS는 실제 운영 정보가 확정된 뒤 검증

## 로컬 VM과 다른 점

| 구분 | 로컬 VM | OCI Oracle Linux 서버 |
|---|---|---|
| 위치 | Windows PC 내부 | OCI 클라우드 |
| 접속 | Host-only/NAT/Bridged | SSH, VPN, Bastion, Public IP |
| 네트워크 제어 | VM 네트워크 설정 | VCN, Subnet, NSG, Security List |
| 보안 | 개인 실습 중심 | 운영 보안 정책 적용 |
| 도메인/TLS | 생략 가능 | 운영에서는 필요 |
| SAML | 완전 검증 어려움 | 운영 도메인 기준 검증 |

현재 환경은 로컬 VM이 아니라 OCI에 떠 있는 원격 서버이므로, OCI 네트워크와 서버 내부 설정을 함께 봐야 한다.

## 핵심 판단

이 프로젝트의 운영 구조는 아래 한 줄로 정리할 수 있다.

```text
Windows PC에서 빌드와 업로드를 수행하고, OCI Oracle Linux Compute VM에서 Nginx와 Spring Boot JAR를 운영하는 구조
```

따라서 앞으로 작업할 때는 항상 아래 네 영역을 분리해서 생각한다.

1. Windows PC 작업 도구
2. OCI 네트워크/보안그룹
3. Oracle Linux 서버 설정
4. 애플리케이션 배포 파일과 운영 보안 설정
