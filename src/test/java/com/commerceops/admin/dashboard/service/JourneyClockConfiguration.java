package com.commerceops.admin.dashboard.service;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class JourneyClockConfiguration {
    public static final Instant NOW = Instant.parse("2026-09-26T12:00:00Z");
    @Bean
    @Primary
    DashboardPeriodResolver journeyPeriodResolver() {
        return new DashboardPeriodResolver(Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
