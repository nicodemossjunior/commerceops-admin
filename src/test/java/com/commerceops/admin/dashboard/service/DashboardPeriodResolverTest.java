package com.commerceops.admin.dashboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.commerceops.admin.common.error.RequestValidationException;
import com.commerceops.admin.dashboard.model.DashboardPeriodShortcut;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DashboardPeriodResolverTest {

    private static final Instant NOW = Instant.parse("2026-09-09T15:30:00Z");

    private final DashboardPeriodResolver resolver = new DashboardPeriodResolver(
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void defaultsToLastThirtyDays() {
        var period = resolver.resolve(null, null, null);

        assertThat(period.from()).isEqualTo(Instant.parse("2026-08-10T15:30:00Z"));
        assertThat(period.to()).isEqualTo(NOW);
    }

    @Test
    void resolvesUtcCalendarShortcuts() {
        assertThat(resolver.resolve(DashboardPeriodShortcut.TODAY, null, null).from())
                .isEqualTo(Instant.parse("2026-09-09T00:00:00Z"));
        assertThat(resolver.resolve(DashboardPeriodShortcut.THIS_MONTH, null, null).from())
                .isEqualTo(Instant.parse("2026-09-01T00:00:00Z"));
        assertThat(resolver.resolve(DashboardPeriodShortcut.LAST_7_DAYS, null, null).from())
                .isEqualTo(Instant.parse("2026-09-02T15:30:00Z"));
    }

    @Test
    void acceptsInclusiveCustomRange() {
        var period = resolver.resolve(DashboardPeriodShortcut.CUSTOM, NOW, NOW);

        assertThat(period.from()).isEqualTo(NOW);
        assertThat(period.to()).isEqualTo(NOW);
    }

    @Test
    void rejectsIncompleteOrReversedCustomRange() {
        assertThatThrownBy(() -> resolver.resolve(DashboardPeriodShortcut.CUSTOM, NOW, null))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("requires both from and to");
        assertThatThrownBy(() -> resolver.resolve(
                DashboardPeriodShortcut.CUSTOM,
                NOW.plusSeconds(1),
                NOW
        ))
                .isInstanceOf(RequestValidationException.class)
                .hasMessageContaining("before or equal");
    }
}
