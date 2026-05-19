export type RefApprovalHistoryItem = {
  fromStatus: string | null;
  toStatus: string;
  actorName: string;
  comment: string | null;
  occurredAt: string;
};

export type RefApprovalItem = {
  id: number;
  title: string;
  requesterName: string;
  requesterTeam: string;
  requestType: string;
  summary: string;
  status: string;
  submittedAt: string;
  decidedAt: string | null;
  history: RefApprovalHistoryItem[];
};

export type RefApprovalDecisionRequest = {
  comment?: string;
};
