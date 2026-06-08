# Gold 메타데이터·리니지 정의

작성일: 2026-06-08

이 문서는 거버넌스 포털이 관리할 메타데이터와 데이터 리니지의 기술적 정의다.
배경과 비유 중심의 설명은 `ai-data-governance-overview.md`를 참고한다.

## 1. 관리 범위 결정

| 단계 | 포털 취급 |
|---|---|
| Bronze | 포털 카탈로그에 등재하지 않음. Gold의 출처 메타로만 ID/파일명 보관 |
| Silver | 포털 카탈로그에 등재하지 않음. Gold의 출처 메타로만 ID/.md 링크 보관 |
| **Gold** | **1급 자산. 카탈로그/검색/등급/소유자/리니지의 주체** |
| Chunk | Gold의 자식. 출처 추적 시점에만 노출 |
| Vector | Gold의 속성. 별도 카탈로그 아님 |

Bronze/Silver의 처리·관리는 100% Dataiku 영역이다. 포털은 "Gold가 어떤 Silver/Bronze에서 왔는지"만 안다.

## 2. 패러다임 — 일반 DB 거버넌스와의 차이

| 구분 | 일반 DB 거버넌스 | AI 데이터 거버넌스 (본 프로젝트) |
|---|---|---|
| 데이터 단위 | 테이블 / 컬럼 / 로우 | 파일 / 페이지 / 청크 / 임베딩 |
| 변환 본질 | SQL 조인/집계 | 파싱 / 청킹 / 임베딩 |
| 핵심 메타 | 스키마/타입/PK | 모델/청크전략/토큰수/품질 |
| 리니지 목적 | 영향도/장애 추적 | AI 답변 근거(출처) 추적 |
| 거버넌스 질문 | "이 컬럼은 신뢰할 수 있나" | "이 AI 답변은 어느 문서에서 나왔나" |

본 프로젝트는 비즈니스 용어/소유자 같은 도메인 메타가 부차적이고, AI 파이프라인 메타가 핵심이다.

## 3. 메타데이터 4계층

### A. 기술 메타데이터 (Technical)

- 파일/스키마/타입/위치/크기/통계
- 자동 수집: OCI Data Catalog Harvester가 5개 ADB에서 긁어옴
- 100% 자동, 사람 입력 불필요

### B. 비즈니스 메타데이터 (Business)

- 비즈니스 용어, 도메인, 소유자
- 본 프로젝트에서는 최소화. 일반 데이터 거버넌스의 핵심이지만 본 프로젝트 우선순위는 낮다.

### C. 운영 메타데이터 (Operational) — 본 프로젝트의 핵심

- Dataiku Recipe ID/버전/실행 ID
- 파서 종류, OCR 여부, 표 추출 옵션
- 청킹 전략, 청크 크기, 오버랩
- 임베딩 모델, 차원, 비용
- 처리 일시, 소요 시간, 품질 점수
- 자동 수집: Dataiku Public API

### D. 거버넌스 메타데이터 (Governance)

- 데이터 등급 (공개/내부/대외비/극비/MNPI)
- 접근 권한, 마스킹 룰
- 보존 기간, 감사 정책
- 자동 분류 + 사람 검토 (PII/MNPI 검출 후 승인)

본 프로젝트는 C(운영)와 D(거버넌스)가 중심이고 B(비즈니스)는 보조다.

## 4. Gold 자산 메타 (포털이 관리하는 전부)

```yaml
# 식별
asset_id: GLD-2026-000847
name: "2026 Q3 경영보고서 RAG 자산"
domain: 재무
fiscal_period: 2026Q3
doc_type: 분기보고서

# Gold 자체 처리 메타 (Dataiku에서 자동 수집)
chunking:
  strategy: "semantic + recursive"
  chunk_size_tokens: 512
  chunk_overlap: 50
  total_chunks: 87
embedding:
  model: cohere.embed-multilingual-v3.0
  dimensions: 1024
  embedded_at: 2026-06-01 03:25:11
vector_index:
  db: Oracle 23ai AI Vector Search
  collection: FIN_QUARTERLY

# 출처 요약 (Bronze/Silver는 ID와 파일명만)
source:
  bronze_id: BRZ-2026-000847
  bronze_filename: "2026_Q3_경영보고서.pdf"
  bronze_page_count: 124
  silver_id: SLV-2026-000847
  silver_md_link: oci://bucket-silver/.../*.md
  dataiku_project: "AI-DATA-PIPELINE"
  dataiku_flow_url: https://dataiku.../flow/...   # Deep Link

# 거버넌스 (사람 입력 또는 자동 분류)
classification: 대외비
mnpi: true
pii: false
owner: 재무팀 (홍길동 부장)
steward: 데이터팀 (김영희 차장)
retention_until: 2036-06-01
access_roles: [FINANCE, EXECUTIVE]

# 품질 (Dataiku에서 자동)
quality_score: 0.94
last_quality_check: 2026-06-01 03:25:30

# AI 사용 통계 (RAG 앱 로그에서)
rag_usage:
  used_by_apps: ["재무챗봇", "임원리포트봇"]
  citations_30d: 421
  avg_score_30d: 0.87
```

## 5. Chunk 단위 메타 (Gold의 최소 단위)

```yaml
chunk_id: GLD-2026-000847::CHK-0042
parent_gold: GLD-2026-000847
sequence: 42
content_preview: "...3분기 영업이익은 1,820억원으로 전년 동기 대비..."
token_count: 487
char_count: 1843
source_page: 87
source_section: "3. 부문별 실적 > 3.2 IB부문"
embedding_vector_id: VEC_881003
neighbor_chunks: [CHK-0041, CHK-0043]
quality_signals:
  has_numeric_data: true
  has_jargon: true
```

## 6. 데이터 리니지 정의

리니지 단위는 테이블/컬럼이 아니라 자산(파일/청크/벡터)이다.

### End-to-End 흐름

```
원본 시스템
   │ (Dataiku Ingest Recipe)
   ▼
Bronze (원본 그대로)
   │ (Dataiku Parse Recipe)
   ▼
Silver (.md 텍스트)
   │ (Dataiku Chunk Recipe)
   ▼
Gold (청크)
   │ (Embedding Recipe)
   ▼
Vector Index
   │
   ▼
RAG Application → 응답
```

### 카디널리티 — 일반 DB 리니지와의 차이

```
Bronze 1 → Silver 1 → Gold 1 → Chunks N → Vectors N
   (1:1)     (1:1)     (1:N)     (1:1)
```

일반 DB 리니지의 N:M이 아니라 fan-out(1:N) 구조가 핵심이다.

### Gold 중심 그래프 (포털 시각화)

```
원본 PDF ─→ Bronze ─→ Silver ─→ ★Gold★ ─→ Chunks(N) ─→ Vector Index ─→ RAG 앱
 (회색)    (회색)    (회색)    (포털)     (포털)        (포털)        (외부)
   │                                                          
   │ Dataiku 영역 (포털은 ID·파일명만 표시)                       
```

- Gold/Chunk/Vector: 정상 풀 노드 (포털 상세 화면 연결)
- Bronze/Silver: 회색 축약 노드 (Dataiku Deep Link만)
- RAG 앱: 외부 노드 (응답 추적 가능)

### 엣지(변환) 메타

```yaml
edge: Silver → Gold
recipe: DATAIKU_RECIPE_CHUNK_FIN_V2 + EMBED_V1
recipe_version: "2.4.1"
dataiku_run_id: "20260601-032511-job1235"
executed_at: 2026-06-01 03:25:11
duration_sec: 142
parameters:
  chunk_size: 512
  overlap: 50
  embedding_model: cohere.embed-multilingual-v3.0
quality_score: 0.94
status: SUCCESS
deep_link: https://dataiku.../flow/recipes/CHUNK_FIN_V2
```

### 리니지의 최종 목적 — AI 답변 출처 추적

```
RAG 응답 ID → 인용된 청크 N개 → Gold → Silver → Bronze → 원본 파일/페이지
```

이게 본 프로젝트의 가장 중요한 가치다.

## 7. 메타 출처 매트릭스

각 메타가 어디서 오는지 명확해야 운영이 가능하다.

| 메타 | 출처 | 입력 방식 |
|---|---|---|
| Gold 식별/청킹/임베딩 | Dataiku API | 자동 |
| Bronze/Silver ID·파일명 | Dataiku API | 자동 |
| 품질 점수 | Dataiku Recipe 출력 | 자동 |
| 청크 본문/위치 | Dataiku Recipe 출력 | 자동 |
| RAG 인용 통계 | RAG 앱 로그 | 자동 |
| 벡터 인덱스 정보 | Vector DB API | 자동 |
| 등급/MNPI/PII | 자동 검출 + 사람 승인 | 반자동 |
| 소유자/스튜어드/도메인 | 사람 (벌크 업로드) | 수동 |
| 보존/접근 정책 | 등급 기반 자동 + 예외 입력 | 반자동 |
| 접근 이력 | 포털 자체 감사 로그 | 자동 |
| 비용 메트릭 | OCI Billing API + 임베딩 호출 로그 | 자동 |

자동 수집 비율이 압도적으로 높다. 사람 입력은 등급/소유자 정도다.

## 8. Dataiku와 포털의 SoT 분담

두 시스템은 공존하며 서로 보완한다. 같은 정보를 중복하지 않고 역할을 분담한다.

### Dataiku가 Source of Truth인 영역

- Recipe 코드/파라미터
- Dataset 스키마
- 실행 로그/실패 원인
- 컴퓨트 메트릭
- Recipe 개발/디버깅 UI

### 거버넌스 포털이 Source of Truth인 영역

- 데이터 등급 (대외비/MNPI/PII)
- 소유자/스튜어드
- 비즈니스 묶음 (도메인/업무)
- 5개 솔루션 ADB 메타 통합
- 벡터 DB 인덱스 메타
- RAG 응답 ↔ 청크 매핑
- 사용자 접근 이력 (감사)
- End-to-End 리니지 시각화

### 동기화

- 방향: Dataiku → 포털 단방향
- 방식: Dataiku Public API + 웹훅
- 시점: Recipe 실행 이벤트 + 일배치 보정

### 사용자별 진입점

| 사용자 | 도구 |
|---|---|
| 데이터 엔지니어 | Dataiku 직접 사용 + 포털에서 Deep Link로 점프 |
| ML 엔지니어 | Dataiku 우선, 포털에서 모델 영향도 분석 |
| 현업/임원 | 포털만 사용 (Dataiku 라이선스 없음) |
| 감사/컴플라이언스 | 포털만 사용 (리포트 export) |
| 보안 | 포털(등급/이력) + Dataiku 백오피스(권한 설정) |

### 안티패턴 (피해야 할 설계)

- 포털이 Dataiku를 100% 복제 (동기화 코스트 폭증)
- 포털에서 Recipe 편집 시도 (Dataiku 영역)
- Dataiku만 운영 (현업/감사 접근 불가, 5개 솔루션 통합 안 됨)
- 두 시스템 양방향 쓰기 (충돌 발생)

## 9. 인프라 구조

권장 Compartment 구조:

```
OCI Tenancy
├ compartment: governance-portal
│   ├ ADB: governance-meta-db        (포털 메타 저장)
│   ├ Compute VM                     (포털 실행)
│   └ OCI Vault                      (5개 ADB Wallet/패스워드)
├ compartment: solution-a~e
│   └ ADB × 5                        (솔루션별 원천)
├ Object Storage: bronze/silver/gold (자산 저장)
├ Oracle 23ai                        (Vector Search)
├ OCI Data Catalog                   (메타 수집, 테넌시당 1개)
└ Dataiku                            (데이터 가공)
```

5개 ADB 접근은 SELECT_CATALOG_ROLE 또는 read-only 계정으로 한정한다. ADMIN 사용 금지.
Wallet은 OCI Vault에 저장하고 인스턴스 프린서펄로 fetch한다.

## 10. 운영 결정 사항

| 결정 | 값 |
|---|---|
| 카탈로그 단위 | Gold |
| Bronze/Silver | 출처 메타로만 표시. 클릭하면 Dataiku로 |
| 사람 입력 메타 | Gold 단위 등급/소유자/도메인 |
| 자동 수집 | Dataiku API + Vector DB API + RAG 로그 |
| 리니지 책임 영역 | Gold ↔ Chunks ↔ Vector ↔ RAG |
| 동기화 방향 | Dataiku → 포털 단방향 |
| 최종 가치 | AI 응답 → 청크 → Gold → 출처 PDF 페이지 한 줄 추적 |

## 관련 문서

- `ai-data-governance-overview.md`: 본 프로젝트의 배경과 비유 중심 설명
- `portal-screens.md`: 5개 화면 상세 설계
- `menu-data-definition.md`: 현재 메뉴별 데이터 정의 (증권사 예시)
- `../../개선사항/프로젝트-컨텍스트-시스템프롬프트.md`: 프로젝트 전체 컨텍스트
