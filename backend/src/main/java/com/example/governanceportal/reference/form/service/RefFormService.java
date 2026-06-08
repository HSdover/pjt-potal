package com.example.governanceportal.reference.form.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.reference.form.dto.RefFormItem;
import com.example.governanceportal.reference.form.dto.RefFormSaveRequest;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

@Service
public class RefFormService {

    private final JdbcTemplate jdbcTemplate;

    public RefFormService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RefFormItem> findAll() {
        return jdbcTemplate.query(
            """
            SELECT id, name, category, description, target_date, priority
            FROM gov_ref_form_item
            ORDER BY id DESC
            """,
            this::mapItem
        );
    }

    public RefFormItem findById(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT id, name, category, description, target_date, priority
                FROM gov_ref_form_item
                WHERE id = ?
                """,
                this::mapItem,
                id
            );
        } catch (EmptyResultDataAccessException error) {
            throw BusinessException.notFound("Reference form not found: " + id);
        }
    }

    public RefFormItem create(RefFormSaveRequest request) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                INSERT INTO gov_ref_form_item (
                    name,
                    category,
                    description,
                    target_date,
                    priority
                )
                VALUES (?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, request.name());
            statement.setString(2, request.category());
            statement.setString(3, request.description());
            setDate(statement, 4, request.targetDate());
            statement.setString(5, request.priority());
            return statement;
        }, keyHolder);

        return findById(generatedId(keyHolder));
    }

    public RefFormItem update(Long id, RefFormSaveRequest request) {
        int updated = jdbcTemplate.update(
            """
            UPDATE gov_ref_form_item
            SET name = ?,
                category = ?,
                description = ?,
                target_date = ?,
                priority = ?
            WHERE id = ?
            """,
            request.name(),
            request.category(),
            request.description(),
            toSqlDate(request.targetDate()),
            request.priority(),
            id
        );
        if (updated == 0) {
            throw BusinessException.notFound("Reference form not found: " + id);
        }
        return findById(id);
    }

    private RefFormItem mapItem(ResultSet rs, int rowNum) throws SQLException {
        return new RefFormItem(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("description"),
            toLocalDate(rs.getDate("target_date")),
            rs.getString("priority")
        );
    }

    private Long generatedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Generated form id was not returned.");
        }
        return key.longValue();
    }

    private void setDate(PreparedStatement statement, int parameterIndex, LocalDate value) throws SQLException {
        if (value == null) {
            statement.setNull(parameterIndex, Types.DATE);
            return;
        }
        statement.setDate(parameterIndex, Date.valueOf(value));
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    private LocalDate toLocalDate(Date value) {
        return value == null ? null : value.toLocalDate();
    }
}
