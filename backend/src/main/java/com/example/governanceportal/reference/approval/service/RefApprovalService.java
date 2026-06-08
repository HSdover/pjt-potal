package com.example.governanceportal.reference.approval.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.reference.approval.dto.RefApprovalDecisionRequest;
import com.example.governanceportal.reference.approval.dto.RefApprovalHistoryItem;
import com.example.governanceportal.reference.approval.dto.RefApprovalItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RefApprovalService {

    private final JdbcTemplate jdbcTemplate;

    public RefApprovalService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RefApprovalItem findById(Long id) {
        try {
            ApprovalRow row = jdbcTemplate.queryForObject(
                """
                SELECT id,
                       request_title,
                       requester_name,
                       requester_team,
                       request_type,
                       summary,
                       status,
                       submitted_at,
                       decided_at
                FROM gov_access_request
                WHERE id = ?
                """,
                this::mapApprovalRow,
                id
            );
            return toItem(row);
        } catch (EmptyResultDataAccessException error) {
            throw BusinessException.notFound("Reference approval not found: " + id);
        }
    }

    public List<RefApprovalItem> findAll() {
        return jdbcTemplate.query(
                """
                SELECT id,
                       request_title,
                       requester_name,
                       requester_team,
                       request_type,
                       summary,
                       status,
                       submitted_at,
                       decided_at
                FROM gov_access_request
                ORDER BY submitted_at DESC, id DESC
                """,
                this::mapApprovalRow
            )
            .stream()
            .map(this::toItem)
            .toList();
    }

    @Transactional
    public RefApprovalItem approve(Long id, RefApprovalDecisionRequest request) {
        return transit(id, "APPROVED", "최승인", request == null ? null : request.comment());
    }

    @Transactional
    public RefApprovalItem reject(Long id, RefApprovalDecisionRequest request) {
        return transit(id, "REJECTED", "최승인", request == null ? null : request.comment());
    }

    private RefApprovalItem transit(Long id, String toStatus, String actor, String comment) {
        RefApprovalItem current = findById(id);
        String fromStatus = current.status();
        LocalDateTime now = LocalDateTime.now();

        int updated = jdbcTemplate.update(
            """
            UPDATE gov_access_request
            SET status = ?,
                decided_at = ?
            WHERE id = ?
            """,
            toStatus,
            Timestamp.valueOf(now),
            id
        );
        if (updated == 0) {
            throw BusinessException.notFound("Reference approval not found: " + id);
        }

        jdbcTemplate.update(
            """
            INSERT INTO gov_access_request_history (
                request_id,
                from_status,
                to_status,
                actor_name,
                comment,
                occurred_at
            )
            VALUES (?, ?, ?, ?, ?, ?)
            """,
            id,
            fromStatus,
            toStatus,
            actor,
            comment,
            Timestamp.valueOf(now)
        );

        return findById(id);
    }

    private RefApprovalItem toItem(ApprovalRow row) {
        return new RefApprovalItem(
            row.id(),
            row.title(),
            row.requesterName(),
            row.requesterTeam(),
            row.requestType(),
            row.summary(),
            row.status(),
            row.submittedAt(),
            row.decidedAt(),
            findHistory(row.id())
        );
    }

    private List<RefApprovalHistoryItem> findHistory(Long requestId) {
        return jdbcTemplate.query(
            """
            SELECT from_status,
                   to_status,
                   actor_name,
                   comment,
                   occurred_at
            FROM gov_access_request_history
            WHERE request_id = ?
            ORDER BY occurred_at ASC, id ASC
            """,
            (rs, rowNum) -> new RefApprovalHistoryItem(
                rs.getString("from_status"),
                rs.getString("to_status"),
                rs.getString("actor_name"),
                rs.getString("comment"),
                toLocalDateTime(rs.getTimestamp("occurred_at"))
            ),
            requestId
        );
    }

    private ApprovalRow mapApprovalRow(ResultSet rs, int rowNum) throws SQLException {
        return new ApprovalRow(
            rs.getLong("id"),
            rs.getString("request_title"),
            rs.getString("requester_name"),
            rs.getString("requester_team"),
            rs.getString("request_type"),
            rs.getString("summary"),
            rs.getString("status"),
            toLocalDateTime(rs.getTimestamp("submitted_at")),
            toLocalDateTime(rs.getTimestamp("decided_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record ApprovalRow(
        Long id,
        String title,
        String requesterName,
        String requesterTeam,
        String requestType,
        String summary,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime decidedAt
    ) {
    }
}
