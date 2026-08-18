package com.commerceops.admin.common.persistence;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FlywayValidationTest {

    @Autowired
    private Flyway flyway;

    @Test
    void validatesConfiguredMigrations() {
        assertThatCode(() -> flyway.validate()).doesNotThrowAnyException();
    }
}
