# 외부 REST API 연계 전략

## 1. 현재 확정 기준

이 프로젝트의 데이터는 OCI와 외부 모듈에서 REST API로 받아올 가능성이 높다. 따라서 프론트엔드는 외부 시스템을 직접 호출하지 않고, 백엔드가 BFF 역할로 외부 시스템과 통신한다.

```text
Frontend
-> Governance Portal Backend
-> OCI / 외부 모듈 REST API
```

프론트에서 외부 API를 직접 호출하지 않는 이유:

- SSO 세션/권한 판단을 백엔드에서 통제해야 한다.
- 외부 API 인증키, 토큰, endpoint를 브라우저에 노출하면 안 된다.
- requestId, 장애 로그, retry, timeout, masking을 서버에서 통일해야 한다.
- 내부망/운영망에서는 외부 시스템 접근 경로가 서버 기준으로 열리는 경우가 많다.

## 2. 백엔드 호출 방식

기본 REST 호출은 Spring `RestClient + HTTP Interface`를 우선 사용한다. 복잡한 URL 조립을 업무 서비스에 두지 않고, 외부 시스템별 인터페이스를 선언한 뒤 `ExternalApiClientFactory`로 구현체를 생성한다.

```text
ApiClientConfig
-> RestClient
-> ExternalApiLoggingInterceptor
-> ExternalApiClientFactory
-> @HttpExchange interface
```

공통 `RestClient`에 적용된 항목:

- connect timeout
- read timeout
- `X-Request-Id` 외부 호출 전파
- 외부 API 요약 로그
- 외부 API 4xx/5xx를 `ExternalApiException`으로 변환
- request/response body 미로깅
- 민감정보 키워드 마스킹

공통 인프라:

- `common.external.ExternalApiClientFactory`
- `common.external.ExternalApiLoggingInterceptor`
- `common.external.ExternalApiException`
- `common.external.ExternalApiHeaders`
- `common.external.ExternalApiProperties`
- `common.external.ExternalApiLogSanitizer`

환경 변수:

```bash
GOVERNANCE_EXTERNAL_API_CONNECT_TIMEOUT=3s
GOVERNANCE_EXTERNAL_API_READ_TIMEOUT=10s
GOVERNANCE_EXTERNAL_API_LOGGING_ENABLED=true
```

## 3. 외부 API 로그 기준

외부 API 호출 로그는 아래 정보만 남긴다.

| 항목 | 설명 |
|---|---|
| `externalSystem` | 외부 시스템 식별자 |
| `api` | HTTP method + scheme/host/path |
| `status` | HTTP status |
| `elapsedMs` | 응답 시간 |
| `reason` | 실패 사유 요약 |
| `requestId` | 내부 요청 추적 ID |

로그 예:

```text
External API handled. externalSystem=DATA_CATALOG, api=GET https://catalog.example.com/api/datasets, status=200, elapsedMs=132, requestId=...
External API failed. externalSystem=OCI_OBJECT_STORAGE, api=GET https://objectstorage.../n/namespace/b/bucket, elapsedMs=3000, requestId=..., reason=Read timed out
```

금지:

- request body 전체 로그
- response body 전체 로그
- Authorization/Cookie/token/password/SAMLResponse 로그
- 개인정보/계좌/고객식별자 원문 로그

## 4. 선언형 HTTP Interface 사용 방식

외부 시스템별로 인터페이스를 만든다.

```java
@HttpExchange
public interface CatalogClient {

    @GetExchange("/api/v1/datasets/{id}")
    DatasetResponse getDataset(@PathVariable String id);

    @PostExchange("/api/v1/datasets/search")
    DatasetSearchResponse search(@RequestBody DatasetSearchRequest request);
}
```

외부 시스템별 설정 클래스에서 client bean을 만든다.

```java
@Configuration
class CatalogClientConfig {

    @Bean
    CatalogClient catalogClient(ExternalApiClientFactory factory, CatalogProperties properties) {
        return factory.createClient(CatalogClient.class, properties.getBaseUrl());
    }
}
```

업무 서비스는 `CatalogClient`만 주입받고 `RestClient`나 endpoint를 직접 알지 않는다.

```text
Business Service
-> CatalogClient interface
-> ExternalApiClientFactory proxy
-> RestClient
-> External API
```

## 5. 외부 시스템 식별 방식

외부 API 호출 시 내부 식별용 헤더 `X-External-System`을 붙인다.

```java
restClient.get()
    .uri("https://catalog.example.com/api/datasets")
    .header(ExternalApiHeaders.EXTERNAL_SYSTEM, "DATA_CATALOG")
    .retrieve()
    .body(DatasetResponse.class);
```

`X-External-System`은 로그 식별용 내부 마커이며, 실제 외부 시스템으로 요청을 보내기 전에 인터셉터에서 제거한다.

마커가 없으면 host를 externalSystem 값으로 사용한다.

HTTP Interface에서는 공통 헤더를 메서드 인자로 받을 수 있다.

```java
@GetExchange("/api/v1/datasets/{id}")
DatasetResponse getDataset(
    @RequestHeader(ExternalApiHeaders.EXTERNAL_SYSTEM) String externalSystem,
    @PathVariable String id
);
```

또는 시스템별 client wrapper에서 한 번 감싸 외부 시스템명을 고정한다.

## 6. 패키지 구조 권장안

외부 연계 코드는 업무 서비스에 직접 넣지 않고 adapter/client 계층으로 분리한다.

```text
com.example.governanceportal.integration
  ├─ catalog
  │   ├─ CatalogClient
  │   ├─ CatalogProperties
  │   └─ dto
  ├─ oci
  │   ├─ OciObjectStorageClient
  │   ├─ OciProperties
  │   └─ dto
  └─ approval
      ├─ ApprovalClient
      ├─ ApprovalProperties
      └─ dto
```

업무 서비스는 외부 endpoint나 인증 방식을 알지 않고, client 인터페이스만 사용한다.

```text
Business Service
-> CatalogClient
-> RestClient
-> External API
```

## 7. 성공/실패 판정 기준

공통 기준:

- HTTP 2xx: 기술적 성공
- HTTP 4xx: 요청/권한/데이터 오류
- HTTP 5xx: 외부 시스템 장애
- timeout/connect 실패: 외부 통신 장애

단, 실제 업무 성공 여부는 외부 시스템별 응답 body의 result code를 확인해야 할 수 있다.

예:

```json
{
  "resultCode": "E1001",
  "message": "권한 없음"
}
```

이 경우 HTTP 200이어도 업무 실패일 수 있으므로, 시스템별 client에서 `BusinessException` 또는 별도 외부 API 예외로 변환한다.

현재 공통 인프라는 HTTP 4xx/5xx를 `ExternalApiException`으로 변환한다.

```text
ExternalApiException
-> HTTP 502 BAD_GATEWAY
-> code=EXTERNAL_API_ERROR
```

시스템별로 404를 정상 처리해야 하거나, HTTP 200 내부 resultCode를 업무 실패로 봐야 하는 경우에는 시스템별 client wrapper에서 변환 규칙을 추가한다.

## 8. timeout/retry 원칙

초기 기본값:

- connect timeout: 3초
- read timeout: 10초
- retry: 기본 비활성

retry는 요건 확정 전에는 자동 적용하지 않는다.

자동 retry를 신중히 봐야 하는 API:

- 등록
- 수정
- 삭제
- 승인
- 배치 실행
- 결제/거래성 API

조회성 API만 idempotent가 확인되면 retry 후보로 둔다.

## 9. OCI 연계 기준

OCI는 단순 REST API와 OCI SDK가 섞일 수 있다.

- Object Storage, IAM, Vault 등 OCI 전용 기능은 OCI SDK wrapper 우선 검토
- 단순 REST endpoint는 공통 `RestClient` 사용
- OCI SDK를 쓰더라도 업무 서비스에서 SDK를 직접 호출하지 않고 `integration.oci` wrapper를 둔다.

OCI SDK wrapper도 같은 로그 원칙을 따른다.

```text
externalSystem=OCI_OBJECT_STORAGE
api=GET object-storage/object
elapsedMs=...
requestId=...
```

## 10. 요건 확정 후 결정할 항목

- 외부 시스템 목록과 시스템 코드
- base URL과 네트워크 접근 경로
- 인증 방식: API key, mTLS, OAuth2 client credentials, 사내 토큰, OCI signer
- endpoint별 timeout
- endpoint별 retry 가능 여부
- 200 응답 안의 업무 실패 판정 기준
- 장애 알림 기준
- 외부 API 로그 보관 위치와 기간

## 11. 현재 구현된 공통 인프라

- `config.ApiClientConfig`
- `common.external.ExternalApiClientFactory`
- `common.external.ExternalApiException`
- `common.external.ExternalApiLoggingInterceptor`
- `common.external.ExternalApiHeaders`
- `common.external.ExternalApiProperties`
- `common.external.ExternalApiLogSanitizer`

향후 외부 연계 구현자는 반드시 공통 `RestClient`를 주입받아 사용한다.
