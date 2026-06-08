INSERT INTO sample (
    sample_name,
    description
)
VALUES
    ('Bronze Asset Sample', 'Bronze 원천 파일 자산 등록 샘플입니다.'),
    ('Silver Parse Sample', 'Dataiku Parser 결과 연동 샘플입니다.'),
    ('Gold Chunk Sample', 'RAG 청킹/임베딩 결과 연동 샘플입니다.');

INSERT INTO sample_jpa (
    sample_name,
    description
)
VALUES
    ('JPA Sample A', 'Sample row managed through Spring Data JPA.'),
    ('JPA Sample B', 'Second sample row managed through Spring Data JPA.'),
    ('JPA Sample C', 'Third sample row managed through Spring Data JPA.'),
    ('JPA Sample D - Bronze Asset', 'Bronze 원천 파일 자산 등록 샘플입니다.'),
    ('JPA Sample E - Silver Parse', 'Dataiku Parser 결과 메타데이터 샘플입니다.'),
    ('JPA Sample F - Gold Chunk', 'RAG 청킹 결과 메타데이터 샘플입니다.'),
    ('JPA Sample G - Vector', 'Vector DB 연동 메타데이터 샘플입니다.'),
    ('JPA Sample H - Pipeline', 'Dataiku Recipe 실행 이력 샘플입니다.'),
    ('JPA Sample I - Agent Mapping', 'AI Agent 데이터셋 매핑 샘플입니다.'),
    ('JPA Sample J - Access Request', '데이터 접근 신청 샘플입니다.'),
    ('JPA Sample K - Audit', 'RAG 응답 출처 감사 샘플입니다.'),
    ('JPA Sample L - Retention', NULL),
    ('JPA Sample M - Masking', NULL),
    ('JPA Sample N - Quality', '품질 점수와 extraction confidence 확인 샘플입니다.'),
    ('JPA Sample O - Lineage', 'Bronze-Silver-Gold lineage 확인 샘플입니다.');

INSERT INTO portal_permission (
    permission_code,
    permission_name,
    permission_type,
    target_key,
    action_code,
    description
)
VALUES
    ('DASHBOARD_READ', '대시보드 조회', 'SCREEN', 'dashboard', 'READ', '일현황, 파이프라인 현황, AI 에이전트 현황 메뉴 조회 권한'),
    ('META_VIEW', '메타데이터 조회', 'SCREEN', 'metadata', 'READ', '메타관리와 통합메타관리 화면 조회 권한'),
    ('PIPELINE_READ', '파이프라인 조회', 'SCREEN', 'pipeline', 'READ', '파이프라인 목록 화면 조회 권한'),
    ('AI_AGENT_READ', 'AI 에이전트 조회', 'SCREEN', 'ai-data', 'READ', 'AI 에이전트 매핑과 데이터 매쉬 화면 조회 권한'),
    ('REQUEST_READ', '신청관리 조회', 'SCREEN', 'request', 'READ', '파이프라인/AI 에이전트 신청 목록 조회 권한'),
    ('DP_VIEWER_READ', 'DP뷰어 조회', 'SCREEN', 'dp-viewer', 'READ', '문서 파싱 목록 조회 권한'),
    ('NOTICE_READ', '공지사항 조회', 'SCREEN', 'system.notice', 'READ', '시스템 공지사항 조회 권한'),
    ('PERMISSION_MANAGE', '권한관리', 'ADMIN', 'system.permission', 'MANAGE', '사용자/그룹/IAM 역할별 포털 권한 관리 권한'),
    ('REF_VIEW', '개발참고 조회', 'SCREEN', 'development-reference', 'READ', '개발참고 메뉴와 샘플 화면 조회 권한'),
    ('SAMPLE_READ', '샘플 CRUD 조회', 'SCREEN', 'sample', 'READ', '샘플 CRUD 목록 조회 권한'),
    ('SAMPLE_CREATE', '샘플 CRUD 등록', 'CRUD', 'sample', 'CREATE', '샘플 CRUD 등록 권한'),
    ('SAMPLE_UPDATE', '샘플 CRUD 수정', 'CRUD', 'sample', 'UPDATE', '샘플 CRUD 수정 권한'),
    ('SAMPLE_DELETE', '샘플 CRUD 삭제', 'CRUD', 'sample', 'DELETE', '샘플 CRUD 삭제 권한'),
    ('SAMPLE_JPA_READ', 'JPA 샘플 조회', 'SCREEN', 'sample-jpa', 'READ', 'JPA 샘플 목록 조회 권한'),
    ('SAMPLE_JPA_CREATE', 'JPA 샘플 등록', 'CRUD', 'sample-jpa', 'CREATE', 'JPA 샘플 등록 권한'),
    ('SAMPLE_JPA_UPDATE', 'JPA 샘플 수정', 'CRUD', 'sample-jpa', 'UPDATE', 'JPA 샘플 수정 권한'),
    ('SAMPLE_JPA_DELETE', 'JPA 샘플 삭제', 'CRUD', 'sample-jpa', 'DELETE', 'JPA 샘플 삭제 권한'),
    ('SAMPLE_JPA_EXPORT', 'JPA 샘플 다운로드', 'DOWNLOAD', 'sample-jpa', 'DOWNLOAD', 'JPA 샘플 엑셀/대용량 다운로드 권한'),
    ('SAMPLE_JPA_IMPORT', 'JPA 샘플 업로드', 'UPLOAD', 'sample-jpa', 'UPLOAD', 'JPA 샘플 엑셀 업로드 권한'),
    ('BATCH_ADMIN', '배치 관리', 'ADMIN', 'batch', 'MANAGE', '배치 작업 목록 조회와 수동 실행 권한');

INSERT INTO portal_iam_role (
    role_code,
    role_name,
    external_group_name,
    description
)
VALUES
    ('ADMIN', '관리자', '관리자', 'IAM 관리자 권한. 포털 전체 메뉴와 관리 기능을 사용합니다.'),
    ('AI_AGENT_ADMIN', 'AI 에이전트관리자', 'AI 에이전트관리자', 'IAM AI 에이전트관리자 권한. AI 데이터와 신청/대시보드를 중심으로 사용합니다.'),
    ('DATA_ADMIN', '데이터 관리자', '데이터 관리자', 'IAM 데이터 관리자 권한. 메타데이터, 파이프라인, DP뷰어, 데이터 반입 기능을 사용합니다.');

INSERT INTO portal_permission_assignment (
    subject_type,
    subject_id,
    subject_name,
    permission_code,
    allowed,
    created_at,
    updated_at
)
SELECT 'IAM_ROLE', 'ADMIN', '관리자', permission_code, TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'
FROM portal_permission;

INSERT INTO portal_permission_assignment (
    subject_type,
    subject_id,
    subject_name,
    permission_code,
    allowed,
    created_at,
    updated_at
)
VALUES
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'DASHBOARD_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'META_VIEW', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'AI_AGENT_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'REQUEST_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'DP_VIEWER_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'AI_AGENT_ADMIN', 'AI 에이전트관리자', 'NOTICE_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'DASHBOARD_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'META_VIEW', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'PIPELINE_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'REQUEST_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'DP_VIEWER_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'NOTICE_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'SAMPLE_JPA_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'SAMPLE_JPA_EXPORT', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('IAM_ROLE', 'DATA_ADMIN', '데이터 관리자', 'SAMPLE_JPA_IMPORT', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00');

INSERT INTO portal_permission_assignment (
    subject_type,
    subject_id,
    subject_name,
    permission_code,
    allowed,
    created_at,
    updated_at
)
VALUES
    ('GROUP', 'AI_AGENT_TEAM', 'AI Agent 운영팀', 'DASHBOARD_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'AI_AGENT_TEAM', 'AI Agent 운영팀', 'AI_AGENT_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'AI_AGENT_TEAM', 'AI Agent 운영팀', 'REQUEST_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'DATA_STEWARD_TEAM', '데이터 스튜어드팀', 'DASHBOARD_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'DATA_STEWARD_TEAM', '데이터 스튜어드팀', 'META_VIEW', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'DATA_STEWARD_TEAM', '데이터 스튜어드팀', 'PIPELINE_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('GROUP', 'DATA_STEWARD_TEAM', '데이터 스튜어드팀', 'DP_VIEWER_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00');

INSERT INTO portal_permission_assignment (
    subject_type,
    subject_id,
    subject_name,
    permission_code,
    allowed,
    created_at,
    updated_at
)
SELECT 'USER', 'local-admin', 'Local Admin', permission_code, TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'
FROM portal_permission;

INSERT INTO portal_permission_assignment (
    subject_type,
    subject_id,
    subject_name,
    permission_code,
    allowed,
    created_at,
    updated_at
)
VALUES
    ('USER', 'local-dev', 'Local Developer', 'DASHBOARD_READ', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('USER', 'local-dev', 'Local Developer', 'META_VIEW', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00'),
    ('USER', 'local-dev', 'Local Developer', 'REF_VIEW', TRUE, TIMESTAMP '2026-06-08 00:00:00', TIMESTAMP '2026-06-08 00:00:00');

INSERT INTO gov_asset_catalog (
    asset_id,
    asset_name,
    asset_stage,
    asset_kind,
    source_system,
    storage_location,
    owner_name,
    steward_name,
    classification,
    mnpi,
    pii,
    domain_name,
    description,
    lifecycle_status,
    created_at,
    updated_at
)
VALUES
    ('BRZ-2026-000847', '고객 상담 원본 PDF', 'BRONZE', 'PDF 파일', 'ADB-CRM', 'oci://governance-bronze/customer/2026/06/call_000847.pdf', '고객데이터팀', '김스튜어드', '대외비', TRUE, TRUE, '고객', '원천 파일 보존 계층입니다. hash_sha256, page_count, classification을 관리합니다.', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-08 09:00:00'),
    ('SLV-2026-000847', '고객 상담 파싱 Markdown', 'SILVER', 'Markdown 문서', 'Dataiku Parser', 'oci://governance-silver/customer/2026/06/call_000847.md', '고객데이터팀', '김스튜어드', '대외비', TRUE, TRUE, '고객', 'Dataiku Parser가 추출한 텍스트/표/레이아웃 결과입니다.', 'ACTIVE', TIMESTAMP '2026-06-01 09:20:00', TIMESTAMP '2026-06-08 09:20:00'),
    ('GLD-2026-000847', '고객 상담 RAG 청킹 세트', 'GOLD', 'Chunk Dataset', 'Dataiku Recipe', 'oci://governance-gold/customer/2026/06/call_000847/', 'AI플랫폼팀', '박스튜어드', '대외비', TRUE, TRUE, '고객', '청킹 전략, 토큰 크기, 임베딩 모델, 총 청크 수를 관리하는 Gold 자산입니다.', 'ACTIVE', TIMESTAMP '2026-06-01 10:00:00', TIMESTAMP '2026-06-08 10:00:00'),
    ('GLD-2026-000847-CHK-0042', '고객 상담 RAG Chunk 0042', 'CHUNK', 'Text Chunk', 'Dataiku Recipe', 'vectordb://rag/customer/chunks/0042', 'AI플랫폼팀', '박스튜어드', '대외비', TRUE, TRUE, '고객', 'RAG 응답 출처 추적을 위해 page, token_count, vector_id와 연계되는 청크입니다.', 'ACTIVE', TIMESTAMP '2026-06-01 10:05:00', TIMESTAMP '2026-06-08 10:05:00'),
    ('VEC-2026-881003', '고객 상담 Vector 881003', 'VECTOR', 'Embedding Vector', 'Vector DB', 'vectordb://rag/customer/vector/881003', 'AI플랫폼팀', '박스튜어드', '대외비', TRUE, TRUE, '고객', 'embedding_model, dimensions, cost 정보를 추적하는 벡터 메타입니다.', 'ACTIVE', TIMESTAMP '2026-06-01 10:10:00', TIMESTAMP '2026-06-08 10:10:00'),
    ('BRZ-2026-000901', '거래 주문 로그 원본', 'BRONZE', 'LOG 파일', 'ADB-TRADING', 'oci://governance-bronze/trading/2026/06/order_000901.log', '트레이딩데이터팀', '이스튜어드', '내부', FALSE, FALSE, '주문', '거래 주문 로그 원본 자산입니다.', 'ACTIVE', TIMESTAMP '2026-06-02 08:00:00', TIMESTAMP '2026-06-06 08:00:00'),
    ('SLV-2026-000901', '거래 주문 로그 파싱 결과', 'SILVER', 'Structured Log', 'Dataiku Parser', 'oci://governance-silver/trading/2026/06/order_000901.jsonl', '트레이딩데이터팀', '이스튜어드', '내부', FALSE, FALSE, '주문', '로그 패턴 기반으로 파싱한 JSONL 자산입니다.', 'ACTIVE', TIMESTAMP '2026-06-02 08:10:00', TIMESTAMP '2026-06-06 08:10:00'),
    ('GLD-2026-000901', '거래 주문 RAG 청킹 세트', 'GOLD', 'Chunk Dataset', 'Dataiku Recipe', 'oci://governance-gold/trading/2026/06/order_000901/', 'AI플랫폼팀', '최스튜어드', '내부', FALSE, FALSE, '주문', '주문 관련 질의 응답에 사용하는 Gold 청킹 세트입니다.', 'ACTIVE', TIMESTAMP '2026-06-02 08:30:00', TIMESTAMP '2026-06-06 08:30:00');

INSERT INTO gov_pipeline_run (
    pipeline_name,
    recipe_id,
    recipe_version,
    dataiku_run_id,
    source_asset_id,
    target_asset_id,
    process_stage,
    status,
    processed_count,
    quality_score,
    duration_seconds,
    started_at,
    ended_at
)
VALUES
    ('bronze-to-silver-customer-parser', 'DKU-PARSE-CUSTOMER', '1.3.0', 'RUN-20260526-001', 'BRZ-2026-000847', 'SLV-2026-000847', 'SILVER', 'SUCCESS', 980, 96.40, 412, TIMESTAMP '2026-05-26 01:00:00', TIMESTAMP '2026-05-26 01:06:52'),
    ('bronze-to-silver-customer-parser', 'DKU-PARSE-CUSTOMER', '1.3.0', 'RUN-20260527-001', 'BRZ-2026-000847', 'SLV-2026-000847', 'SILVER', 'SUCCESS', 1020, 97.10, 401, TIMESTAMP '2026-05-27 01:00:00', TIMESTAMP '2026-05-27 01:06:41'),
    ('bronze-to-silver-customer-parser', 'DKU-PARSE-CUSTOMER', '1.3.0', 'RUN-20260528-001', 'BRZ-2026-000847', 'SLV-2026-000847', 'SILVER', 'SUCCESS', 1110, 97.00, 418, TIMESTAMP '2026-05-28 01:00:00', TIMESTAMP '2026-05-28 01:06:58'),
    ('silver-to-gold-customer-chunking', 'DKU-CHUNK-CUSTOMER', '2.1.0', 'RUN-20260529-001', 'SLV-2026-000847', 'GLD-2026-000847', 'GOLD', 'FAILED', 980, 82.30, 620, TIMESTAMP '2026-05-29 01:00:00', TIMESTAMP '2026-05-29 01:10:20'),
    ('silver-to-gold-customer-chunking', 'DKU-CHUNK-CUSTOMER', '2.1.1', 'RUN-20260530-001', 'SLV-2026-000847', 'GLD-2026-000847', 'GOLD', 'SUCCESS', 1050, 97.80, 588, TIMESTAMP '2026-05-30 01:00:00', TIMESTAMP '2026-05-30 01:09:48'),
    ('silver-to-gold-customer-chunking', 'DKU-CHUNK-CUSTOMER', '2.1.1', 'RUN-20260531-001', 'SLV-2026-000847', 'GLD-2026-000847', 'GOLD', 'SUCCESS', 1180, 98.20, 575, TIMESTAMP '2026-05-31 01:00:00', TIMESTAMP '2026-05-31 01:09:35'),
    ('gold-to-vector-customer-embedding', 'DKU-EMBED-CUSTOMER', '1.0.2', 'RUN-20260601-001', 'GLD-2026-000847', 'VEC-2026-881003', 'VECTOR', 'SUCCESS', 1240, 98.10, 490, TIMESTAMP '2026-06-01 01:00:00', TIMESTAMP '2026-06-01 01:08:10'),
    ('gold-to-vector-customer-embedding', 'DKU-EMBED-CUSTOMER', '1.0.2', 'RUN-20260602-001', 'GLD-2026-000847', 'VEC-2026-881003', 'VECTOR', 'SUCCESS', 1190, 97.90, 488, TIMESTAMP '2026-06-02 01:00:00', TIMESTAMP '2026-06-02 01:08:08'),
    ('bronze-to-silver-trading-parser', 'DKU-PARSE-TRADING', '1.1.0', 'RUN-20260603-001', 'BRZ-2026-000901', 'SLV-2026-000901', 'SILVER', 'SUCCESS', 1280, 96.70, 452, TIMESTAMP '2026-06-03 01:00:00', TIMESTAMP '2026-06-03 01:07:32'),
    ('silver-to-gold-trading-chunking', 'DKU-CHUNK-TRADING', '1.4.0', 'RUN-20260604-001', 'SLV-2026-000901', 'GLD-2026-000901', 'GOLD', 'SUCCESS', 1300, 97.40, 540, TIMESTAMP '2026-06-04 01:00:00', TIMESTAMP '2026-06-04 01:09:00'),
    ('silver-to-gold-trading-chunking', 'DKU-CHUNK-TRADING', '1.4.0', 'RUN-20260605-001', 'SLV-2026-000901', 'GLD-2026-000901', 'GOLD', 'RUNNING', 1220, 94.20, 0, TIMESTAMP '2026-06-05 01:00:00', NULL),
    ('gold-to-vector-customer-embedding', 'DKU-EMBED-CUSTOMER', '1.0.3', 'RUN-20260606-001', 'GLD-2026-000847', 'VEC-2026-881003', 'VECTOR', 'SUCCESS', 1250, 98.40, 478, TIMESTAMP '2026-06-06 01:00:00', TIMESTAMP '2026-06-06 01:07:58'),
    ('bronze-to-silver-customer-parser', 'DKU-PARSE-CUSTOMER', '1.3.1', 'RUN-20260607-001', 'BRZ-2026-000847', 'SLV-2026-000847', 'SILVER', 'PENDING', 1310, 0.00, 0, TIMESTAMP '2026-06-07 01:00:00', NULL),
    ('gold-to-vector-customer-embedding', 'DKU-EMBED-CUSTOMER', '1.0.3', 'RUN-20260608-001', 'GLD-2026-000847', 'VEC-2026-881003', 'VECTOR', 'SUCCESS', 1248, 98.60, 470, TIMESTAMP '2026-06-08 01:00:00', TIMESTAMP '2026-06-08 01:07:50');

INSERT INTO gov_ai_agent_mapping (
    agent_name,
    dataset_asset_id,
    permission_level,
    status,
    last_used_at,
    today_access_count
)
VALUES
    ('고객 상담 Agent', 'GLD-2026-000847', 'READ_MASKED', 'ACTIVE', TIMESTAMP '2026-06-08 11:20:00', 284),
    ('주문 분석 Agent', 'GLD-2026-000901', 'READ_INTERNAL', 'ACTIVE', TIMESTAMP '2026-06-08 10:55:00', 97),
    ('감사 대응 Agent', 'GLD-2026-000847-CHK-0042', 'READ_AUDIT', 'REVIEW', TIMESTAMP '2026-06-07 18:40:00', 12);

INSERT INTO gov_access_request (
    id,
    request_title,
    request_type,
    requester_name,
    requester_team,
    summary,
    status,
    target_date,
    priority,
    submitted_at,
    decided_at
)
VALUES
    (1, 'Agent A의 고객 분석 데이터셋 접근 신청', 'DATA_ACCESS', '김신청', 'AI 추진팀', 'Agent A가 GLD-2026-000847 고객 상담 RAG 청킹 세트에 read 권한을 신청했습니다.', 'REVIEW', DATE '2026-06-15', 'HIGH', TIMESTAMP '2026-05-10 10:12:00', NULL),
    (2, '전처리 파이프라인 상용 배포 신청', 'PIPELINE_DEPLOY', '이배포', '데이터플랫폼팀', 'DKU-CHUNK-CUSTOMER 2.1.1 버전을 상용 환경에 반영합니다.', 'APPROVED', DATE '2026-05-20', 'NORMAL', TIMESTAMP '2026-05-05 14:00:00', TIMESTAMP '2026-05-07 11:20:00'),
    (3, 'Vector DB 신규 컬렉션 접근 신청', 'VECTOR_ACCESS', '박분석', 'AI서비스팀', 'RAG 응답 출처 검증을 위해 VEC-2026-881003 조회 권한을 신청했습니다.', 'SUBMITTED', DATE '2026-06-18', 'NORMAL', TIMESTAMP '2026-06-08 09:30:00', NULL);

INSERT INTO gov_access_request_history (
    request_id,
    from_status,
    to_status,
    actor_name,
    comment,
    occurred_at
)
VALUES
    (1, NULL, 'DRAFT', '김신청', '초안 작성', TIMESTAMP '2026-05-09 18:00:00'),
    (1, 'DRAFT', 'SUBMITTED', '김신청', '결재 상신', TIMESTAMP '2026-05-10 10:12:00'),
    (1, 'SUBMITTED', 'REVIEW', '박검토', '데이터 분류와 마스킹 정책 검토 시작', TIMESTAMP '2026-05-11 09:30:00'),
    (2, NULL, 'DRAFT', '이배포', '초안 작성', TIMESTAMP '2026-05-04 16:00:00'),
    (2, 'DRAFT', 'SUBMITTED', '이배포', '결재 상신', TIMESTAMP '2026-05-05 14:00:00'),
    (2, 'SUBMITTED', 'APPROVED', '최승인', 'Dataiku run 검증 결과 양호', TIMESTAMP '2026-05-07 11:20:00'),
    (3, NULL, 'SUBMITTED', '박분석', '권한 신청', TIMESTAMP '2026-06-08 09:30:00');

INSERT INTO gov_ref_form_item (
    name,
    category,
    description,
    target_date,
    priority
)
VALUES
    ('Dataiku Recipe 메타 동기화 신청', '파이프라인', 'recipe_id, run_id, 파라미터, 품질 점수를 포털에 동기화합니다.', DATE '2026-06-15', 'HIGH'),
    ('AI Agent 데이터셋 매핑 등록', 'AI Agent', '고객 상담 Agent와 Gold 청킹 세트를 연결합니다.', DATE '2026-07-01', 'NORMAL'),
    ('민감정보 마스킹 정책 검토', '보안', 'MNPI/PII 포함 자산의 마스킹 정책을 검토합니다.', DATE '2026-06-20', 'HIGH');

INSERT INTO gov_ref_board (
    title,
    category,
    writer_name,
    content,
    view_count,
    created_at,
    updated_at
)
VALUES
    ('RAG 데이터 품질 점검 기준 공유', '공지', '데이터관리팀', 'RAG 청킹 품질과 응답 출처 검증 기준을 공유합니다.\n\n- extraction_confidence\n- chunk token count\n- source page mapping', 12, TIMESTAMP '2026-06-01 09:30:00', TIMESTAMP '2026-06-01 09:30:00'),
    ('AI 데이터셋 반입 요청 양식', '자료', 'AI플랫폼팀', '신규 데이터셋 반입 요청 시 필요한 Bronze/Silver/Gold 메타 항목과 검토 절차를 정리했습니다.', 8, TIMESTAMP '2026-06-03 14:10:00', TIMESTAMP '2026-06-03 14:10:00'),
    ('파이프라인 배치 점검 안내', '운영', '운영지원팀', 'Dataiku Recipe 실행 이력과 Vector DB 동기화 점검 시간을 안내합니다.', 5, TIMESTAMP '2026-06-05 17:00:00', TIMESTAMP '2026-06-05 17:00:00');

INSERT INTO gov_integrated_meta (
    meta_id,
    meta_type,
    meta_name,
    asset_kind,
    source_system,
    owner_department,
    security_level,
    updated_at
)
VALUES
    ('STM-001', 'STRUCTURED', '고객 기본 테이블', '테이블', 'ADB-CRM', '고객데이터팀', '대외비', TIMESTAMP '2026-06-07 10:20:00'),
    ('STM-002', 'STRUCTURED', '주문 체결 테이블', '테이블', 'ADB-TRADING', '트레이딩데이터팀', '내부', TIMESTAMP '2026-06-06 15:35:00'),
    ('FIM-001', 'FILE', '고객 상담 원본 PDF', 'PDF 파일', 'ADB-CRM', '고객데이터팀', '대외비', TIMESTAMP '2026-06-08 09:10:00'),
    ('FIM-002', 'FILE', '접근 감사 로그', '로그 파일', 'PORTAL', '플랫폼운영팀', '내부', TIMESTAMP '2026-06-05 18:00:00'),
    ('SSM-001', 'SEMI_STRUCTURED', '고객 이벤트 Topic', 'Kafka Topic', 'ADB-MOBILE', '디지털채널팀', '내부', TIMESTAMP '2026-06-04 13:15:00'),
    ('SSM-002', 'SEMI_STRUCTURED', '투자성향 프로파일 JSON', 'JSON Document', 'ADB-WM', '자산관리데이터팀', '대외비', TIMESTAMP '2026-06-02 11:00:00');

INSERT INTO gov_integrated_meta_section (
    id,
    meta_id,
    section_id,
    title,
    display_order
)
VALUES
    (1, 'STM-001', 'basic', '기본정보', 1),
    (2, 'STM-002', 'basic', '기본정보', 1),
    (3, 'FIM-001', 'basic', '기본정보', 1),
    (4, 'FIM-001', 'security', '보안정보', 2),
    (5, 'FIM-001', 'parsing', '파싱정보', 3),
    (6, 'FIM-001', 'chunking', '청킹정보', 4),
    (7, 'FIM-001', 'storage', '저장정보', 5),
    (8, 'FIM-002', 'basic', '기본정보', 1),
    (9, 'FIM-002', 'security', '보안정보', 2),
    (10, 'FIM-002', 'parsing', '파싱정보', 3),
    (11, 'FIM-002', 'chunking', '청킹정보', 4),
    (12, 'FIM-002', 'storage', '저장정보', 5),
    (13, 'SSM-001', 'basic', '기본정보', 1),
    (14, 'SSM-001', 'schema', '스키마정보', 2),
    (15, 'SSM-002', 'basic', '기본정보', 1),
    (16, 'SSM-002', 'schema', '스키마정보', 2);

INSERT INTO gov_integrated_meta_section_field (
    section_pk,
    field_key,
    field_value,
    display_order
)
VALUES
    (1, '메타ID', 'STM-001', 1),
    (1, '자산유형', '테이블', 2),
    (1, '논리명', '고객 기본', 3),
    (1, '물리명', 'TB_CUSTOMER_BASE', 4),
    (1, '원천시스템', 'ADB-CRM', 5),
    (1, '담당부서', '고객데이터팀', 6),
    (1, '보안등급', '대외비', 7),
    (1, '업무도메인', '고객', 8),
    (1, '최종수정일', '2026-06-07T10:20:00', 9),
    (2, '메타ID', 'STM-002', 1),
    (2, '자산유형', '테이블', 2),
    (2, '논리명', '주문 체결', 3),
    (2, '물리명', 'TB_ORDER_EXECUTION', 4),
    (2, '원천시스템', 'ADB-TRADING', 5),
    (2, '담당부서', '트레이딩데이터팀', 6),
    (2, '보안등급', '내부', 7),
    (2, '업무도메인', '주문', 8),
    (2, '최종수정일', '2026-06-06T15:35:00', 9),
    (3, '메타ID', 'FIM-001', 1),
    (3, '자산유형', 'PDF 파일', 2),
    (3, '파일명', 'call_000847.pdf', 3),
    (3, 'asset_id', 'BRZ-2026-000847', 4),
    (3, '원천시스템', 'ADB-CRM', 5),
    (3, '담당부서', '고객데이터팀', 6),
    (3, '보안등급', '대외비', 7),
    (3, '최종수정일', '2026-06-08T09:10:00', 8),
    (4, '개인정보포함', 'Y', 1),
    (4, 'MNPI포함', 'Y', 2),
    (4, '마스킹정책', '고객명/생년월일 부분 마스킹', 3),
    (4, '접근권한', 'CRM_DATA_OWNER', 4),
    (5, 'parser', 'Dataiku Parser', 1),
    (5, 'parsed_at', '2026-06-01T09:20:00', 2),
    (5, 'total_tokens', '124,800', 3),
    (5, 'extraction_confidence', '97.8', 4),
    (6, 'chunk_strategy', 'semantic-page', 1),
    (6, 'chunk_size_tokens', '800', 2),
    (6, 'chunk_overlap', '120', 3),
    (6, 'total_chunks', '412', 4),
    (7, '저장소', 'OCI Object Storage', 1),
    (7, 'Bronze 경로', 'oci://governance-bronze/customer/2026/06/call_000847.pdf', 2),
    (7, 'Silver 경로', 'oci://governance-silver/customer/2026/06/call_000847.md', 3),
    (7, 'Gold 경로', 'oci://governance-gold/customer/2026/06/call_000847/', 4),
    (8, '메타ID', 'FIM-002', 1),
    (8, '자산유형', '로그 파일', 2),
    (8, '파일명', 'access_audit_yyyyMMdd.log', 3),
    (8, '원천시스템', 'PORTAL', 4),
    (8, '담당부서', '플랫폼운영팀', 5),
    (8, '보안등급', '내부', 6),
    (8, '최종수정일', '2026-06-05T18:00:00', 7),
    (9, '개인정보포함', 'N', 1),
    (9, '암호화여부', 'Y', 2),
    (9, '마스킹정책', '계정 식별자 해시 처리', 3),
    (9, '접근권한', 'SECURITY_AUDITOR', 4),
    (10, '파일포맷', 'LOG', 1),
    (10, '인코딩', 'UTF-8', 2),
    (10, '패턴', 'timestamp|user|action|resource', 3),
    (10, '헤더존재', 'N', 4),
    (11, '청킹전략', 'time-window', 1),
    (11, '청크크기', '1 hour', 2),
    (11, '오버랩', '5 minutes', 3),
    (12, '저장소', 'NAS', 1),
    (12, '마운트', '/data/audit', 2),
    (12, '경로', '/portal/access/', 3),
    (12, '보존기간', '3년', 4),
    (13, '메타ID', 'SSM-001', 1),
    (13, '자산유형', 'Kafka Topic', 2),
    (13, '스키마명', 'customer.event.v1', 3),
    (13, '원천시스템', 'ADB-MOBILE', 4),
    (13, '담당부서', '디지털채널팀', 5),
    (13, '보안등급', '내부', 6),
    (13, '최종수정일', '2026-06-04T13:15:00', 7),
    (14, '포맷', 'JSON', 1),
    (14, '스키마레지스트리', 'schema-registry-01', 2),
    (14, '키필드', 'customerId', 3),
    (14, '필수필드', 'eventId,eventType,eventTime', 4),
    (15, '메타ID', 'SSM-002', 1),
    (15, '자산유형', 'JSON Document', 2),
    (15, '문서명', 'investment_profile', 3),
    (15, '원천시스템', 'ADB-WM', 4),
    (15, '담당부서', '자산관리데이터팀', 5),
    (15, '보안등급', '대외비', 6),
    (15, '최종수정일', '2026-06-02T11:00:00', 7),
    (16, '포맷', 'JSON', 1),
    (16, '루트노드', '$.profile', 2),
    (16, '배열필드', 'riskAnswers', 3),
    (16, '필수필드', 'profileId,customerId,riskGrade', 4);

INSERT INTO gov_integrated_meta_column (
    meta_id,
    ordinal,
    column_name,
    data_type,
    nullable_yn,
    key_type,
    security_level,
    description
)
VALUES
    ('STM-001', 1, 'CUST_ID', 'VARCHAR(20)', 'N', 'PK', '대외비', '고객 식별자'),
    ('STM-001', 2, 'CUST_NM', 'VARCHAR(100)', 'N', '-', '대외비', '고객명'),
    ('STM-001', 3, 'BIRTH_DT', 'DATE', 'Y', '-', '민감', '생년월일'),
    ('STM-001', 4, 'JOIN_DT', 'DATE', 'N', '-', '일반', '가입일'),
    ('STM-002', 1, 'ORDER_ID', 'VARCHAR(30)', 'N', 'PK', '내부', '주문 식별자'),
    ('STM-002', 2, 'ACCOUNT_NO', 'VARCHAR(20)', 'N', 'FK', '대외비', '계좌번호'),
    ('STM-002', 3, 'SYMBOL', 'VARCHAR(20)', 'N', '-', '일반', '종목코드'),
    ('STM-002', 4, 'EXEC_QTY', 'NUMBER(18,4)', 'N', '-', '내부', '체결수량');
