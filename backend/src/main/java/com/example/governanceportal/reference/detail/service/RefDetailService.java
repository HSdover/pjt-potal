package com.example.governanceportal.reference.detail.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.reference.detail.dto.RefDetailItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class RefDetailService {

    private final JdbcTemplate jdbcTemplate;

    public RefDetailService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RefDetailItem> findAll() {
        return jdbcTemplate.query(
            """
            SELECT id,
                   asset_name,
                   asset_id,
                   asset_stage,
                   owner_name,
                   description,
                   lifecycle_status,
                   created_at
            FROM gov_asset_catalog
            ORDER BY updated_at DESC, id DESC
            """,
            this::mapItem
        );
    }

    public RefDetailItem findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT id,
                       asset_name,
                       asset_id,
                       asset_stage,
                       owner_name,
                       description,
                       lifecycle_status,
                       created_at
                FROM gov_asset_catalog
                WHERE id = ?
                """,
                this::mapItem,
                id
            );
        } catch (EmptyResultDataAccessException error) {
            throw BusinessException.notFound("Reference detail not found: " + id);
        }
    }

    private RefDetailItem mapItem(ResultSet rs, int rowNum) throws SQLException {
        return new RefDetailItem(
            rs.getLong("id"),
            rs.getString("asset_name"),
            rs.getString("asset_id"),
            rs.getString("asset_stage"),
            rs.getString("owner_name"),
            rs.getString("description"),
            rs.getString("lifecycle_status"),
            toLocalDateTime(rs.getTimestamp("created_at"))
        );
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
