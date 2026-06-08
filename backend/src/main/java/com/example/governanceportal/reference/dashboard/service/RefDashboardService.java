package com.example.governanceportal.reference.dashboard.service;

import com.example.governanceportal.reference.dashboard.dto.RefDashboardData;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.CategoryBars;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.Kpi;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.StatusSlice;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.TimeSeries;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

@Service
public class RefDashboardService {

    private final JdbcTemplate jdbcTemplate;

    public RefDashboardService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public RefDashboardData getMain() {
        LocalDate anchorDate = latestRunDate();
        long todayProcessedCount = processedCountOn(anchorDate);
        long totalRuns = count("SELECT COUNT(*) FROM gov_pipeline_run");
        long successRuns = count("SELECT COUNT(*) FROM gov_pipeline_run WHERE status = 'SUCCESS'");
        long failedRuns = count("SELECT COUNT(*) FROM gov_pipeline_run WHERE status = 'FAILED'");
        long pendingRequests = count("SELECT COUNT(*) FROM gov_access_request WHERE status IN ('SUBMITTED', 'REVIEW')");
        long successRate = totalRuns == 0 ? 0 : Math.round(successRuns * 100.0 / totalRuns);

        List<Kpi> kpis = List.of(
            new Kpi("일일 처리 건수", todayProcessedCount, "건", trend(todayProcessedCount, processedCountOn(anchorDate.minusDays(1)))),
            new Kpi("성공률", successRate, "%", successRuns + "/" + totalRuns),
            new Kpi("실패 실행", failedRuns, "건", "Dataiku run"),
            new Kpi("미결 신청", pendingRequests, "건", "승인 대기")
        );

        TimeSeries timeSeries = buildTimeSeries(anchorDate);
        CategoryBars categoryBars = buildCategoryBars();
        List<StatusSlice> donut = buildStatusDonut();

        return new RefDashboardData(kpis, timeSeries, categoryBars, donut);
    }

    private TimeSeries buildTimeSeries(LocalDate anchorDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        Map<LocalDate, Long> countsByDate = new LinkedHashMap<>();
        LocalDate startDate = anchorDate.minusDays(13);
        RowCallbackHandler rowCallbackHandler = rs ->
            countsByDate.put(rs.getDate("run_date").toLocalDate(), rs.getLong("processed_count"));
        jdbcTemplate.query(
            """
            SELECT CAST(started_at AS DATE) AS run_date,
                   SUM(processed_count) AS processed_count
            FROM gov_pipeline_run
            WHERE started_at >= ?
            GROUP BY CAST(started_at AS DATE)
            ORDER BY run_date
            """,
            ps -> ps.setTimestamp(1, Timestamp.valueOf(startDate.atStartOfDay())),
            rowCallbackHandler
        );

        List<String> labels = new ArrayList<>(14);
        List<Long> values = new ArrayList<>(14);
        for (int i = 0; i < 14; i++) {
            LocalDate date = startDate.plusDays(i);
            labels.add(date.format(formatter));
            values.add(countsByDate.getOrDefault(date, 0L));
        }
        return new TimeSeries(labels, values);
    }

    private CategoryBars buildCategoryBars() {
        List<String> labels = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        jdbcTemplate.query(
            """
            SELECT asset_stage, COUNT(*) AS asset_count
            FROM gov_asset_catalog
            GROUP BY asset_stage
            ORDER BY asset_stage
            """,
            rs -> {
                labels.add(rs.getString("asset_stage"));
                values.add(rs.getLong("asset_count"));
            }
        );
        return new CategoryBars(labels, values);
    }

    private List<StatusSlice> buildStatusDonut() {
        return jdbcTemplate.query(
            """
            SELECT status, COUNT(*) AS status_count
            FROM gov_pipeline_run
            GROUP BY status
            ORDER BY status
            """,
            (rs, rowNum) -> new StatusSlice(rs.getString("status"), rs.getLong("status_count"))
        );
    }

    private LocalDate latestRunDate() {
        Date value = jdbcTemplate.query(
            "SELECT MAX(CAST(started_at AS DATE)) AS max_run_date FROM gov_pipeline_run",
            rs -> rs.next() ? rs.getDate("max_run_date") : null
        );
        return value == null ? LocalDate.now() : value.toLocalDate();
    }

    private long processedCountOn(LocalDate date) {
        return count(
            "SELECT COALESCE(SUM(processed_count), 0) FROM gov_pipeline_run WHERE CAST(started_at AS DATE) = ?",
            Date.valueOf(date)
        );
    }

    private long count(String sql, Object... args) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
        return value == null ? 0 : value;
    }

    private String trend(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? "0%" : "+100%";
        }
        long percent = Math.round((current - previous) * 100.0 / previous);
        return (percent > 0 ? "+" : "") + percent + "%";
    }
}
