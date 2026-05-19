export type DashboardTone = "success" | "warning" | "danger" | "info";

export type DashboardMetric = {
  id: string;
  label: string;
  value: number;
  unit: string;
  tone: DashboardTone;
  delta: string;
  description: string;
};

export type IntegrationStatus = {
  systemName: string;
  purpose: string;
  status: string;
  tone: DashboardTone;
  lastSyncedAt: string;
  latencyMs: number;
  backlogCount: number;
};

export type ApprovalQueue = {
  label: string;
  count: number;
  sla: string;
  owner: string;
  tone: DashboardTone;
};

export type FailureJob = {
  jobName: string;
  sourceSystem: string;
  failedCount: number;
  lastFailedAt: string;
  nextAction: string;
  tone: DashboardTone;
};

export type AgentUsage = {
  agentName: string;
  mappedDatasets: number;
  todayAccessCount: number;
  lastUsedAt: string;
  status: string;
  tone: DashboardTone;
};

export type DataDomainSummary = {
  label: string;
  count: number;
  ratio: number;
  tone: DashboardTone;
};

export type DashboardData = {
  asOf: string;
  overallStatus: string;
  overallTone: DashboardTone;
  metrics: DashboardMetric[];
  integrations: IntegrationStatus[];
  approvalQueues: ApprovalQueue[];
  failureJobs: FailureJob[];
  agentUsages: AgentUsage[];
  dataDomains: DataDomainSummary[];
};

export async function fetchDashboardData(): Promise<DashboardData> {
  return {
    asOf: "2026-05-18 17:00",
    overallStatus: "주의",
    overallTone: "warning",
    metrics: [
      {
        id: "catalog",
        label: "관리 데이터셋",
        value: 1284,
        unit: "건",
        tone: "success",
        delta: "+32",
        description: "DataPortal/OCI 기준 동기화 대상",
      },
      {
        id: "approval",
        label: "승인 대기",
        value: 17,
        unit: "건",
        tone: "warning",
        delta: "+5",
        description: "접근 권한, 배포, 메타 변경 요청",
      },
      {
        id: "failures",
        label: "연계 실패",
        value: 4,
        unit: "건",
        tone: "danger",
        delta: "-2",
        description: "재시도 또는 DLQ 확인 필요",
      },
      {
        id: "agents",
        label: "Agent 매핑",
        value: 86,
        unit: "건",
        tone: "info",
        delta: "+7",
        description: "Agent-데이터셋 사용 권한 매핑",
      },
      {
        id: "access",
        label: "오늘 데이터 접근",
        value: 12430,
        unit: "회",
        tone: "success",
        delta: "+11%",
        description: "사용자/Agent 데이터 조회 이력",
      },
      {
        id: "parser",
        label: "파서 재처리",
        value: 2,
        unit: "건",
        tone: "warning",
        delta: "동일",
        description: "비정형 문서 처리 실패 대기",
      },
    ],
    integrations: [
      {
        systemName: "DataPortal",
        purpose: "메타데이터/데이터셋 상세",
        status: "정상",
        tone: "success",
        lastSyncedAt: "16:58",
        latencyMs: 180,
        backlogCount: 0,
      },
      {
        systemName: "OCI Data Catalog",
        purpose: "카탈로그/분류/소유자",
        status: "지연",
        tone: "warning",
        lastSyncedAt: "16:31",
        latencyMs: 820,
        backlogCount: 12,
      },
      {
        systemName: "ADB / ORDS",
        purpose: "Golden Layer 속성 조회",
        status: "정상",
        tone: "success",
        lastSyncedAt: "16:55",
        latencyMs: 240,
        backlogCount: 0,
      },
      {
        systemName: "Dataiku",
        purpose: "파이프라인/실행 이력",
        status: "정상",
        tone: "success",
        lastSyncedAt: "16:50",
        latencyMs: 360,
        backlogCount: 1,
      },
      {
        systemName: "GenON",
        purpose: "AI Agent 정보",
        status: "정상",
        tone: "success",
        lastSyncedAt: "16:52",
        latencyMs: 210,
        backlogCount: 0,
      },
      {
        systemName: "Anyflow",
        purpose: "신청/승인 워크플로우",
        status: "주의",
        tone: "warning",
        lastSyncedAt: "16:20",
        latencyMs: 970,
        backlogCount: 5,
      },
      {
        systemName: "Parser",
        purpose: "비정형 문서 파싱 결과",
        status: "오류",
        tone: "danger",
        lastSyncedAt: "15:44",
        latencyMs: 0,
        backlogCount: 2,
      },
      {
        systemName: "IAM / Knox",
        purpose: "사용자/권한 회수 이벤트",
        status: "정상",
        tone: "success",
        lastSyncedAt: "16:57",
        latencyMs: 160,
        backlogCount: 0,
      },
    ],
    approvalQueues: [
      {
        label: "데이터 접근 권한",
        count: 9,
        sla: "4시간",
        owner: "데이터 오너",
        tone: "warning",
      },
      {
        label: "파이프라인 배포",
        count: 3,
        sla: "1일",
        owner: "운영 승인자",
        tone: "info",
      },
      {
        label: "메타데이터 변경",
        count: 5,
        sla: "8시간",
        owner: "Data Steward",
        tone: "warning",
      },
    ],
    failureJobs: [
      {
        jobName: "parser-result-callback",
        sourceSystem: "Parser",
        failedCount: 2,
        lastFailedAt: "15:44",
        nextAction: "payload 검증 후 재처리",
        tone: "danger",
      },
      {
        jobName: "oci-catalog-sync",
        sourceSystem: "OCI Data Catalog",
        failedCount: 1,
        lastFailedAt: "16:31",
        nextAction: "timeout 재시도 대기",
        tone: "warning",
      },
      {
        jobName: "anyflow-approval-pull",
        sourceSystem: "Anyflow",
        failedCount: 1,
        lastFailedAt: "16:20",
        nextAction: "승인 상태 수동 확인",
        tone: "warning",
      },
    ],
    agentUsages: [
      {
        agentName: "리서치 문서 요약 Agent",
        mappedDatasets: 14,
        todayAccessCount: 842,
        lastUsedAt: "16:59",
        status: "활성",
        tone: "success",
      },
      {
        agentName: "고객 응대 Agent",
        mappedDatasets: 11,
        todayAccessCount: 631,
        lastUsedAt: "16:47",
        status: "활성",
        tone: "success",
      },
      {
        agentName: "투자 리포트 Agent",
        mappedDatasets: 9,
        todayAccessCount: 318,
        lastUsedAt: "16:12",
        status: "검토",
        tone: "warning",
      },
      {
        agentName: "내부 지식 검색 Agent",
        mappedDatasets: 22,
        todayAccessCount: 1120,
        lastUsedAt: "17:00",
        status: "활성",
        tone: "success",
      },
    ],
    dataDomains: [
      { label: "Golden Layer", count: 312, ratio: 82, tone: "success" },
      { label: "민감정보 포함", count: 184, ratio: 48, tone: "warning" },
      { label: "비정형 문서", count: 438, ratio: 64, tone: "info" },
      { label: "권한 만료 예정", count: 27, ratio: 21, tone: "danger" },
    ],
  };
}

export function formatNumber(value: number) {
  return new Intl.NumberFormat("ko-KR").format(value);
}
