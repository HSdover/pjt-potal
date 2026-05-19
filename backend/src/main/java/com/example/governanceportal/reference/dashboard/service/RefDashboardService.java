package com.example.governanceportal.reference.dashboard.service;

import com.example.governanceportal.reference.dashboard.dto.RefDashboardData;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.CategoryBars;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.Kpi;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.StatusSlice;
import com.example.governanceportal.reference.dashboard.dto.RefDashboardData.TimeSeries;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RefDashboardService {

    public RefDashboardData getMain() {
        List<Kpi> kpis = List.of(
            new Kpi("일일 처리 건수", 1248, "건", "+4.2%"),
            new Kpi("성공률", 98, "%", "+0.3%p"),
            new Kpi("실패 건수", 21, "건", "-1.1%"),
            new Kpi("미결 신청", 7, "건", "0")
        );

        TimeSeries timeSeries = buildTimeSeries();
        CategoryBars categoryBars = new CategoryBars(
            List.of("메타", "권한", "파이프라인", "비정형", "감사"),
            List.of(312L, 184L, 256L, 142L, 354L)
        );
        List<StatusSlice> donut = List.of(
            new StatusSlice("SUCCESS", 820),
            new StatusSlice("RUNNING", 144),
            new StatusSlice("FAILED", 21),
            new StatusSlice("PENDING", 63)
        );

        return new RefDashboardData(kpis, timeSeries, categoryBars, donut);
    }

    private TimeSeries buildTimeSeries() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        LocalDate today = LocalDate.now();
        List<String> labels = new ArrayList<>(14);
        List<Long> values = new ArrayList<>(14);
        long[] sample = {980, 1020, 1110, 980, 1050, 1180, 1240, 1190, 1280, 1300, 1220, 1250, 1310, 1248};
        for (int i = 0; i < 14; i++) {
            labels.add(today.minusDays(13 - i).format(formatter));
            values.add(sample[i]);
        }
        return new TimeSeries(labels, values);
    }
}
