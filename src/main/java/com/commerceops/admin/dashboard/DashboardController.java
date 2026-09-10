package com.commerceops.admin.dashboard;

import com.commerceops.admin.dashboard.dto.DashboardSummaryResponse;
import com.commerceops.admin.dashboard.model.DashboardPeriodShortcut;
import com.commerceops.admin.dashboard.service.DashboardService;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'READ_ONLY', 'SUPPORT')")
    public DashboardSummaryResponse summary(
            @RequestParam(required = false) DashboardPeriodShortcut period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.summary(period, from, to);
    }
}
