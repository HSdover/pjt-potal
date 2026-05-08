package com.example.governanceportal.catalog;

import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final JdbcClient jdbcClient;

    public CatalogController(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @GetMapping("/metadata")
    List<MetadataCatalogItem> metadata() {
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
                FROM metadata_catalog
                ORDER BY metadata_id
                """)
            .query(MetadataCatalogItem.class)
            .list();
    }

    @GetMapping("/lineage")
    List<LineageFlowItem> lineage() {
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

    @GetMapping("/source-sample")
    List<SourceSampleItem> sourceSample() {
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
}
