package com.commerceops.admin.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

public record DashboardPeriodResponse(
        @Schema(description = "Inclusive resolved period start") Instant from,
        @Schema(description = "Inclusive resolved period end") Instant to
) {
}
