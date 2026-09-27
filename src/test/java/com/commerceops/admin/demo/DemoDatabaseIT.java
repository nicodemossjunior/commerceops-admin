package com.commerceops.admin.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.json.JsonMapper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class DemoDatabaseIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("commerceops_demo");
    private static final Instant REFERENCE = Instant.parse("2026-09-26T12:00:00Z");

    private Connection connect() throws Exception {
        return DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
    }

    @BeforeEach
    void clean() throws Exception {
        try (var connection = connect()) {
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbc.execute("drop schema if exists demo_support cascade");
            jdbc.execute("drop schema public cascade");
            jdbc.execute("create schema public");
        }
    }

    @Test
    void loadInspectRepeatResetAndRebuildPreserveUnrelatedRows() throws Exception {
        try (var connection = connect()) {
            var database = new DemoDatabase(connection);
            database.migrate();
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            assertThat(jdbc.queryForObject("select count(*) from product", Integer.class)).isZero();
            String manifest = database.execute("load", 42, REFERENCE, "test-demo-password", 10);
            assertThat(database.execute("inspect", 0, REFERENCE, null, 10)).isEqualTo(manifest);
            assertThat(database.execute("load", 42, REFERENCE, "test-demo-password", 10)).isEqualTo(manifest);
            assertThat(jdbc.queryForObject("select count(*) from sales_order", Integer.class)).isEqualTo(500);
            var parsed = JsonMapper.builder().findAndAddModules().build().readTree(manifest);
            var totals = jdbc.queryForMap("""
                    select sum(case when status <> 'CANCELLED' then total_amount else 0 end) gross,
                    sum(case when status not in ('CANCELLED','REFUNDED') then total_amount else 0 end) net
                    from sales_order where created_at between ? and ?
                    """, java.sql.Timestamp.from(REFERENCE.minusSeconds(30L * 86400)), java.sql.Timestamp.from(REFERENCE));
            assertThat(totals.get("gross").toString()).isEqualTo(parsed.path("expectedMetrics").path("grossRevenue").asText());
            assertThat(totals.get("net").toString()).isEqualTo(parsed.path("expectedMetrics").path("netRevenue").asText());
            assertThat(jdbc.queryForObject("select count(*) from order_item where product_name='Demo Product 0'", Integer.class)).isPositive();
            jdbc.update("insert into category(public_id,name,slug,status,created_at,updated_at) values(gen_random_uuid(),'Unrelated','unrelated','ACTIVE',now(),now())");
            database.execute("reset", 42, REFERENCE, null, 10);
            assertThat(jdbc.queryForObject("select name from category", String.class)).isEqualTo("Unrelated");
            assertThat(jdbc.queryForObject("select count(*) from sales_order", Integer.class)).isZero();
            assertThat(database.execute("load", 42, REFERENCE, "test-demo-password", 10)).isEqualTo(manifest);
        }
    }

    @Test
    void refusesConflictingInputsStaleOwnershipAndExternalReferencesAtomically() throws Exception {
        try (var connection = connect()) {
            var database = new DemoDatabase(connection);
            database.migrate();
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            database.execute("load", 42, REFERENCE, "test-demo-password", 10);
            assertThatThrownBy(() -> database.execute("load", 43, REFERENCE, "test-demo-password", 10))
                    .hasMessageContaining("conflict");
            jdbc.update("""
                    insert into customer_note(public_id, customer_id, note, created_by, created_at, updated_at)
                    select gen_random_uuid(), c.id, 'Unrelated note', a.id, now(), now()
                    from customer c cross join admin_user a limit 1
                    """);
            assertThatThrownBy(() -> database.execute("reset", 42, REFERENCE, null, 10)).isInstanceOf(Exception.class);
            assertThat(jdbc.queryForObject("select count(*) from sales_order", Integer.class)).isEqualTo(500);
            assertThat(jdbc.queryForObject("select count(*) from audit_log", Integer.class)).isPositive();
            jdbc.update("delete from customer_note where note='Unrelated note'");
            jdbc.update("delete from coupon where code='DEMO-COUPON-0'");
            assertThatThrownBy(() -> database.execute("load", 42, REFERENCE, "test-demo-password", 10)).hasMessageContaining("stale");
        }
    }

    @Test
    void rollsBackFailedLoadAndRejectsUnsafeDatabaseBeforeMigration() throws Exception {
        try (var connection = connect()) {
            var database = new DemoDatabase(connection);
            database.migrate();
            var jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
            jdbc.update("insert into category(public_id,name,slug,status,created_at,updated_at) values(gen_random_uuid(),'Conflict','demo-category-5','ACTIVE',now(),now())");
            assertThatThrownBy(() -> database.execute("load", 42, REFERENCE, "test-demo-password", 10)).isInstanceOf(Exception.class);
            assertThat(jdbc.queryForObject("select count(*) from admin_user", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("select count(*) from category", Integer.class)).isEqualTo(1);
        }
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl().replace("commerceops_demo", "postgres"),
                POSTGRES.getUsername(), POSTGRES.getPassword())) {
            assertThatThrownBy(() -> new DemoDatabase(connection)).hasMessageContaining("dedicated");
        }
    }
}
