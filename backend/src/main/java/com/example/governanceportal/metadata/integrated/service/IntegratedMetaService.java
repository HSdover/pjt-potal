package com.example.governanceportal.metadata.integrated.service;

import com.example.governanceportal.common.error.BusinessException;
import com.example.governanceportal.common.list.ListResponse;
import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.list.PageSupport;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaDetail;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaItem;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaListRequest;
import com.example.governanceportal.metadata.integrated.dto.IntegratedMetaSearchFilter;
import com.example.governanceportal.metadata.integrated.dto.MetaKeyValue;
import com.example.governanceportal.metadata.integrated.dto.MetaSection;
import com.example.governanceportal.metadata.integrated.dto.StructuredColumnMeta;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class IntegratedMetaService {

    private static final String STRUCTURED = "STRUCTURED";
    private static final String FILE = "FILE";
    private static final String SEMI_STRUCTURED = "SEMI_STRUCTURED";

    private final JdbcTemplate jdbcTemplate;

    public IntegratedMetaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ListResponse<IntegratedMetaItem> search(IntegratedMetaListRequest request) {
        int pageNo = PageSupport.normalizePageNo(request == null ? null : request.pageNo());
        int pageSize = PageSupport.normalizePageSize(request == null ? null : request.pageSize());
        IntegratedMetaSearchFilter filters = request == null ? null : request.filters();
        List<ListSortRequest> sort = request == null ? List.of() : request.sort();

        List<IntegratedMetaItem> filtered = jdbcTemplate.query(
                """
                SELECT meta_id,
                       meta_type,
                       meta_name,
                       asset_kind,
                       source_system,
                       owner_department,
                       security_level,
                       updated_at
                FROM gov_integrated_meta
                """,
                this::mapItem
            )
            .stream()
            .filter(item -> matches(item, filters))
            .sorted(comparator(sort))
            .toList();

        int offset = PageSupport.offset(pageNo, pageSize);
        List<IntegratedMetaItem> rows = filtered.stream()
            .skip(offset)
            .limit(pageSize)
            .toList();

        return new ListResponse<>(rows, filtered.size(), pageNo, pageSize);
    }

    public IntegratedMetaDetail findById(String metaId) {
        IntegratedMetaItem item = findItem(metaId);
        return new IntegratedMetaDetail(
            item.metaId(),
            item.metaType(),
            item.metaName(),
            findSections(metaId),
            findColumns(metaId)
        );
    }

    private IntegratedMetaItem findItem(String metaId) {
        try {
            return jdbcTemplate.queryForObject(
                """
                SELECT meta_id,
                       meta_type,
                       meta_name,
                       asset_kind,
                       source_system,
                       owner_department,
                       security_level,
                       updated_at
                FROM gov_integrated_meta
                WHERE meta_id = ?
                """,
                this::mapItem,
                metaId
            );
        } catch (EmptyResultDataAccessException error) {
            throw BusinessException.notFound("Integrated metadata was not found: " + metaId);
        }
    }

    private List<MetaSection> findSections(String metaId) {
        return jdbcTemplate.query(
                """
                SELECT id,
                       section_id,
                       title
                FROM gov_integrated_meta_section
                WHERE meta_id = ?
                ORDER BY display_order ASC, id ASC
                """,
                (rs, rowNum) -> new SectionRow(
                    rs.getLong("id"),
                    rs.getString("section_id"),
                    rs.getString("title")
                ),
                metaId
            )
            .stream()
            .map(section -> new MetaSection(section.sectionId(), section.title(), findFields(section.id())))
            .toList();
    }

    private List<MetaKeyValue> findFields(Long sectionPk) {
        return jdbcTemplate.query(
            """
            SELECT field_key,
                   field_value
            FROM gov_integrated_meta_section_field
            WHERE section_pk = ?
            ORDER BY display_order ASC, id ASC
            """,
            (rs, rowNum) -> new MetaKeyValue(rs.getString("field_key"), rs.getString("field_value")),
            sectionPk
        );
    }

    private List<StructuredColumnMeta> findColumns(String metaId) {
        return jdbcTemplate.query(
            """
            SELECT ordinal,
                   column_name,
                   data_type,
                   nullable_yn,
                   key_type,
                   security_level,
                   description
            FROM gov_integrated_meta_column
            WHERE meta_id = ?
            ORDER BY ordinal ASC
            """,
            (rs, rowNum) -> new StructuredColumnMeta(
                rs.getInt("ordinal"),
                rs.getString("column_name"),
                rs.getString("data_type"),
                rs.getString("nullable_yn"),
                rs.getString("key_type"),
                rs.getString("security_level"),
                rs.getString("description")
            ),
            metaId
        );
    }

    private boolean matches(IntegratedMetaItem item, IntegratedMetaSearchFilter filters) {
        if (filters == null) {
            return true;
        }

        if (StringUtils.hasText(filters.metaType()) && !item.metaType().equals(filters.metaType())) {
            return false;
        }

        if (filters.searchTypes() != null) {
            List<String> searchTypes = filters.searchTypes();
            if (searchTypes.isEmpty()) {
                return false;
            }

            Set<String> itemSearchTypes = Set.copyOf(item.searchTypes());
            boolean hasSelectedType = searchTypes.stream().anyMatch(itemSearchTypes::contains);
            if (!hasSelectedType) {
                return false;
            }
        }

        String keyword = normalize(filters.keyword());
        if (!StringUtils.hasText(keyword)) {
            return true;
        }

        String lowered = keyword.toLowerCase(Locale.ROOT);
        return item.metaId().toLowerCase(Locale.ROOT).contains(lowered)
            || item.metaName().toLowerCase(Locale.ROOT).contains(lowered)
            || item.assetKind().toLowerCase(Locale.ROOT).contains(lowered)
            || item.sourceSystem().toLowerCase(Locale.ROOT).contains(lowered)
            || item.ownerDepartment().toLowerCase(Locale.ROOT).contains(lowered)
            || item.securityLevel().toLowerCase(Locale.ROOT).contains(lowered);
    }

    private Comparator<IntegratedMetaItem> comparator(List<ListSortRequest> sort) {
        List<ListSortRequest> normalizedSort = sort == null ? List.of() : sort;
        Comparator<IntegratedMetaItem> comparator = null;

        for (ListSortRequest request : normalizedSort) {
            Comparator<IntegratedMetaItem> next = comparatorFor(request.field());
            if ("desc".equalsIgnoreCase(request.direction())) {
                next = next.reversed();
            }
            comparator = comparator == null ? next : comparator.thenComparing(next);
        }

        return comparator == null
            ? Comparator.comparing(IntegratedMetaItem::updatedAt).reversed()
            : comparator;
    }

    private Comparator<IntegratedMetaItem> comparatorFor(String field) {
        return switch (field == null ? "" : field) {
            case "metaId" -> Comparator.comparing(IntegratedMetaItem::metaId, String.CASE_INSENSITIVE_ORDER);
            case "metaName" -> Comparator.comparing(IntegratedMetaItem::metaName, String.CASE_INSENSITIVE_ORDER);
            case "assetKind" -> Comparator.comparing(IntegratedMetaItem::assetKind, String.CASE_INSENSITIVE_ORDER);
            case "sourceSystem" -> Comparator.comparing(IntegratedMetaItem::sourceSystem, String.CASE_INSENSITIVE_ORDER);
            case "ownerDepartment" -> Comparator.comparing(IntegratedMetaItem::ownerDepartment, String.CASE_INSENSITIVE_ORDER);
            case "securityLevel" -> Comparator.comparing(IntegratedMetaItem::securityLevel, String.CASE_INSENSITIVE_ORDER);
            case "updatedAt" -> Comparator.comparing(IntegratedMetaItem::updatedAt);
            default -> Comparator.comparing(IntegratedMetaItem::metaId, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private IntegratedMetaItem mapItem(ResultSet rs, int rowNum) throws SQLException {
        String metaType = rs.getString("meta_type");
        return new IntegratedMetaItem(
            rs.getString("meta_id"),
            metaType,
            rs.getString("meta_name"),
            rs.getString("asset_kind"),
            rs.getString("source_system"),
            rs.getString("owner_department"),
            rs.getString("security_level"),
            searchTypes(metaType),
            toLocalDateTime(rs.getTimestamp("updated_at"))
        );
    }

    private List<String> searchTypes(String metaType) {
        return switch (metaType) {
            case STRUCTURED -> List.of("BUSINESS", "TECHNICAL", "OWNER", "SECURITY");
            case FILE -> List.of("FILE", "OWNER", "SECURITY", "STORAGE");
            case SEMI_STRUCTURED -> List.of("BUSINESS", "TECHNICAL", "OWNER", "STORAGE");
            default -> List.of();
        };
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private record SectionRow(Long id, String sectionId, String title) {
    }
}
