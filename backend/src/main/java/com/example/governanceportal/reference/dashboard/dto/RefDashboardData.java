package com.example.governanceportal.reference.dashboard.dto;

import java.io.Serializable;
import java.util.List;

public record RefDashboardData(
    List<Kpi> kpis,
    TimeSeries timeSeries,
    CategoryBars categoryBars,
    List<StatusSlice> statusDonut
) implements Serializable {
    private static final long serialVersionUID = 1L;

    public record Kpi(String label, long value, String unit, String trend) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record TimeSeries(List<String> labels, List<Long> values) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record CategoryBars(List<String> labels, List<Long> values) implements Serializable {
        private static final long serialVersionUID = 1L;
    }

    public record StatusSlice(String name, long value) implements Serializable {
        private static final long serialVersionUID = 1L;
    }
}
