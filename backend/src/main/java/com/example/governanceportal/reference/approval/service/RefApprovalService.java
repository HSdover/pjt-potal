package com.example.governanceportal.reference.approval.service;

import com.example.governanceportal.reference.approval.dto.RefApprovalDecisionRequest;
import com.example.governanceportal.reference.approval.dto.RefApprovalHistoryItem;
import com.example.governanceportal.reference.approval.dto.RefApprovalItem;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RefApprovalService {

    private final Map<Long, RefApprovalItem> store = new LinkedHashMap<>();

    public RefApprovalService() {
        store.put(1L, new RefApprovalItem(
            1L,
            "Agent A의 고객 분석 데이터셋 접근 신청",
            "김신청",
            "AI 추진팀",
            "DATA_ACCESS",
            "Agent A가 고객 분석 데이터셋(customer_analytics)에 read 권한 신청",
            "REVIEW",
            LocalDateTime.of(2026, 5, 10, 10, 12),
            null,
            new ArrayList<>(List.of(
                new RefApprovalHistoryItem(null, "DRAFT", "김신청", "초안 작성", LocalDateTime.of(2026, 5, 9, 18, 0)),
                new RefApprovalHistoryItem("DRAFT", "SUBMITTED", "김신청", "결재 상신", LocalDateTime.of(2026, 5, 10, 10, 12)),
                new RefApprovalHistoryItem("SUBMITTED", "REVIEW", "박검토", "검토 시작", LocalDateTime.of(2026, 5, 11, 9, 30))
            ))
        ));
        store.put(2L, new RefApprovalItem(
            2L,
            "전처리 파이프라인 상용 배포 신청",
            "이배포",
            "데이터플랫폼팀",
            "PIPELINE_DEPLOY",
            "신규 전처리 파이프라인(daily_summary_v3)의 상용 환경 반영",
            "APPROVED",
            LocalDateTime.of(2026, 5, 5, 14, 0),
            LocalDateTime.of(2026, 5, 7, 11, 20),
            new ArrayList<>(List.of(
                new RefApprovalHistoryItem(null, "DRAFT", "이배포", "초안 작성", LocalDateTime.of(2026, 5, 4, 16, 0)),
                new RefApprovalHistoryItem("DRAFT", "SUBMITTED", "이배포", "결재 상신", LocalDateTime.of(2026, 5, 5, 14, 0)),
                new RefApprovalHistoryItem("SUBMITTED", "APPROVED", "최승인", "테스트 결과 양호", LocalDateTime.of(2026, 5, 7, 11, 20))
            ))
        ));
    }

    public RefApprovalItem findById(Long id) {
        RefApprovalItem item = store.get(id);
        if (item == null) {
            throw new IllegalArgumentException("Reference approval not found: " + id);
        }
        return item;
    }

    public List<RefApprovalItem> findAll() {
        return List.copyOf(store.values());
    }

    public RefApprovalItem approve(Long id, RefApprovalDecisionRequest request) {
        return transit(id, "APPROVED", "최승인", request == null ? null : request.comment());
    }

    public RefApprovalItem reject(Long id, RefApprovalDecisionRequest request) {
        return transit(id, "REJECTED", "최승인", request == null ? null : request.comment());
    }

    private RefApprovalItem transit(Long id, String toStatus, String actor, String comment) {
        RefApprovalItem current = findById(id);
        String fromStatus = current.status();
        LocalDateTime now = LocalDateTime.now();

        List<RefApprovalHistoryItem> history = new ArrayList<>(current.history());
        history.add(new RefApprovalHistoryItem(fromStatus, toStatus, actor, comment, now));

        RefApprovalItem updated = new RefApprovalItem(
            current.id(),
            current.title(),
            current.requesterName(),
            current.requesterTeam(),
            current.requestType(),
            current.summary(),
            toStatus,
            current.submittedAt(),
            now,
            history
        );
        store.put(id, updated);
        return updated;
    }
}
