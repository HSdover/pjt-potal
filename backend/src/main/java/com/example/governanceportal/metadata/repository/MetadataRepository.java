package com.example.governanceportal.metadata.repository;

import com.example.governanceportal.common.list.ListSortRequest;
import com.example.governanceportal.common.persistence.QueryParts;
import com.example.governanceportal.metadata.dto.MetadataItem;
import com.example.governanceportal.metadata.dto.MetadataSearchFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class MetadataRepository {

    private final JdbcClient jdbcClient;

    public MetadataRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<MetadataItem> findAll() {
        return jdbcClient.sql("""
                SELECT
                    metadata_id,
                    dataset_name,
                    dataset_type,
                    source_name,
                    storage_location,
                    row_count,
                    columns_summary,
                    description
                FROM metadata_dataset
                ORDER BY metadata_id
                """)
            .query(MetadataItem.class)
            .list();
    }

    public long count(MetadataSearchFilter filters) {
        QueryParts queryParts = buildWhere(filters);
        Long totalCount = jdbcClient.sql("SELECT COUNT(*) FROM metadata_dataset " + queryParts.where())
            .params(queryParts.params())
            .query(Long.class)
            .single();

        return totalCount == null ? 0 : totalCount;
    }

    public List<MetadataItem> search(MetadataSearchFilter filters, List<ListSortRequest> sort, int offset, int pageSize) {
        QueryParts queryParts = buildWhere(filters);
        String orderBy = buildOrderBy(sort);

        return jdbcClient.sql("""
                SELECT
                    metadata_id,
                    dataset_name,
                    dataset_type,
                    source_name,
                    storage_location,
                    row_count,
                    columns_summary,
                    description
                FROM metadata_dataset
                """ + queryParts.where() + orderBy + """
                OFFSET :offset ROWS FETCH NEXT :pageSize ROWS ONLY
                """)
            .params(queryParts.params())
            .param("offset", offset)
            .param("pageSize", pageSize)
            .query(MetadataItem.class)
            .list();
    }

    private QueryParts buildWhere(MetadataSearchFilter filters) {
        List<String> conditions = new ArrayList<>();
        Map<String, Object> params = new HashMap<>();

        if (filters != null && StringUtils.hasText(filters.datasetType())) {
            conditions.add("dataset_type = :datasetType");
            params.put("datasetType", filters.datasetType());
        }

        if (filters != null && StringUtils.hasText(filters.keyword())) {
            conditions.add("""
                (
                    dataset_name LIKE :keyword
                    OR dataset_type LIKE :keyword
                    OR source_name LIKE :keyword
                    OR storage_location LIKE :keyword
                    OR columns_summary LIKE :keyword
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
            return "ORDER BY metadata_id ";
        }

        List<String> orderItems = sort.stream()
            .map(this::toOrderItem)
            .filter(StringUtils::hasText)
            .toList();

        if (orderItems.isEmpty()) {
            return "ORDER BY metadata_id ";
        }

        return "ORDER BY " + String.join(", ", orderItems) + " ";
    }

    private String toOrderItem(ListSortRequest sort) {
        if (sort == null || !StringUtils.hasText(sort.field())) {
            return "";
        }

        String column = switch (sort.field()) {
            case "metadataId" -> "metadata_id";
            case "datasetName" -> "dataset_name";
            case "datasetType" -> "dataset_type";
            case "sourceName" -> "source_name";
            case "storageLocation" -> "storage_location";
            case "rowCount" -> "row_count";
            case "columnsSummary" -> "columns_summary";
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
