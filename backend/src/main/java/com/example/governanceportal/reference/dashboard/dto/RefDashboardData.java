package com.example.governanceportal.reference.dashboard.dto;

import java.util.List;

public record RefDashboardData(
    List<Kpi> kpis,
    TimeSeries timeSeries,
    CategoryBars categoryBars,
    List<StatusSlice> statusDonut
) {
    public record Kpi(String label, long value, String unit, String trend) {
    }

    public record TimeSeries(List<String> labels, List<Long> values) {
    }

    public record CategoryBars(List<String> labels, List<Long> values) {
    }

    public record StatusSlice(String name, long value) {
    }
}
