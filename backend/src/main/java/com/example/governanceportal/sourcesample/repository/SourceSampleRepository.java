package com.example.governanceportal.sourcesample.repository;

import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.persistence.QueryParts;
import com.example.governanceportal.sourcesample.dto.SourceSampleItem;
import com.example.governanceportal.sourcesample.dto.SourceSampleSearchFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class SourceSampleRepository {

    private final JdbcClient jdbcClient;

    public SourceSampleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<SourceSampleItem> findPreview() {
        return jdbcClient.sql("""
                SELECT
                    sample_id,
                    institution_name,
                    institution_type,
                    sido_name,
                    sigungu_name,
                    specialty_name,
                    opened_date
                FROM hira_institution_sample
                ORDER BY sample_id
                FETCH FIRST 20 ROWS ONLY
                """)
            .query(SourceSampleItem.class)
            .list();
    }

    public long count(SourceSampleSearchFilter filters) {
        QueryParts queryParts = buildWhere(filters);
        Long totalCount = jdbcClient.sql("SELECT COUNT(*) FROM hira_institution_sample " + queryParts.where())
            .params(queryParts.params())
            .query(Long.class)
            .single();

        return totalCount == null ? 0 : totalCount;
    }

    public List<SourceSampleItem> search(
        SourceSampleSearchFilter filters,
        List<ListSortRequest> sort,
        int offset,
        int pageSize
    ) {
        QueryParts queryParts = buildWhere(filters);
        String orderBy = buildOrderBy(sort);

        return jdbcClient.sql("""
                SELECT
                    sample_id,
                    institution_name,
                    institution_type,
                    sido_name,
                    sigungu_name,
                    specialty_name,
                    opened_date
                FROM hira_institution_sample
                """ + queryParts.where() + orderBy + """
                OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                """)
            .params(queryParts.params())
            .param("offset", offset)
            .param("pageSize", pageSize)
            .query(SourceSampleItem.class)
            .list();
    }

    private QueryParts buildWhere(SourceSampleSearchFilter filters) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filters != null && StringUtils.hasText(filters.region())) {
            conditions.add("sido_name = :region");
            params.put("region", filters.region());
        }

        if (filters != null && StringUtils.hasText(filters.keyword())) {
            conditions.add("""
                (
                    institution_name LIKE :keyword
                    OR institution_type LIKE :keyword
                    OR sido_name LIKE :keyword
                    OR sigungu_name LIKE :keyword
                    OR specialty_name LIKE :keyword
                )
                """);
            params.put("keyword", "%" + filters.keyword().trim() + "%");
        }

        if (conditions.isEmpty()) {
            return new QueryParts("", params);
        }

        return new QueryParts("WHERE " + String.join(" AND ", conditions) + " ", params);
    }

    private String buildOrderBy(List<ListSortRequest> sort) {
        if (sort == null || sort.isEmpty()) {
            return "ORDER BY sample_id ";
        }

        List<String> orderItems = sort.stream()
            .map(this::toOrderItem)
            .filter(StringUtils::hasText)
            .toList();

        if (orderItems.isEmpty()) {
            return "ORDER BY sample_id ";
        }

        return "ORDER BY " + String.join(", ", orderItems) + " ";
    }

    private String toOrderItem(ListSortRequest sort) {
        if (sort == null || !StringUtils.hasText(sort.field())) {
            return "";
        }

        String column = switch (sort.field()) {
            case "sampleId" -> "sample_id";
            case "institutionName" -> "institution_name";
            case "institutionType" -> "institution_type";
            case "sidoName" -> "sido_name";
            case "sigunguName" -> "sigungu_name";
            case "specialtyName" -> "specialty_name";
            case "openedDate" -> "opened_date";
            default -> "";
        };

        if (!StringUtils.hasText(column)) {
            return "";
        }

        String requestedDirection = sort.direction() == null ? "" : sort.direction().toLowerCase(Locale.ROOT);
        String direction = "desc".equals(requestedDirection) ? "DESC" : "ASC";
        return column + " " + direction;
    }
}
