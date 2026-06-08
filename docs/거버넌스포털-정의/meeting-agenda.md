# 거버넌스 포털 정의 회의

작성일: 2026-06-08
회의 목적: 화면 구성과 데이터 모델(테이블) 정의 합의

## 1. 회의 개요

### 1.1 목적

`거버넌스포털-정의` 폴더의 4개 문서를 기반으로 다음 2가지를 합의한다.

1. **화면 구성**: 어떤 메뉴와 화면을 가질 것인가
2. **데이터 모델**: 각 화면별로 어떤 테이블을 어떻게 정의할 것인가

### 1.2 사전 읽기 자료

회의 전 참석자가 반드시 읽어야 할 자료. 회의 시간을 토의에 쓰기 위함.

| 문서 | 목적 | 읽는 데 걸리는 시간 |
|---|---|---|
| `ai-data-governance-overview.md` | 프로젝트 본질 이해 | 15분 |
| `gold-metadata-lineage.md` | 데이터 모델 기술 정의 | 20분 |
| `portal-screens.md` | 화면 설계 (이전 기준) | 25분 |
| `menu-data-definition.md` | 현재 메뉴별 데이터 정의 | 30분 |

총 사전 학습 시간: 약 90분

### 1.3 참석자 (권장)

| 역할 | 책임 영역 |
|---|---|
| PM/기획자 | 메뉴/화면 우선순위, 비즈니스 요구사항 |
| 백엔드 리드 | 테이블/API 설계 의사결정 |
| 프론트 리드 | 화면 구현 가능성, UX |
| 데이터 엔지니어 (Dataiku 측) | 메타 동기화 방식, Recipe 메타 출력 가능 범위 |
| 거버넌스/보안 담당자 | MNPI/PII 분류, 감사 요건 |
| 인프라/DBA | ADB 구조, 벡터 DB 연동 |

### 1.4 회의 시간

| 구간 | 시간 | 내용 |
|---|---|---|
| 1 | 10분 | 사전 합의 사항 확인 |
| 2 | 50분 | **안건 1: 화면 구성** |
| 3 | 10분 | 휴식 |
| 4 | 60분 | **안건 2: 데이터 모델/테이블 정의** |
| 5 | 20분 | 의사결정 정리 + 액션 아이템 |
| **합계** | **150분 (2시간 30분)** | |

---

## 2. 사전 합의 사항 (회의 시작 전 재확인)

회의를 시작하기 전 다음 사항이 합의됐는지 빠르게 확인한다. 합의 안 됐다면 이 회의의 전제가 흔들리므로 먼저 다룬다.

| # | 합의 사항 | 출처 | 합의 |
|---|---|---|---|
| 1 | 본 프로젝트는 **AI/RAG 파이프라인 거버넌스**이지 일반 데이터 거버넌스가 아니다 | overview §1 | ☐ |
| 2 | 도메인/업무 정의(비즈니스 용어집)는 본 프로젝트 주업무가 아니다 | overview §9 | ☐ |
| 3 | 카탈로그의 1급 시민은 **Gold** 자산이다 (Bronze/Silver는 출처 메타로만) | lineage §1 | ☐ |
| 4 | Dataiku와 포털은 **공존**하며 단방향 동기화(Dataiku → 포털)한다 | lineage §8 | ☐ |
| 5 | 본 프로젝트의 최종 가치는 **"AI 답변의 출처 추적"** 이다 | overview §8 | ☐ |
| 6 | 메타데이터의 80%는 **자동 수집**, 20%만 사람 입력이다 | lineage §7 | ☐ |
| 7 | 현재 router에 정의된 8개 Lv1 메뉴 구조를 기본으로 유지한다 | 현 코드 | ☐ |

⚠ 7개 중 하나라도 ☐ 라면, 이 항목부터 먼저 토의한다. 합의 없이 화면/테이블 정의는 무의미하다.

---

## 3. 안건 1: 화면 구성 (50분)

### 3.1 현재 메뉴 구조 (router 기준)

| Lv1 | Lv2 | 상태 |
|---|---|---|
| 메타데이터 | 메타관리 | 구현됨 |
| 파이프라인관리 | 파이프라인목록 | placeholder |
| AI데이터관리 | AI에이전트 매핑 | placeholder |
| AI데이터관리 | 데이터 매쉬 | placeholder |
| 신청관리 | 파이프라인신청 목록 | placeholder |
| 신청관리 | AI에이전트 신청목록 | placeholder |
| DP뷰어 | 문서 파싱 목록 | placeholder |
| 대시보드 | 일현황 | 구현됨 |
| 대시보드 | 파이프라인 현황 | placeholder |
| 대시보드 | AI에이전트 현황 | placeholder |
| 시스템 관리 | 공지사항 | placeholder |
| 시스템 관리 | 권한관리 | 구현됨 |

### 3.2 추가/조정 검토 화면

#### A. AI 응답 출처 추적 (신규 추가 검토)

본 프로젝트의 최종 가치인 "AI 답변의 출처 추적"을 담당하는 화면이 현재 메뉴에 없다.

**배치 옵션**:

| 안 | 위치 | 장점 | 단점 |
|---|---|---|---|
| A | AI데이터관리 > AI 응답 추적 (신규) | 명시적 메뉴로 핵심 가치 가시화 | 메뉴 추가 |
| B | 대시보드 > AI에이전트 현황의 액션 | 메뉴 안 늘림 | 진입점 불명확 |
| C | AI에이전트 매핑의 [응답 추적] 버튼 | 매핑과 함께 자연스러움 | 별도 화면 X |

**📋 결정 필요**: 안 A/B/C 중 어떤 방식으로 할 것인가?

#### B. 데이터 매쉬의 의도 명확화

현재 router에 추가됐으나 screen-domain-template에 없다. 의도가 모호하다.

**의도 후보**:

| 안 | 화면 성격 | 데이터 |
|---|---|---|
| A | 도메인 토폴로지 맵 | 도메인별 Gold/Agent 분포 |
| B | 전체 리니지 시각화 | Bronze→Silver→Gold→Vector→RAG 그래프 |
| C | 도메인별 데이터셋 카탈로그 | 메타관리의 도메인별 뷰 |
| D | A+B 통합 (상단 토폴로지 + 하단 리니지) | 둘 다 |

**📋 결정 필요**: 데이터 매쉬는 무엇인가?
권장: **안 D** (토폴로지 + 리니지 시각화 통합)

#### C. 메타관리 상세 다이얼로그 탭 추가

현재: 기본정보 | 컬럼정보
권장: 기본정보 | 컬럼정보 | **리니지** | **사용현황**

**📋 결정 필요**: 리니지/사용현황 탭을 추가할 것인가?

### 3.3 화면별 1차 오픈 우선순위 안

| 우선 | 화면 | 근거 |
|---|---|---|
| **MVP 1차** | 메타관리 (구현 완료 보강) | 카탈로그 진입점 |
| MVP 1차 | 일현황 대시보드 (구현 완료 보강) | 운영 모니터링 |
| MVP 1차 | AI에이전트 매핑 | 거버넌스 핵심 |
| MVP 1차 | 파이프라인목록 | Dataiku 동기화 |
| **2차** | 신청관리 (파이프라인/에이전트) | 1차 마스터 확정 후 |
| 2차 | DP뷰어 (문서 파싱 목록) | 품질 검증 |
| 2차 | AI 응답 추적 | RAG 앱 로그 연동 필요 |
| **3차** | 데이터 매쉬 (리니지) | 마스터 안정화 후 |
| 3차 | 파이프라인 현황 / AI에이전트 현황 | 운영 메트릭 |
| 3차 | 공지사항 | 단순 게시판 |
| 운영 선행 | 권한관리 | IAM/AD 권한 매핑과 메뉴/API 접근 제어 |

**📋 결정 필요**: 1차 오픈 범위와 일정?

### 3.4 화면 구성 토의 항목

| # | 토의 항목 | 결정 결과 |
|---|---|---|
| 3-1 | "데이터 매쉬"의 의도를 무엇으로 확정할 것인가 | |
| 3-2 | AI 응답 출처 추적 화면을 어디에 둘 것인가 | |
| 3-3 | 메타관리 상세에 리니지/사용현황 탭을 추가할 것인가 | |
| 3-4 | 신청관리를 파이프라인/에이전트 2개로 유지할 것인가, 통합할 것인가 | |
| 3-5 | DP뷰어를 메타관리에 통합할 가능성은 없는가 | |
| 3-6 | 1차 MVP 오픈 범위와 일정 | |
| 3-7 | 메뉴명 최종 확정 (한글 표기 일관성) | |

---

## 4. 안건 2: 데이터 모델/테이블 정의 (60분)

### 4.1 마스터 엔티티 7개 (전체 그림)

```text
                          [도메인]
                             │
              ┌──────────────┼──────────────┐
              ▼              ▼              ▼
         [데이터셋]      [파이프라인]      [에이전트]
         (Gold/메타)    (Dataiku Flow)     (챗봇)
              │              │              │
              │              ▼              │
              │         [Dataiku Run]       │
              │         (실행이력)          │
              │                             │
              └──────────[매핑]─────────────┘
                            │
                            ▼
                       [응답 로그]
                       (인용 청크 추적)

  모든 변경 ↓
  [신청-승인 워크플로우]
```

### 4.2 핵심 테이블 안 (8개)

각 테이블의 핵심 컬럼 안. 모든 테이블에 공통 감사 컬럼(CREATED_BY/CREATED_AT/UPDATED_BY/UPDATED_AT)이 포함된다고 가정.

#### A. DATASET_META (데이터셋 메타)

가장 핵심. 메타관리 화면이 직접 사용한다.

| 컬럼 | 타입 | 키 | 설명 | 출처 |
|---|---|---|---|---|
| META_ID | VARCHAR2(20) | PK | 메타 식별자 | 시스템 |
| ASSET_TYPE | VARCHAR2(20) | | STRUCTURED/FILE/SEMI_STRUCTURED | 시스템 |
| META_NAME | VARCHAR2(200) | | 메타명 | 사람 |
| DOMAIN_CODE | VARCHAR2(20) | FK→DOMAIN | 도메인 코드 | 사람 |
| SOURCE_SYSTEM | VARCHAR2(100) | | 원천 시스템명 | 자동 |
| OWNER_DEPT | VARCHAR2(100) | | 담당부서 | 사람 |
| OWNER_USER_ID | VARCHAR2(50) | FK→USER | 소유자 | 사람 |
| STEWARD_USER_ID | VARCHAR2(50) | FK→USER | 스튜어드 | 사람 |
| SECURITY_LEVEL | VARCHAR2(20) | | 공개/내부/대외비/MNPI/극비 | 자동+승인 |
| HAS_PII | CHAR(1) | | Y/N | 자동 |
| HAS_MNPI | CHAR(1) | | Y/N | 자동+승인 |
| RETENTION_UNTIL | DATE | | 보존 만료일 | 자동(등급 기반) |
| BRONZE_ID | VARCHAR2(20) | | 원본 Bronze ID | Dataiku |
| BRONZE_FILENAME | VARCHAR2(500) | | 원본 파일명 | Dataiku |
| SILVER_ID | VARCHAR2(20) | | Silver ID | Dataiku |
| DATAIKU_PROJECT | VARCHAR2(100) | | Dataiku 프로젝트명 | Dataiku |
| DATAIKU_FLOW_URL | VARCHAR2(500) | | Deep Link | Dataiku |
| QUALITY_SCORE | NUMBER(3,2) | | 종합 품질 점수 | Dataiku |
| LAST_PROCESSED_AT | TIMESTAMP | | 최종 처리 일시 | Dataiku |

#### B. DATASET_CHUNK (Gold 자산의 청크)

반정형 자산에만 해당. SEMI_STRUCTURED 자산의 청크 정보.

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| CHUNK_ID | VARCHAR2(50) | PK | 청크 식별자 (예: GLD-...-CHK-0042) |
| META_ID | VARCHAR2(20) | FK | 부모 메타 |
| SEQUENCE | NUMBER | | 청크 순서 |
| CONTENT_PREVIEW | VARCHAR2(2000) | | 본문 미리보기 |
| TOKEN_COUNT | NUMBER | | 토큰 수 |
| SOURCE_PAGE | NUMBER | | 원본 PDF 페이지 |
| SOURCE_SECTION | VARCHAR2(500) | | 섹션 경로 |
| VECTOR_ID | VARCHAR2(50) | | 벡터 DB ID |
| EMBEDDING_MODEL | VARCHAR2(100) | | 임베딩 모델 |

#### C. AI_AGENT (AI 에이전트)

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| AGENT_ID | VARCHAR2(20) | PK | 에이전트 식별자 |
| AGENT_NAME | VARCHAR2(100) | | 에이전트명 |
| PURPOSE | VARCHAR2(500) | | 용도 |
| OWNER_DEPT | VARCHAR2(100) | | 소유부서 |
| OWNER_USER_ID | VARCHAR2(50) | FK→USER | 담당자 |
| LLM_MODEL | VARCHAR2(100) | | 연결 LLM |
| ENDPOINT_URL | VARCHAR2(500) | | 엔드포인트 |
| STATUS | VARCHAR2(20) | | 개발중/검토중/운영중/중단 |
| ALLOW_MNPI | CHAR(1) | | MNPI 접근 허용 |
| ALLOW_PII | CHAR(1) | | PII 접근 허용 |
| ACCESS_ROLES | VARCHAR2(500) | | 접근 가능 role (CSV) |

#### D. AGENT_DATASET_MAPPING (매핑)

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| MAPPING_ID | VARCHAR2(20) | PK | 매핑 ID |
| AGENT_ID | VARCHAR2(20) | FK | 에이전트 |
| META_ID | VARCHAR2(20) | FK | 데이터셋 |
| ACCESS_TYPE | VARCHAR2(20) | | 읽기/쓰기 |
| MAPPED_AT | TIMESTAMP | | 매핑 일시 |
| REQUESTED_BY | VARCHAR2(50) | | 신청자 |
| APPROVED_BY | VARCHAR2(50) | | 승인자 |
| REQUEST_ID | VARCHAR2(20) | FK→REQUEST | 연결 신청 |
| STATUS | VARCHAR2(20) | | 활성/만료/해제 |

#### E. PIPELINE (파이프라인)

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| PIPELINE_ID | VARCHAR2(20) | PK | 파이프라인 ID |
| PIPELINE_NAME | VARCHAR2(200) | | 명 |
| DOMAIN_CODE | VARCHAR2(20) | FK | 도메인 |
| PROCESS_TYPE | VARCHAR2(50) | | 정형→Gold / 비정형→Gold |
| DATAIKU_PROJECT | VARCHAR2(100) | | Dataiku 프로젝트 |
| DATAIKU_FLOW_URL | VARCHAR2(500) | | Deep Link |
| STATUS | VARCHAR2(20) | | 개발/검토/상용/폐기 |
| RECIPE_LIST | CLOB | | Recipe 구성 (JSON) |
| LAST_RUN_AT | TIMESTAMP | | 최근 실행 |
| LAST_RUN_STATUS | VARCHAR2(20) | | 최근 결과 |

#### F. PIPELINE_RUN (파이프라인 실행 이력)

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| RUN_ID | VARCHAR2(30) | PK | Dataiku Run ID |
| PIPELINE_ID | VARCHAR2(20) | FK | 파이프라인 |
| STARTED_AT | TIMESTAMP | | 시작 |
| ENDED_AT | TIMESTAMP | | 종료 |
| DURATION_SEC | NUMBER | | 소요 (초) |
| STATUS | VARCHAR2(20) | | SUCCESS/FAILED/RUNNING |
| INPUT_COUNT | NUMBER | | 입력 자산 수 |
| OUTPUT_COUNT | NUMBER | | 출력 자산 수 |
| QUALITY_SCORE | NUMBER(3,2) | | 품질 |
| ERROR_MESSAGE | VARCHAR2(2000) | | 오류 |

#### G. REQUEST (신청)

다양한 신청 유형을 한 테이블로 통합.

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| REQUEST_ID | VARCHAR2(20) | PK | 신청 ID |
| REQUEST_CATEGORY | VARCHAR2(30) | | PIPELINE/AGENT |
| REQUEST_TYPE | VARCHAR2(30) | | 신규배포/모델변경/매핑/해제/권한확대 |
| TARGET_ID | VARCHAR2(20) | | 대상 ID (PIPELINE_ID 또는 AGENT_ID) |
| REQUESTER_USER_ID | VARCHAR2(50) | | 신청자 |
| REQUEST_REASON | VARCHAR2(2000) | | 사유 |
| IMPACT_INFO | CLOB | | 영향도 정보 (JSON) |
| STATUS | VARCHAR2(20) | | 작성중/검토중/승인대기/승인/반려/철회 |
| CURRENT_APPROVER | VARCHAR2(50) | | 현재 승인자 |
| FINAL_APPROVED_AT | TIMESTAMP | | 최종 승인 시각 |

#### H. RAG_RESPONSE_LOG (AI 응답 로그)

| 컬럼 | 타입 | 키 | 설명 |
|---|---|---|---|
| RESPONSE_ID | VARCHAR2(30) | PK | 응답 ID |
| AGENT_ID | VARCHAR2(20) | FK | 응답한 에이전트 |
| USER_ID | VARCHAR2(50) | | 질의 사용자 |
| QUESTION | CLOB | | 질문 |
| ANSWER | CLOB | | 답변 |
| LLM_MODEL | VARCHAR2(100) | | 사용 LLM |
| CITED_CHUNK_IDS | VARCHAR2(2000) | | 인용 청크 IDs (CSV) |
| AVG_SCORE | NUMBER(3,2) | | 평균 검색 점수 |
| HAS_MNPI_CITATION | CHAR(1) | | MNPI 청크 포함 여부 |
| RESPONDED_AT | TIMESTAMP | | 응답 시각 |

### 4.3 보조 테이블

| 테이블 | 용도 |
|---|---|
| DOMAIN | 도메인 코드/명 마스터 (재무/리스크/상품/CRM/AML) |
| USER | SAML 인증 사용자 (기존 SAML 연동) |
| REQUEST_APPROVAL_LOG | 신청 승인 단계별 이력 |
| METADATA_AUDIT_LOG | 메타 변경 감사 로그 (등급 변경 등) |
| NOTICE | 공지사항 |

### 4.4 테이블 정의 우선순위

| 순서 | 테이블 | 이유 |
|---|---|---|
| 1 | DATASET_META, DOMAIN, USER | 메타관리 화면이 즉시 필요 |
| 2 | AI_AGENT, AGENT_DATASET_MAPPING | 매핑 화면 |
| 3 | PIPELINE, PIPELINE_RUN | Dataiku 동기화 |
| 4 | REQUEST, REQUEST_APPROVAL_LOG | 신청관리 |
| 5 | DATASET_CHUNK | DP뷰어/응답추적 |
| 6 | RAG_RESPONSE_LOG | AI 응답 추적 |
| 7 | NOTICE, METADATA_AUDIT_LOG | 후순위 |

### 4.5 데이터 모델 토의 항목

| # | 토의 항목 | 결정 결과 |
|---|---|---|
| 4-1 | DATASET_META를 정형/파일/반정형 단일 테이블로 둘 것인가, 분리할 것인가 | |
| 4-2 | 청크 테이블을 메타와 1:N으로 둘 것인가, 별도 Gold-only 테이블로 둘 것인가 | |
| 4-3 | REQUEST를 단일 테이블로 통합할 것인가, 신청유형별로 분리할 것인가 | |
| 4-4 | RAG_RESPONSE_LOG의 보존 기간 (모든 응답 저장 vs 샘플링) | |
| 4-5 | 인용 청크를 CSV로 둘 것인가, RESPONSE_CITATION 테이블 분리할 것인가 | |
| 4-6 | 메타 변경 이력(등급/소유자 변경)을 별도 감사 테이블로 둘 것인가 | |
| 4-7 | 사용자 권한 모델: 도메인별 소유 vs role 매트릭스 | |
| 4-8 | Dataiku 메타 동기화 주기와 방식 (실시간 웹훅 vs 일배치) | |
| 4-9 | 도메인 코드를 코드 테이블로 둘 것인가, ENUM으로 둘 것인가 | |
| 4-10 | 보안 등급/접근 정책을 별도 정책 테이블로 둘 것인가 | |

---

## 5. 의사결정 사항 정리 (회의 종료 시)

| 결정 항목 | 결정 결과 | 결정자 |
|---|---|---|
| 데이터 매쉬 정의 | | |
| AI 응답 추적 화면 배치 | | |
| 메타관리 탭 구성 | | |
| 1차 MVP 범위 | | |
| 1차 MVP 일정 | | |
| 신청관리 통합/분리 | | |
| 메타 테이블 통합/분리 | | |
| REQUEST 테이블 구조 | | |
| Dataiku 동기화 방식 | | |
| 권한 모델 | | |

---

## 6. 액션 아이템

| # | 액션 | 담당 | 기한 |
|---|---|---|---|
| 1 | 결정된 화면 구성을 router에 반영 | 프론트 리드 | |
| 2 | 결정된 데이터 모델을 ERD로 정리 | 백엔드 리드 | |
| 3 | Dataiku Public API 연동 가능 항목 정리 | 데이터 엔지니어 | |
| 4 | MNPI/PII 분류 기준 문서 작성 | 거버넌스 담당자 | |
| 5 | 도메인 코드/명 마스터 확정 | 기획자 | |
| 6 | 1차 MVP 화면 와이어프레임 작성 | UX/프론트 | |
| 7 | 응답 로그 보존 정책 확정 | 인프라/보안 | |
| 8 | 차주 후속 회의 일정 조율 | PM | |

---

## 7. 부록 A. 회의 진행 팁

### 7.1 회의 시작 5분 안에 확인할 것

- 참석자가 사전 자료를 읽었는가? (안 읽은 사람이 절반 이상이면 회의 연기 권장)
- 의사결정권자가 참석했는가? (백엔드 리드, PM 둘 다 필수)
- §2의 사전 합의 7개가 모두 ☑ 인가?

### 7.2 의사결정이 막힐 때

| 막히는 패턴 | 해결책 |
|---|---|
| "이거 결정하려면 X가 먼저 정해져야 한다" | X를 의제로 추가하고 본 항목은 보류 |
| "현장 운영자 의견이 필요하다" | 결정 보류, 액션 아이템으로 |
| "보안 정책에 따라 다르다" | 보안 담당자 미참석 시 다음 회의로 |
| 결정 못 짓고 시간 초과 | 우선순위 낮으면 보류, 높으면 별도 회의 |

### 7.3 한 회의에서 결정해야 할 것 vs 다음으로 미룰 것

**이번 회의에서 결정 (필수)**:

- 데이터 매쉬 정의 (안 D 권장)
- 1차 MVP 화면 범위
- DATASET_META 컬럼 구조
- REQUEST 통합/분리

**다음 회의로 미뤄도 OK**:

- 상세 ERD (PK/FK 외래키 옵션)
- 인덱스 전략
- API 엔드포인트 명명 규칙
- 화면 와이어프레임 디테일

---

## 8. 부록 B. 예시 발표 흐름 (안건 1 기준)

발표자가 회의에서 사용할 수 있는 50분 흐름.

### 8.1 (5분) 현재 상황 정리

> "오늘 합의해야 할 두 가지는 화면 구성과 데이터 모델입니다.
> 먼저 화면부터 보겠습니다.
> 현재 router에 8개 메뉴가 정의돼 있고, 11개 화면 중 2개가 구현됐습니다."

### 8.2 (10분) screen-domain과 router의 차이 설명

- screen-domain-template의 A01~G02 분류 보여주기
- router의 실제 메뉴와 매핑
- 데이터 매쉬가 신규로 추가된 점

### 8.3 (10분) 누락된 것 — AI 응답 추적

- portal-screens.md의 ④ 화면 보여주기
- "AI 챗봇이 거짓말을 했다" 시나리오 설명
- 어디에 둘지 의견 수렴 (안 A/B/C)

### 8.4 (10분) 데이터 매쉬 의도

- menu-data-definition.md의 ④ 보여주기
- 도메인 토폴로지 + 리니지 시각화 (안 D)
- 결정

### 8.5 (10분) 메타관리 보강

- 현재 다이얼로그 (기본정보 + 컬럼정보)
- 리니지/사용현황 탭 추가 검토
- 반정형 자산의 청킹/임베딩 노출 검토

### 8.6 (5분) 1차 MVP 결정

- 4.4의 우선순위 표
- 1차 오픈 화면 합의
- 일정 합의

---

## 9. 부록 C. 의사결정 후 산출물 형식

회의가 끝나면 다음 3가지를 작성한다.

### 9.1 의사결정 기록 (Decision Record)

```text
DR-001  2026-06-XX
제목: 데이터 매쉬는 도메인 토폴로지 + 리니지 통합 화면으로 정의한다
참석자: ...
배경: ...
선택지: 안 A/B/C/D
결정: 안 D
근거: ...
영향: 3차 오픈으로 미뤄짐, ERD 변경 필요
```

### 9.2 ERD 초안

§4의 8개 테이블 + 보조 테이블을 단일 ERD로 정리.
도구: draw.io, Mermaid, dbdiagram.io 중 합의된 것 사용.

### 9.3 화면 우선순위 표 (확정본)

§3.3 표를 회의 결정 결과로 갱신해 별도 문서로 보관.

---

## 10. 회의 후 검토 자료

회의 종료 후 다음 문서에 결과를 반영한다.

| 문서 | 반영 내용 |
|---|---|
| `portal-screens.md` | 결정된 화면 구성 |
| `menu-data-definition.md` | 결정된 데이터 정의 |
| `gold-metadata-lineage.md` | 변경된 메타 모델 |
| (신규) `erd-v1.md` | ERD 초안 |
| (신규) `decision-records.md` | 의사결정 기록 |

---

## 관련 문서

- `ai-data-governance-overview.md`: 본 프로젝트 본질
- `gold-metadata-lineage.md`: 메타 모델·리니지 기술 정의
- `portal-screens.md`: 5개 핵심 화면 설계
- `menu-data-definition.md`: 현재 메뉴별 데이터 정의
- `../governance/screen-domain-template.md`: 기능목록 기반 화면 분류
- `../../개선사항/프로젝트-컨텍스트-시스템프롬프트.md`: 프로젝트 전체 컨텍스트
