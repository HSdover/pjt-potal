package com.example.governanceportal.sample.repository;

import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.persistence.QueryParts;
import com.example.governanceportal.sample.dto.SampleItem;
import com.example.governanceportal.sample.dto.SampleSearchFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class SampleRepository {

    private final JdbcClient jdbcClient;

    public SampleRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public long count(SampleSearchFilter filters) {
        QueryParts queryParts = buildWhere(filters);
        Long totalCount = jdbcClient.sql("SELECT COUNT(*) FROM sample " + queryParts.where())
            .params(queryParts.params())
            .query(Long.class)
            .single();

        return totalCount == null ? 0 : totalCount;
    }

    public List<SampleItem> search(SampleSearchFilter filters, List<ListSortRequest> sort, int offset, int pageSize) {
        QueryParts queryParts = buildWhere(filters);
        String orderBy = buildOrderBy(sort);

        return jdbcClient.sql("""
                SELECT
                    sample_id,
                    sample_name,
                    description
                FROM sample
                """ + queryParts.where() + orderBy + """
                OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                """)
            .params(queryParts.params())
            .param("offset", offset)
            .param("pageSize", pageSize)
            .query(SampleItem.class)
            .list();
    }

    private QueryParts buildWhere(SampleSearchFilter filters) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filters != null && StringUtils.hasText(filters.keyword())) {
            conditions.add("""
                (
                    sample_name LIKE :keyword
                    OR description LIKE :keyword
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
            case "id" -> "sample_id";
            case "name" -> "sample_name";
            case "description" -> "description";
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
