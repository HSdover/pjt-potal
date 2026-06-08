# Permission Management Guide

작성일: 2026-06-08

이 문서는 시스템 관리 > 권한관리 화면과 IAM/AD 연동 기준의 권한 처리 구조를 정리한다.

## 1. 기능 범위

권한관리는 IAM에서 내려오는 최초 3개 역할과 포털 내부 권한 코드를 연결한다.

기본 IAM 역할:

- `ADMIN`: 관리자
- `AI_AGENT_ADMIN`: AI 에이전트관리자
- `DATA_ADMIN`: 데이터 관리자

제어 대상:

- 메뉴/화면 조회 권한
- CRUD 권한
- 다운로드 권한
- 업로드 권한
- 관리자 기능 권한

## 2. DB 구조

로컬 H2 기준 DDL은 `backend/src/main/resources/schema.sql`에 있다.

| 테이블 | 용도 |
|---|---|
| `portal_permission` | 포털 내부 권한 코드 정의 |
| `portal_iam_role` | IAM 역할 코드와 외부 IAM/AD 그룹명 매핑 |
| `portal_permission_assignment` | IAM 역할, 그룹, 사용자별 허용 권한 |

초기 데이터는 `backend/src/main/resources/data.sql`에서 생성한다.

핵심 권한 코드 예:

| 권한 코드 | 유형 | 용도 |
|---|---|---|
| `DASHBOARD_READ` | SCREEN | 대시보드 메뉴 조회 |
| `META_VIEW` | SCREEN | 메타관리 조회 |
| `PIPELINE_READ` | SCREEN | 파이프라인목록 조회 |
| `AI_AGENT_READ` | SCREEN | AI 에이전트 매핑/데이터 매쉬 조회 |
| `REQUEST_READ` | SCREEN | 신청관리 조회 |
| `DP_VIEWER_READ` | SCREEN | 문서 파싱 목록 조회 |
| `NOTICE_READ` | SCREEN | 공지사항 조회 |
| `PERMISSION_MANAGE` | ADMIN | 권한관리 화면/API 사용 |
| `SAMPLE_JPA_EXPORT` | DOWNLOAD | JPA 샘플 엑셀/대용량 다운로드 |
| `SAMPLE_JPA_IMPORT` | UPLOAD | JPA 샘플 엑셀 업로드 |

## 3. 백엔드 처리 흐름

주요 파일:

- `backend/src/main/java/com/example/governanceportal/system/permission/api/PortalPermissionManagementController.java`
- `backend/src/main/java/com/example/governanceportal/system/permission/service/PortalPermissionManagementService.java`
- `backend/src/main/java/com/example/governanceportal/user/service/PortalPermissionService.java`
- `backend/src/main/java/com/example/governanceportal/config/SecurityConfig.java`

API:

| Method | URL | 설명 | 필요 권한 |
|---|---|---|---|
| `GET` | `/api/system/permissions` | 권한 코드, 대상, 할당 목록 조회 | `PERMISSION_MANAGE` |
| `PUT` | `/api/system/permissions/assignments` | 대상별 권한 저장 | `PERMISSION_MANAGE` |

SAML 로그인 사용자는 다음 순서로 권한을 계산한다.

1. SAML attribute에서 사용자 ID 후보를 읽는다: `uid`, `employeeNumber`, `sAMAccountName`, NameID
2. SAML `roles`, `groups`, `memberOf` 값을 외부 subject로 수집한다.
3. `portal_permission_assignment.subject_type = 'USER'`에서 사용자별 권한을 찾는다.
4. `subject_type = 'GROUP'`에서 AD 그룹별 권한을 찾는다.
5. `portal_iam_role.external_group_name`, `role_name`, `role_code`와 IAM/AD 그룹 값을 매칭해 `IAM_ROLE` 권한을 찾는다.
6. `application-saml.yml`의 `group-permission-mappings`는 보조 fallback으로만 사용한다.

로컬 로그인 사용자는 `application-local.yml`의 계정별 권한 목록을 세션 권한으로 사용한다. 따라서 권한관리 화면에서 DB 권한을 수정해도 기존 `local-dev`, `local-admin` 권한에는 즉시 영향을 주지 않는다. IAM/SAML 로그인에서는 DB 권한 테이블 기준으로 적용된다.

## 4. 프론트 처리 흐름

주요 파일:

- `frontend/src/router/index.ts`
- `frontend/src/features/permission-management/pages/PermissionManagementPage.vue`
- `frontend/src/features/permission-management/api.ts`
- `frontend/src/shared/auth/permissions.ts`
- `frontend/src/shared/components/auth/AuthButton.vue`

라우터의 `meta.auth`가 메뉴/화면 노출 기준이다. 버튼은 `AuthButton`의 `auth` prop으로 노출을 제어한다.

프론트 권한 제어는 UX 목적의 숨김 처리이고, 실제 차단은 백엔드의 `@PreAuthorize`와 `SecurityConfig`가 담당한다.

## 5. 서버단 권한 체크 적용 위치

예시:

- `SampleController`: `SAMPLE_READ`, `SAMPLE_CREATE`, `SAMPLE_UPDATE`, `SAMPLE_DELETE`
- `SampleJpaController`: `SAMPLE_JPA_READ`, `SAMPLE_JPA_CREATE`, `SAMPLE_JPA_UPDATE`, `SAMPLE_JPA_DELETE`, `SAMPLE_JPA_EXPORT`, `SAMPLE_JPA_IMPORT`
- `IntegratedMetaController`: `META_VIEW`
- 개발참고 API: `REF_VIEW`
- `PortalPermissionManagementController`: `PERMISSION_MANAGE`
- `BatchExecutionController`: `BATCH_ADMIN`

새 API를 추가할 때는 화면 버튼만 숨기지 말고 컨트롤러 또는 서비스 진입점에 서버단 권한 검사를 함께 추가한다.

## 6. 로컬 테스트 방법

기본 로컬 계정:

| 계정 | 비밀번호 | 용도 |
|---|---|---|
| `local-dev` | `local1234!` | 로컬 개발용 전체 권한 |
| `local-admin` | `local1234!` | 로컬 관리자 테스트 |

권한 제한 테스트가 필요하면 `application-local.yml`의 `app.auth.local-dev-user.users`에 제한 계정을 추가한다.

예:

```yaml
- user-id: local-viewer
  display-name: Local Viewer
  password: local1234!
  permissions:
    - DASHBOARD_READ
```

재시작 후 `local-viewer`로 로그인하면 대시보드 외 메뉴/버튼/API 제한을 확인할 수 있다.

## 7. 운영 반영 주의사항

- `dev`/`prod` 프로파일은 `schema.sql`/`data.sql`을 실행하지 않는다.
- 실제 개발/운영 DB에는 `portal_permission`, `portal_iam_role`, `portal_permission_assignment`를 마이그레이션 절차로 생성해야 한다.
- IAM/AD에서 내려오는 실제 그룹명은 `portal_iam_role.external_group_name`에 맞춘다.
- 운영에서 권한관리 화면을 사용할 계정 또는 IAM 역할에는 `PERMISSION_MANAGE`를 부여해야 한다.
- 권한 변경 이력 감사가 필요하면 `portal_permission_assignment` 변경 이력 테이블을 추가한다.

## 8. 검증

```powershell
cd C:\workspace\governance-portal\backend
.\gradlew.bat test

cd C:\workspace\governance-portal\frontend
npm.cmd run lint
npm.cmd run build
```

관련 테스트:

- `GovernancePortalApplicationTests.permissionManagementDataCanBeViewedAndSaved`
- `GovernancePortalApplicationTests.permissionManagementRequiresManagePermission`
- `GovernancePortalApplicationTests.sampleJpaWriteAndDownloadRequireDedicatedPermissions`
