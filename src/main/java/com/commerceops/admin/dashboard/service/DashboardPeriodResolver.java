package com.commerceops.admin.dashboard.service;

import com.commerceops.admin.common.error.RequestValidationException;
import com.commerceops.admin.dashboard.model.DashboardPeriod;
import com.commerceops.admin.dashboard.model.DashboardPeriodShortcut;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

@Component
public class DashboardPeriodResolver {

    private final Clock clock;

    public DashboardPeriodResolver() {
        this(Clock.systemUTC());
    }

    DashboardPeriodResolver(Clock clock) {
        this.clock = clock;
    }

    public DashboardPeriod resolve(DashboardPeriodShortcut shortcut, Instant from, Instant to) {
        DashboardPeriodShortcut effectiveShortcut = shortcut == null
                ? DashboardPeriodShortcut.LAST_30_DAYS
                : shortcut;
        Instant now = clock.instant();

        if (effectiveShortcut == DashboardPeriodShortcut.CUSTOM) {
            if (from == null || to == null) {
                throw new RequestValidationException("Custom dashboard period requires both from and to values.");
            }
            validateRange(from, to);
            return new DashboardPeriod(from, to);
        }

        return switch (effectiveShortcut) {
            case TODAY -> new DashboardPeriod(startOfDay(now), now);
            case LAST_7_DAYS -> new DashboardPeriod(now.minusSeconds(7L * 24 * 60 * 60), now);
            case LAST_30_DAYS -> new DashboardPeriod(now.minusSeconds(30L * 24 * 60 * 60), now);
            case THIS_MONTH -> new DashboardPeriod(startOfMonth(now), now);
            case CUSTOM -> throw new IllegalStateException("Custom period was not resolved.");
        };
    }

    private void validateRange(Instant from, Instant to) {
        if (from.isAfter(to)) {
            throw new RequestValidationException("Dashboard period from must be before or equal to to.");
        }
    }

    private Instant startOfDay(Instant instant) {
        return instant.atZone(ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    private Instant startOfMonth(Instant instant) {
        LocalDate date = instant.atZone(ZoneOffset.UTC).toLocalDate().withDayOfMonth(1);
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }
}
