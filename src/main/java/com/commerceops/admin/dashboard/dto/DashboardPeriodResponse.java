package com.commerceops.admin.dashboard.dto;

import java.time.Instant;

public record DashboardPeriodResponse(Instant from, Instant to) {
}
