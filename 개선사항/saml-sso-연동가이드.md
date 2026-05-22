# SAML SSO 연동 가이드

## 현재 방향

이 프로젝트의 SSO는 OIDC/OAuth2 Client 방식이 아니라 SAML2 Service Provider 방식으로 맞춘다.

프론트엔드는 직접 SAML 응답을 처리하지 않는다. 사용자가 미인증 상태에서 API를 호출하면 백엔드가 401을 반환하고, 프론트 공통 API 클라이언트가 `/saml2/authenticate/knox`로 이동시킨다. 이후 IAM/KNOX IdP 인증을 거쳐 백엔드의 `/login/saml2/sso/knox`가 SAMLResponse를 수신하고 세션을 만든다.

## 프로젝트 반영 내용

| 구분 | 반영 내용 |
|---|---|
| 백엔드 의존성 | `spring-security-saml2-service-provider` 유지 |
| OIDC 의존성 | `spring-boot-starter-oauth2-client`, `spring-boot-starter-oauth2-resource-server` 제거 |
| SAML 프로파일 | `backend/src/main/resources/application-saml.yml` 추가 |
| 보안 체인 | `saml` 프로파일에서 `saml2Login()` 사용 |
| CSRF 정책 | API 쓰기 요청은 CSRF 보호, SAML ACS `POST /login/saml2/sso/**`만 예외, SPA 헤더 토큰은 plain 토큰으로 검증 |
| 권한 매핑 | SAML 그룹/역할 attribute를 내부 권한 코드로 변환 |
| 로그아웃 | 현재는 로컬 세션 로그아웃만 적용. SAML SLO는 IAM/KNOX 정책 확인 후 적용 |
| 프론트 로그인 진입 | 기본값 `/saml2/authenticate/knox` |
| 로컬 개발 | `local` 프로파일의 임시 권한 사용자 유지 |

## 실행 프로파일

로컬 개발은 실제 SSO 없이 진행한다.

```bash
SPRING_PROFILES_ACTIVE=local
```

SAML 연동 검증 또는 운영 환경은 SAML 프로파일을 사용한다.

```bash
SPRING_PROFILES_ACTIVE=saml
SAML_IDP_METADATA_URI=https://idp.example.com/metadata
```

운영에서 추가로 필요한 값이 있으면 환경변수로 지정한다.

```bash
SAML_SP_ENTITY_ID=https://portal.example.com/saml2/service-provider-metadata/knox
SAML_SP_ACS_LOCATION=https://portal.example.com/login/saml2/sso/knox
```

프론트 로그인 진입 경로는 기본값을 그대로 쓰면 된다.

```bash
VITE_LOGIN_ENTRY_PATH=/saml2/authenticate/knox
```

## IAM/KNOX 담당자에게 요청할 값

| 항목 | 설명 |
|---|---|
| IdP Metadata URL | 백엔드 `SAML_IDP_METADATA_URI`에 사용 |
| IdP Entity ID | metadata에 포함되는 값인지 확인 |
| SSO URL | AuthnRequest를 받을 IdP URL |
| IdP 인증서 | SAMLResponse 서명 검증용 |
| NameID 형식 | 사번, 계정 ID, 이메일 중 어떤 값인지 확인 |
| 사용자 attribute | `uid`, `employeeNumber`, `displayName`, `roles`, `groups` 등 |
| 권한 attribute | 포털 권한으로 매핑할 그룹/역할 속성 |

## IAM/KNOX에 등록할 SP 값

운영 도메인 기준:

```text
SP Entity ID:
https://{portal-domain}/saml2/service-provider-metadata/knox

ACS URL:
https://{portal-domain}/login/saml2/sso/knox

Login Entry:
https://{portal-domain}/saml2/authenticate/knox
```

로컬 또는 개발 검증 기준:

```text
ACS URL:
http://localhost:18080/login/saml2/sso/knox

Login Entry:
http://localhost:18080/saml2/authenticate/knox
```

## 권한 처리 방향

현재 `/api/me`는 SAML 인증 후 아래 순서로 사용자 정보를 구성한다.

| 응답 필드 | 우선순위 |
|---|---|
| `userId` | `uid` -> `employeeNumber` -> `sAMAccountName` -> SAML NameID |
| `displayName` | `displayName` -> `cn` -> `name` -> `userId` |
| `permissions` | SAML 그룹/역할 매핑 결과. 필요 시 내부 권한 코드만 직접 권한 attribute로 허용 |

IAM/KNOX가 내려주는 그룹명은 포털 내부 권한명과 다를 수 있으므로 원문을 그대로 권한으로 쓰지 않는다. `roles`, `groups`, `memberOf` 값은 아래 설정을 통해 내부 권한 코드로 변환한다.

```yaml
app:
  auth:
    saml:
      # IdP가 DASHBOARD_READ 같은 내부 권한 코드를 직접 내려줄 때만 사용한다.
      direct-permission-attributes: []
      group-attributes:
        - roles
        - groups
        - memberOf
      group-permission-mappings:
        KNOX_DG_PORTAL_ADMIN:
          - DASHBOARD_READ
          - SAMPLE_READ
          - BATCH_ADMIN
        KNOX_DG_PORTAL_VIEWER:
          - DASHBOARD_READ
```

`memberOf`가 `CN=KNOX_DG_PORTAL_ADMIN,OU=Groups,DC=example,DC=com` 같은 DN 형식으로 내려오면 `CN` 값을 추출해서 매핑한다. 최종 운영 전 IAM/KNOX에서 실제 내려주는 그룹명과 attribute명을 확인한 뒤 이 매핑을 채워야 한다.

## 확인 순서

1. `SPRING_PROFILES_ACTIVE=saml`과 `SAML_IDP_METADATA_URI`를 설정하고 백엔드를 기동한다.
2. `/saml2/service-provider-metadata/knox`가 열리는지 확인한다.
3. 해당 metadata 또는 위 SP 값을 IAM/KNOX에 등록한다.
4. `/saml2/authenticate/knox` 접근 시 IdP 로그인 화면으로 이동하는지 확인한다.
5. 로그인 성공 후 `/login/saml2/sso/knox`로 SAMLResponse가 들어오는지 확인한다.
6. `/api/me` 응답에서 `authenticated=true`, 사용자 ID, attribute 기반 권한이 내려오는지 확인한다.
7. 프론트 화면에서 권한 기반 메뉴/버튼 노출이 맞는지 확인한다.

## 로그아웃 정책

현재 코드는 애플리케이션 로컬 세션 로그아웃만 수행한다.

```java
.logout(logout -> logout.logoutSuccessUrl("/"))
```

이 경우 포털 세션은 종료되지만 IAM/KNOX IdP 세션은 남아 있을 수 있다. 사용자가 다시 `/saml2/authenticate/knox`로 진입하면 IdP 세션 때문에 비밀번호 입력 없이 재로그인될 수 있다.

SAML SLO(Single Logout)는 IAM/KNOX가 지원하거나 보안 정책상 요구할 때만 적용한다. SLO를 켜려면 IdP metadata, SP metadata, logout endpoint, 서명/인증서 정책이 맞아야 하므로 코드만 먼저 켜지 않는다.

IAM/KNOX 담당자에게 아래 항목을 확인한다.

| 항목 | 확인 내용 |
|---|---|
| SLO 지원 여부 | IdP가 SAML Single Logout을 지원하는지 |
| SLO 필수 여부 | 업무/보안 정책상 포털 로그아웃 시 IdP 세션도 종료해야 하는지 |
| 바인딩 방식 | Redirect 바인딩인지 POST 바인딩인지 |
| Logout URL | IdP SingleLogoutService URL |
| 서명 요구 | LogoutRequest/LogoutResponse 서명이 필수인지 |
| 인증서 정책 | SP 서명 인증서 등록과 갱신 방식 |
| 운영 UX | 다른 사내 시스템 세션까지 같이 종료해도 되는지 |

SLO가 요구되면 보안 체인에 아래 설정을 추가한다.

```java
.saml2Logout(Customizer.withDefaults())
```

SLO를 켜는 경우 `/logout/saml2/slo/**` 엔드포인트, CSRF 예외 필요 여부, metadata 노출 값, IdP 로그아웃 응답 흐름을 별도 PoC로 검증한다.

## 주의사항

- 운영 환경에서는 반드시 `SPRING_PROFILES_ACTIVE=saml`을 포함해야 한다.
- `local` 프로파일은 개발 편의를 위한 임시 권한 방식이므로 운영에서 사용하면 안 된다.
- SAML은 브라우저 리다이렉트와 백엔드 세션을 기반으로 처리한다. 프론트가 JWT를 저장하거나 SAMLResponse를 직접 파싱하지 않는다.
- SAML ACS POST는 IdP가 자동 제출하는 폼이므로 CSRF 토큰을 포함할 수 없다. 대신 SAMLResponse 서명, issuer, audience, destination, 만료 시간 검증으로 보호한다.
- 프론트는 `XSRF-TOKEN` 쿠키의 plain 값을 `X-XSRF-TOKEN` 헤더로 보내므로, 백엔드는 SPA 전용 CSRF request handler로 헤더 토큰을 plain 값으로 검증한다.
- SLO를 적용하지 않으면 포털 로그아웃 후에도 IdP 세션이 유지될 수 있다. 운영 정책에 따라 허용 가능한지 확인해야 한다.
- IdP 인증서 만료일과 metadata 갱신 주기를 운영 절차에 포함해야 한다.
