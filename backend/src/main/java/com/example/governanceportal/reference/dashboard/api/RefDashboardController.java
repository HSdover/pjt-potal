package com.example.governanceportal.reference.dashboard.api;

import com.example.governanceportal.reference.dashboard.dto.RefDashboardData;
import com.example.governanceportal.reference.dashboard.service.RefDashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reference/dashboards")
public class RefDashboardController {

    private final RefDashboardService refDashboardService;

    public RefDashboardController(RefDashboardService refDashboardService) {
        this.refDashboardService = refDashboardService;
    }

    @GetMapping("/main")
    public RefDashboardData getMain() {
        return refDashboardService.getMain();
    }
}
