package com.example.governanceportal.reference.board.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.reference.board.dto.RefBoardAttachment;
import com.example.governanceportal.reference.board.dto.RefBoardItem;
import com.example.governanceportal.reference.board.dto.RefBoardListRequest;
import com.example.governanceportal.reference.board.dto.RefBoardSaveRequest;
import com.example.governanceportal.reference.board.dto.RefBoardSearchFilter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RefBoardService {

    private final JdbcTemplate jdbcTemplate;

    public RefBoardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ListResponse<RefBoardItem> search(RefBoardListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request == null ? null : request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request == null ? null : request.pageSize());
        RefBoardSearchFilter filters = request == null ? null : request.filters();
        List<ListSortRequest> sort = request == null ? List.of() : request.sort();

        List<RefBoardItem> filtered = findAllRows().stream()
            .filter(item -> matches(item, filters))
            .sorted(comparator(sort))
            .toList();

        int offset = PageSupport.offset(pageNo, pageSize);
        List<RefBoardItem> rows = filtered.stream()
            .skip(offset)
            .limit(pageSize)
            .toList();

        return new ListResponse<>(rows, filtered.size(), pageNo, pageSize);
    }

    @Transactional
    public RefBoardItem findById(Long id) {
        int updated = jdbcTemplate.update(
            "UPDATE gov_ref_board SET view_count = view_count + 1 WHERE id = ?",
            id
        );
        if (updated == 0) {
            throw BusinessException.notFound("Reference board item not found: " + id);
        }
        return findExisting(id);
    }

    public RefBoardItem create(RefBoardSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        RefBoardAttachment attachment = normalizeAttachment(request.attachment());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                INSERT INTO gov_ref_board (
                    title,
                    category,
                    writer_name,
                    content,
                    attachment_id,
                    attachment_file_name,
                    attachment_size,
                    attachment_content_type,
                    attachment_download_url,
                    view_count,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            setSaveStatement(statement, request, attachment, 0, now, now);
            return statement;
        }, keyHolder);

        return findExisting(generatedId(keyHolder));
    }

    public RefBoardItem update(Long id, RefBoardSaveRequest request) {
        RefBoardItem existing = findExisting(id);
        RefBoardAttachment attachment = normalizeAttachment(request.attachment());
        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(
                """
                UPDATE gov_ref_board
                SET title = ?,
                    category = ?,
                    writer_name = ?,
                    content = ?,
                    attachment_id = ?,
                    attachment_file_name = ?,
                    attachment_size = ?,
                    attachment_content_type = ?,
                    attachment_download_url = ?,
                    view_count = ?,
                    created_at = ?,
                    updated_at = ?
                WHERE id = ?
                """
            );
            setSaveStatement(statement, request, attachment, existing.viewCount(), existing.createdAt(), LocalDateTime.now());
            statement.setLong(13, id);
            return statement;
        });
        if (updated == 0) {
            throw BusinessException.notFound("Reference board item not found: " + id);
        }
        return findExisting(id);
    }

    public void delete(Long id) {
        int deleted = jdbcTemplate.update("DELETE FROM gov_ref_board WHERE id = ?", id);
        if (deleted == 0) {
            throw BusinessException.notFound("Reference board item not found: " + id);
        }
    }

    private List<RefBoardItem> findAllRows() {
        return jdbcTemplate.query(
            """
            SELECT id,
                   title,
                   category,
                   writer_name,
                   content,
                   attachment_id,
                   attachment_file_name,
                   attachment_size,
                   attachment_content_type,
                   attachment_download_url,
                   view_count,
                   created_at,
                   updated_at
            FROM gov_ref_board
            """,
            this::mapItem
        );
    }

    private RefBoardItem findExisting(Long id) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT id,
                       title,
                       category,
                       writer_name,
                       content,
                       attachment_id,
                       attachment_file_name,
                       attachment_size,
                       attachment_content_type,
                       attachment_download_url,
                       view_count,
                       created_at,
                       updated_at
                FROM gov_ref_board
                WHERE id = ?
                """,
                this::mapItem,
                id
            );
        } catch (EmptyResultDataAccessException error) {
            throw BusinessException.notFound("Reference board item not found: " + id);
        }
    }

    private RefBoardItem mapItem(ResultSet rs, int rowNum) throws SQLException {
        return new RefBoardItem(
            rs.getLong("id"),
            rs.getString("title"),
            rs.getString("category"),
            rs.getString("writer_name"),
            rs.getString("content"),
            mapAttachment(rs),
            rs.getInt("view_count"),
            toLocalDateTime(rs.getTimestamp("created_at")),
            toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private RefBoardAttachment mapAttachment(ResultSet rs) throws SQLException {
        String attachmentId = rs.getString("attachment_id");
        if (!StringUtils.hasText(attachmentId)) {
            return null;
        }
        String downloadUrl = rs.getString("attachment_download_url");
        return new RefBoardAttachment(
            attachmentId,
            rs.getString("attachment_file_name"),
            rs.getLong("attachment_size"),
            rs.getString("attachment_content_type"),
            StringUtils.hasText(downloadUrl) ? downloadUrl : "/api/reference/boards/attachments/" + attachmentId
        );
    }

    private void setSaveStatement(
        PreparedStatement statement,
        RefBoardSaveRequest request,
        RefBoardAttachment attachment,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) throws SQLException {
        statement.setString(1, normalize(request.title()));
        statement.setString(2, normalize(request.category()));
        statement.setString(3, normalize(request.writerName()));
        statement.setString(4, normalize(request.content()));
        if (attachment == null) {
            statement.setNull(5, Types.VARCHAR);
            statement.setNull(6, Types.VARCHAR);
            statement.setNull(7, Types.BIGINT);
            statement.setNull(8, Types.VARCHAR);
            statement.setNull(9, Types.VARCHAR);
        } else {
            statement.setString(5, attachment.attachmentId());
            statement.setString(6, attachment.fileName());
            statement.setLong(7, attachment.size());
            statement.setString(8, attachment.contentType());
            statement.setString(9, attachment.downloadUrl());
        }
        statement.setInt(10, viewCount);
        statement.setTimestamp(11, Timestamp.valueOf(createdAt));
        statement.setTimestamp(12, Timestamp.valueOf(updatedAt));
    }

    private boolean matches(RefBoardItem item, RefBoardSearchFilter filters) {
        if (filters == null) {
            return true;
        }

        String category = normalize(filters.category());
        if (StringUtils.hasText(category) && !item.category().equals(category)) {
            return false;
        }

        String keyword = normalize(filters.keyword());
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String loweredKeyword = keyword.toLowerCase(Locale.ROOT);
        return item.title().toLowerCase(Locale.ROOT).contains(loweredKeyword)
            || item.writerName().toLowerCase(Locale.ROOT).contains(loweredKeyword)
            || item.content().toLowerCase(Locale.ROOT).contains(loweredKeyword);
    }

    private Comparator<RefBoardItem> comparator(List<ListSortRequest> sort) {
        List<ListSortRequest> normalizedSort = sort == null ? List.of() : sort;
        Comparator<RefBoardItem> comparator = null;

        for (ListSortRequest request : normalizedSort) {
            Comparator<RefBoardItem> next = comparatorFor(request.field());
            if ("desc".equalsIgnoreCase(request.direction())) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }

        return comparator == null ? Comparator.comparing(RefBoardItem::id).reversed() : comparator;
    }

    private Comparator<RefBoardItem> comparatorFor(String field) {
        return switch (field == null ? "" : field) {
            case "title" -> Comparator.comparing(RefBoardItem::title, String.CASE_INSENSITIVE_ORDER);
            case "category" -> Comparator.comparing(RefBoardItem::category, String.CASE_INSENSITIVE_ORDER);
            case "writerName" -> Comparator.comparing(RefBoardItem::writerName, String.CASE_INSENSITIVE_ORDER);
            case "viewCount" -> Comparator.comparingInt(RefBoardItem::viewCount);
            case "createdAt" -> Comparator.comparing(RefBoardItem::createdAt);
            case "updatedAt" -> Comparator.comparing(RefBoardItem::updatedAt);
            default -> Comparator.comparing(RefBoardItem::id);
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private RefBoardAttachment normalizeAttachment(RefBoardAttachment attachment) {
        if (attachment == null) {
            return null;
        }

        String attachmentId = normalize(attachment.attachmentId());
        return new RefBoardAttachment(
            attachmentId,
            normalize(attachment.fileName()),
            attachment.size(),
            normalize(attachment.contentType()),
            "/api/reference/boards/attachments/" + attachmentId
        );
    }

    private Long generatedId(KeyHolder keyHolder) {
        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Generated board id was not returned.");
        }
        return key.longValue();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
