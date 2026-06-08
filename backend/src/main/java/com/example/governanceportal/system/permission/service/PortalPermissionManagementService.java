package com.example.governanceportal.system.permission.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.system.permission.dto.PortalPermissionAssignment;
import com.example.governanceportal.system.permission.dto.PortalPermissionAssignmentUpdateRequest;
import com.example.governanceportal.system.permission.dto.PortalPermissionDefinition;
import com.example.governanceportal.system.permission.dto.PortalPermissionManagementData;
import com.example.governanceportal.system.permission.dto.PortalPermissionSubject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class PortalPermissionManagementService {

    private static final Set<String> SUBJECT_TYPES = Set.of("IAM_ROLE", "GROUP", "USER");

    private final JdbcTemplate jdbcTemplate;

    public PortalPermissionManagementService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public PortalPermissionManagementData findManagementData() {
        return new PortalPermissionManagementData(
            findPermissions(),
            findSubjects(),
            findAssignments()
        );
    }

    @Transactional
    public PortalPermissionAssignment updateAssignment(PortalPermissionAssignmentUpdateRequest request) {
        String subjectType = normalizeSubjectType(request.subjectType());
        String subjectId = normalizeRequired(request.subjectId(), "subjectId");
        String subjectName = normalizeRequired(request.subjectName(), "subjectName");
        List<String> permissionCodes = normalizePermissionCodes(request.permissionCodes());
        validatePermissionCodes(permissionCodes);

        jdbcTemplate.update(
            """
            DELETE FROM portal_permission_assignment
            WHERE subject_type = ?
              AND subject_id = ?
            """,
            subjectType,
            subjectId
        );

        if (!permissionCodes.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            jdbcTemplate.batchUpdate(
                """
                INSERT INTO portal_permission_assignment (
                    subject_type,
                    subject_id,
                    subject_name,
                    permission_code,
                    allowed,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, TRUE, ?, ?)
                """,
                permissionCodes,
                permissionCodes.size(),
                (PreparedStatement statement, String permissionCode) -> {
                    statement.setString(1, subjectType);
                    statement.setString(2, subjectId);
                    statement.setString(3, subjectName);
                    statement.setString(4, permissionCode);
                    statement.setTimestamp(5, Timestamp.valueOf(now));
                    statement.setTimestamp(6, Timestamp.valueOf(now));
                }
            );
        }

        return new PortalPermissionAssignment(subjectType, subjectId, permissionCodes);
    }

    @Transactional(readOnly = true)
    public List<String> findEffectivePermissionCodes(String userId, Set<String> externalSubjects) {
        Set<String> permissions = new LinkedHashSet<>();

        String normalizedUserId = normalize(userId);
        if (StringUtils.hasText(normalizedUserId)) {
            permissions.addAll(findAllowedPermissionCodes("USER", normalizedUserId));
        }

        for (String subject : externalSubjects == null ? Set.<String>of() : externalSubjects) {
            String normalizedSubject = normalize(subject);
            if (!StringUtils.hasText(normalizedSubject)) {
                continue;
            }

            permissions.addAll(findAllowedPermissionCodes("GROUP", normalizedSubject));
            permissions.addAll(findIamRolePermissionCodes(normalizedSubject));
        }

        return List.copyOf(permissions);
    }

    private List<PortalPermissionDefinition> findPermissions() {
        return jdbcTemplate.query(
            """
            SELECT permission_code,
                   permission_name,
                   permission_type,
                   target_key,
                   action_code,
                   description
            FROM portal_permission
            ORDER BY permission_type, target_key, action_code, permission_code
            """,
            this::mapPermission
        );
    }

    private List<PortalPermissionSubject> findSubjects() {
        List<PortalPermissionSubject> subjects = new ArrayList<>();
        subjects.addAll(jdbcTemplate.query(
            """
            SELECT 'IAM_ROLE' AS subject_type,
                   role_code AS subject_id,
                   role_name AS subject_name,
                   description
            FROM portal_iam_role
            ORDER BY role_code
            """,
            this::mapSubject
        ));
        subjects.addAll(jdbcTemplate.query(
            """
            SELECT subject_type,
                   subject_id,
                   MAX(subject_name) AS subject_name,
                   NULL AS description
            FROM portal_permission_assignment
            WHERE subject_type IN ('GROUP', 'USER')
            GROUP BY subject_type, subject_id
            ORDER BY subject_type, subject_id
            """,
            this::mapSubject
        ));
        return subjects;
    }

    private List<PortalPermissionAssignment> findAssignments() {
        return jdbcTemplate.query(
                """
                SELECT subject_type,
                       subject_id,
                       permission_code
                FROM portal_permission_assignment
                WHERE allowed = TRUE
                ORDER BY subject_type, subject_id, permission_code
                """,
                (rs, rowNum) -> new AssignmentRow(
                    rs.getString("subject_type"),
                    rs.getString("subject_id"),
                    rs.getString("permission_code")
                )
            )
            .stream()
            .collect(
                java.util.stream.Collectors.groupingBy(
                    row -> row.subjectType() + "\u0000" + row.subjectId(),
                    java.util.LinkedHashMap::new,
                    java.util.stream.Collectors.mapping(AssignmentRow::permissionCode, java.util.stream.Collectors.toList())
                )
            )
            .entrySet()
            .stream()
            .map(entry -> {
                String[] key = entry.getKey().split("\u0000", 2);
                return new PortalPermissionAssignment(key[0], key[1], entry.getValue());
            })
            .toList();
    }

    private List<String> findAllowedPermissionCodes(String subjectType, String subjectValue) {
        return jdbcTemplate.query(
            """
            SELECT permission_code
            FROM portal_permission_assignment
            WHERE subject_type = ?
              AND allowed = TRUE
              AND (UPPER(subject_id) = UPPER(?) OR UPPER(subject_name) = UPPER(?))
            ORDER BY permission_code
            """,
            (rs, rowNum) -> rs.getString("permission_code"),
            subjectType,
            subjectValue,
            subjectValue
        );
    }

    private List<String> findIamRolePermissionCodes(String subjectValue) {
        return jdbcTemplate.query(
            """
            SELECT assignment.permission_code
            FROM portal_permission_assignment assignment
            JOIN portal_iam_role role
              ON role.role_code = assignment.subject_id
            WHERE assignment.subject_type = 'IAM_ROLE'
              AND assignment.allowed = TRUE
              AND (
                    UPPER(role.role_code) = UPPER(?)
                 OR UPPER(role.role_name) = UPPER(?)
                 OR UPPER(role.external_group_name) = UPPER(?)
                 OR REPLACE(UPPER(role.role_name), ' ', '') = REPLACE(UPPER(?), ' ', '')
                 OR REPLACE(UPPER(role.external_group_name), ' ', '') = REPLACE(UPPER(?), ' ', '')
              )
            ORDER BY assignment.permission_code
            """,
            (rs, rowNum) -> rs.getString("permission_code"),
            subjectValue,
            subjectValue,
            subjectValue,
            subjectValue,
            subjectValue
        );
    }

    private PortalPermissionDefinition mapPermission(ResultSet rs, int rowNum) throws SQLException {
        return new PortalPermissionDefinition(
            rs.getString("permission_code"),
            rs.getString("permission_name"),
            rs.getString("permission_type"),
            rs.getString("target_key"),
            rs.getString("action_code"),
            rs.getString("description")
        );
    }

    private PortalPermissionSubject mapSubject(ResultSet rs, int rowNum) throws SQLException {
        return new PortalPermissionSubject(
            rs.getString("subject_type"),
            rs.getString("subject_id"),
            rs.getString("subject_name"),
            rs.getString("description")
        );
    }

    private String normalizeSubjectType(String value) {
        String normalized = normalizeRequired(value, "subjectType").toUpperCase();
        if (!SUBJECT_TYPES.contains(normalized)) {
            throw BusinessException.badRequest("Unsupported permission subject type: " + value);
        }
        return normalized;
    }

    private List<String> normalizePermissionCodes(List<String> permissionCodes) {
        return permissionCodes.stream()
            .map(this::normalize)
            .filter(StringUtils::hasText)
            .distinct()
            .toList();
    }

    private void validatePermissionCodes(List<String> permissionCodes) {
        if (permissionCodes.isEmpty()) {
            return;
        }

        String placeholders = permissionCodes.stream().map(value -> "?").collect(java.util.stream.Collectors.joining(","));
        Integer existingCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM portal_permission WHERE permission_code IN (" + placeholders + ")",
            Integer.class,
            permissionCodes.toArray(Object[]::new)
        );
        if (existingCount == null || existingCount != permissionCodes.size()) {
            throw BusinessException.badRequest("Unknown permission code is included.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalize(value);
        if (!StringUtils.hasText(normalized)) {
            throw BusinessException.badRequest(fieldName + " is required.");
        }
        return normalized;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private record AssignmentRow(String subjectType, String subjectId, String permissionCode) {
    }
}
