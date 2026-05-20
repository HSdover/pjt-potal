# SAML SSO 전환 시 조치사항

## 목적

현재 프로젝트는 프론트가 `/api/me`를 호출해서 백엔드가 검증한 사용자와 권한을 받는 구조로 정리되어 있다.
SSO 방식이 OIDC가 아니라 SAML로 확정되더라도 프론트 권한 처리 방식은 유지하고, 백엔드 로그인 검증 방식만 SAML2 Login으로 전환한다.

## 현재 유지할 구조

```text
사용자 접속
-> 백엔드 SAML2 Login
-> IAM/KNOX IdP 인증
-> 백엔드 세션 생성
-> 프론트 GET /api/me 호출
-> 백엔드가 사용자 정보와 permissions 반환
-> 프론트는 permissions로 메뉴/라우터/버튼 표시
```

프론트는 권한을 직접 만들지 않는다. 권한은 항상 백엔드 `/api/me` 응답 기준으로 처리한다.

## 내가 먼저 확인해야 할 것

IAM/KNOX 담당자에게 아래 항목을 요청해야 한다.

| 구분 | 확인/요청 항목 | 비고 |
| --- | --- | --- |
| IdP Metadata | metadata URL 또는 metadata XML 파일 | 가능하면 URL 방식이 운영 관리에 유리 |
| IdP Entity ID | SAML IdP 식별자 | metadata에 포함되는 경우가 많음 |
| SSO URL | 로그인 요청을 보낼 URL | metadata에 포함되는 경우가 많음 |
| IdP 인증서 | SAML Response 서명 검증용 인증서 | 만료일 관리 필요 |
| NameID Format | 사용자 식별 방식 | email, persistent, unspecified 등 |
| 사용자 식별 attribute | 사번, 이메일, AD 계정, Knox ID 등 | 포털 userId 기준 결정 필요 |
| 표시명 attribute | 이름/부서 표시용 | `/api/me.displayName`에 사용 |
| 그룹/역할 attribute | 권한 매핑용 groups/roles | 권한 정책 확정 전에는 샘플 값만 확보 |
| SP Entity ID | 우리 포털 서비스 식별자 | 예: `governance-portal` 또는 운영 URL |
| ACS URL 등록 | SAML 응답 수신 URL | `https://도메인/login/saml2/sso/knox` |
| Logout 지원 여부 | SLO 지원 여부 | 미지원이면 포털 세션 로그아웃만 처리 |

## IAM/KNOX에 등록할 값

운영 도메인이 확정되면 아래 값을 IdP에 등록한다.

```text
SP Entity ID:
  https://{portal-domain}/saml2/service-provider-metadata/knox
  또는 IAM 정책에 맞춘 고정 ID

ACS URL:
  https://{portal-domain}/login/saml2/sso/knox

Login 시작 URL:
  https://{portal-domain}/saml2/authenticate/knox
```

로컬 테스트가 허용되는 경우:

```text
ACS URL:
  http://localhost:18080/login/saml2/sso/knox

Login 시작 URL:
  http://localhost:18080/saml2/authenticate/knox
```

## 프로젝트에 적용할 설정

SAML 전용 프로파일을 추가하는 방향이 좋다.

예상 파일:

```text
backend/src/main/resources/application-saml.yml
```

metadata URL을 받을 수 있으면 가장 단순한 설정은 아래 형태다.

```yaml
spring:
  security:
    saml2:
      relyingparty:
        registration:
          knox:
            assertingparty:
              metadata-uri: ${SAML_IDP_METADATA_URI}
```

metadata URL을 받을 수 없으면 IdP Entity ID, SSO URL, 인증서를 개별 설정해야 한다. 이 경우 인증서 파일 위치와 갱신 절차를 별도로 정해야 한다.

## 백엔드 구현 작업

1. `spring-security-saml2-service-provider` 의존성 추가
   - 현재 `backend/build.gradle`에 추가 완료
   - OpenSAML 전이 의존성 해석을 위해 Shibboleth 릴리즈 저장소도 추가 완료

2. `SecurityConfig`에 `saml` 프로파일 체인 추가

```java
@Bean
@Profile("saml")
SecurityFilterChain samlSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .requestMatchers("/saml2/**", "/login/saml2/**").permitAll()
            .requestMatchers("/api/admin/**").hasAuthority("BATCH_ADMIN")
            .anyRequest().authenticated())
        .exceptionHandling(exception -> exception
            .defaultAuthenticationEntryPointFor(
                (request, response, authException) -> response.sendError(401),
                new AntPathRequestMatcher("/api/**")))
        .saml2Login(Customizer.withDefaults())
        .logout(logout -> logout.logoutSuccessUrl("/"))
        .build();
}
```

3. `/api/me`에서 SAML 사용자 attribute 확인
   - `userId`: 사번, 이메일, Knox ID 중 확정된 값
   - `displayName`: 사용자 이름 attribute
   - `permissions`: 권한 정책 확정 전에는 비워두거나 임시 최소 권한만 반환

4. 권한 정책 확정 후 SAML attribute를 포털 권한으로 매핑

```text
SAML groups/roles
-> 포털 permissions
-> DASHBOARD_READ, BATCH_ADMIN 등
```

## 프론트 구현 작업

프론트 권한 구조는 유지한다. 바뀌는 것은 로그인 진입 경로다.

현재 OIDC 기준:

```text
/oauth2/authorization/knox
```

SAML 기준:

```text
/saml2/authenticate/knox
```

권장 작업:

```ts
const LOGIN_ENTRY_PATH = import.meta.env.VITE_LOGIN_ENTRY_PATH ?? "/oauth2/authorization/knox";
```

SAML 환경변수:

```env
VITE_LOGIN_ENTRY_PATH=/saml2/authenticate/knox
```

## 로컬 개발 전략

로컬은 실제 SAML IdP 없이도 개발 가능해야 한다.

현재 구조에서는 `local` 프로파일에서 백엔드가 임시 사용자와 임시 권한을 `/api/me`로 내려준다.

```text
SPRING_PROFILES_ACTIVE=local
```

이 방식은 계속 유지한다.

SAML 통합 테스트가 필요한 경우에만 별도로 아래 프로파일을 사용한다.

```text
SPRING_PROFILES_ACTIVE=saml
```

## 테스트 체크리스트

SAML 연동 시 아래를 순서대로 확인한다.

- [ ] `/saml2/authenticate/knox` 접근 시 IdP 로그인 화면으로 이동
- [ ] 로그인 성공 후 `/login/saml2/sso/knox`로 POST 응답 수신
- [ ] 로그인 후 포털 메인 화면으로 복귀
- [ ] `/api/me`가 200을 반환
- [ ] `/api/me.userId`가 실제 사용자 식별값으로 내려옴
- [ ] `/api/me.displayName`이 표시 가능한 이름으로 내려옴
- [ ] `/api/me.permissions`가 백엔드 기준으로 내려옴
- [ ] 미로그인 상태에서 `/api/**`는 401
- [ ] 미로그인 상태에서 화면 접근은 SAML 로그인으로 진입
- [ ] POST/PUT/DELETE 요청에 CSRF 헤더가 포함됨
- [ ] `/api/admin/**`는 `BATCH_ADMIN` 없으면 차단

## 주의사항

- SAML 인증서는 만료일 관리가 필요하다.
- IdP metadata URL을 쓰더라도 네트워크 접근, 방화벽, 인증서 신뢰 체인을 확인해야 한다.
- 운영에서는 `http://localhost` ACS URL을 등록하지 않는다.
- 프론트에 권한을 하드코딩하지 않는다.
- 권한 정책이 확정되기 전에는 전체 권한을 임시로 주지 말고 최소 권한으로 검증한다.
- SAML은 OIDC와 달리 JWT access token을 API 호출에 들고 다니는 구조가 아니다. 이 프로젝트에서는 백엔드 세션 기반 BFF 구조로 처리한다.
