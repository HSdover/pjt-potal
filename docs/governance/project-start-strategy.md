# Project Start Strategy

작성일: 2026-05-19
최종수정: 2026-05-26

이 문서는 프로젝트 투입 후 별도로 준비해야 할 기반 전략 초안이다.
현재 화면 도메인/템플릿 분류 단계에서는 상세 설계하지 않고, 누락되지 않도록 작업 후보와 검토 항목만 정리한다.

2026-05-26 기준으로 운영 실행 절차, SAML SSO, Oracle/Redis env, 예외/로깅, 외부 REST API 공통 클라이언트는 별도 문서와 코드에 반영되어 있다. 이 문서는 현장 투입 후 내부 정책과 실제 운영 값을 확정해야 하는 항목을 관리하는 용도로 유지한다.

## 1. 내부망 형상관리 전략

### 목적

프로젝트가 내부망으로 들어간 뒤 소스 변경, 리뷰, 릴리즈, 산출물 이력을 일관되게 관리하기 위한 기준을 만든다.

### 확인할 항목

| 구분 | 확인 내용 |
|---|---|
| 형상관리 도구 | 내부 Git 저장소 종류, 접근 방식, 계정/권한 정책 |
| 저장소 구조 | 단일 저장소로 갈지, 애플리케이션/인프라 문서를 분리할지 |
| 브랜치 전략 | `main`, `develop`, `release/*`, `feature/*`, `hotfix/*` 사용 여부 |
| 리뷰 기준 | Merge Request 승인자, 리뷰 필수 여부, 보호 브랜치 |
| 릴리즈 기준 | 태그 규칙, 릴리즈 노트, 배포 산출물 버전명 |
| 반입/반출 | 외부 PC에서 내부망으로 소스/라이브러리 반입하는 절차 |
| 의존성 캐시 | npm, Gradle 의존성 캐시, `offline/` 반입 방식 또는 내부 Nexus/Artifactory 사용 여부 |
| 민감정보 관리 | `.env`, 인증서, 접속 정보, 운영 설정의 저장 금지 기준 |

### 산출물 후보

- 내부망 형상관리 가이드
- 브랜치/태그/릴리즈 규칙
- 코드리뷰 체크리스트
- 반입/반출 체크리스트
- 의존성 캐시 준비 절차
- 인터넷 가능 빌드와 내부망 오프라인 빌드 분기 절차

## 2. OCI Oracle Linux 운영 배포 전략

### 목적

오라클클라우드의 Oracle Linux 서버에서 포털을 운영 배포하기 위한 절차와 책임 범위를 정리한다.

### 기본 배포 관점

현재 프로젝트 구조 기준으로는 아래 배포 형태를 후보로 둔다.

| 구성 | 후보 |
|---|---|
| 운영 OS | Oracle Linux |
| 실행 서버 | OCI Compute |
| 프론트엔드 | 정적 빌드 산출물을 Nginx에서 서빙 |
| 백엔드 | Spring Boot 실행 JAR |
| DB | Oracle DB/ADB, H2는 local 개발용 |
| 캐시 | Redis 운영 캐시, local profile은 simple cache |
| 서비스 관리 | systemd |
| 프록시 | Nginx reverse proxy |
| 설정 관리 | `/opt/governance-portal/config/governance-portal.env` |
| 인증 | SAML2 Service Provider, IAM/KNOX 연동 |
| 외부 연계 | 백엔드 BFF + RestClient/HTTP Interface |

### 확인할 항목

| 구분 | 확인 내용 |
|---|---|
| 서버 접근 | SSH 접근 방식, 배포 계정, sudo 권한 |
| 네트워크 | VCN, subnet, security list, load balancer 사용 여부 |
| 런타임 | JDK 설치 방식, Node/npm이 운영 서버에 필요한지 여부 |
| 배포 위치 | frontend 정적 파일 경로, backend JAR 경로 |
| 환경 설정 | datasource, `SERVER_ADDRESS=127.0.0.1`, 포트, 외부 연계 주소, secret 주입 방식 |
| Redis | host, port, database, password, health check 정책 |
| SAML | IdP metadata, ACS URL, 그룹 attribute, SLO 요구 여부 |
| Oracle Batch | Spring Batch `BATCH_*` 메타테이블 선생성 방식 |
| 외부 API | 시스템별 base URL, 인증 방식, timeout, egress 정책 |
| 서비스 관리 | systemd unit, 재시작 정책, health check |
| 로그 | requestId 기준 Nginx/Spring/외부 API 로그, logrotate 기준 |
| 백업/롤백 | 이전 JAR/dist 유지, 장애 시 되돌리는 절차 |
| 운영 보안 | TLS, 방화벽, 계정 권한, 운영 Swagger/Actuator 노출 여부 |

### 산출물 후보

- Oracle Linux 운영 배포 가이드
- 서버 디렉터리 구조
- systemd 서비스 파일 기준
- Nginx HTTP/HTTPS 설정 기준
- 배포 체크리스트
- 롤백 체크리스트
- 운영 점검 체크리스트

## 3. 현재 단계의 결정

| 항목 | 결정 |
|---|---|
| 내부망 형상관리 | 프로젝트 투입 후 내부 정책 확인 전까지 상세 설계 보류 |
| Oracle Linux 운영 배포 | 기본 실행 절차는 `개선사항/운영작업문서-서버실행-AZ.md`에 상세화 완료. 현장 계정/경로/보안정책은 투입 후 치환 |
| SAML SSO | SAML2 SP 방식 확정. IAM/KNOX 실제 metadata와 그룹 매핑은 투입 후 확정 |
| 권한관리 | IAM 기본 3개 역할과 포털 권한 DB 구조 구현. 실제 IAM/AD 그룹명은 투입 후 `portal_iam_role.external_group_name`에 반영 |
| 외부 REST API | 공통 RestClient/HTTP Interface 구조 확정. 시스템별 endpoint/인증/성공판정은 투입 후 확정 |
| 예외/로깅 | GlobalExceptionHandler, requestId, AOP 로깅, 프론트 handleApiError 기준 반영 |
| 현재 문서 역할 | 후속 작업 누락 방지를 위한 현장 확인 항목 관리 |

## 4. 현재 상세화하지 않는 항목

아래 항목은 화면 도메인/템플릿 분류 단계에서는 결정하지 않는다.
프로젝트 투입 후 실제 내부망 정책, 운영 서버 구성, 보안 기준이 확인되면 별도 상세화한다.

| 항목 | 상세화 시점 |
|---|---|
| API 엔드포인트, request/response | 상세 기능 설계 착수 후 |
| 테이블명, 컬럼명, ERD | 데이터 저장 범위 확정 후 |
| 권한 변경 감사 정책 | 보안/계정 연계 기준 확인 후 |
| 외부 시스템별 호출 상세 | OCI, Dataiku, GenON, Anyflow 등 연계 요건 확인 후 |
| 배치, 큐, DLQ, webhook | 비동기 처리와 실패 재처리 요건 확인 후 |
| 운영 Swagger/Actuator 노출 | 운영 보안 정책 확인 후 |
