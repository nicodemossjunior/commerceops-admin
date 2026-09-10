package com.commerceops.admin.dashboard;

import com.commerceops.admin.dashboard.dto.DashboardSummaryResponse;
import com.commerceops.admin.dashboard.model.DashboardPeriodShortcut;
import com.commerceops.admin.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Operational sales, customer, order, and stock indicators")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'READ_ONLY', 'SUPPORT')")
    @Operation(
            summary = "Get the operational dashboard summary",
            description = "Returns period-based order and revenue metrics plus current customer and low-stock totals. "
                    + "CUSTOM requires inclusive from and to timestamps; the default period is LAST_30_DAYS."
    )
    public DashboardSummaryResponse summary(
            @Parameter(
                    description = "Period shortcut. CUSTOM requires both from and to.",
                    example = "LAST_30_DAYS"
            )
            @RequestParam(required = false) DashboardPeriodShortcut period,
            @Parameter(description = "Inclusive custom range start as an ISO-8601 timestamp")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Inclusive custom range end as an ISO-8601 timestamp")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.summary(period, from, to);
    }
}
