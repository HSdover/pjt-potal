package com.example.governanceportal.lineage.repository;

import com.example.governanceportal.lineage.dto.LineageFlowItem;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class LineageRepository {

    private final JdbcClient jdbcClient;

    public LineageRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<LineageFlowItem> findAll() {
        return jdbcClient.sql("""
                SELECT
                    flow_id,
                    source_name,
                    source_type,
                    target_name,
                    target_type,
                    process_name,
                    transform_type,
                    description,
                    sort_order
                FROM lineage_flow
                ORDER BY sort_order
                """)
            .query(LineageFlowItem.class)
            .list();
    }
}
