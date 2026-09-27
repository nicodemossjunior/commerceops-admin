package com.commerceops.admin.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import java.sql.DriverManager;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Testcontainers
class DemoApiIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("commerceops_demo");
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.open-in-view", () -> "false");
    }
    @Autowired TestRestTemplate http;

    @Test
    void ordinaryStartupIsEmptyAndDemoCanBeExploredThroughAuthenticatedApi() throws Exception {
        Instant reference = Instant.parse("2026-09-26T12:00:00Z");
        try (var connection = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())) {
            try (var statement = connection.createStatement(); var result = statement.executeQuery("select count(*) from product")) {
                result.next();
                assertThat(result.getInt(1)).isZero();
            }
            new DemoDatabase(connection).execute("load", 42, reference, "test-demo-password", 10);
        }
        var login = http.postForEntity("/api/auth/login", Map.of("email", "demo.admin@example.com", "password", "test-demo-password"), JsonNode.class);
        assertThat(login.getStatusCode().value()).isEqualTo(200);
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login.getBody().path("accessToken").asText());
        var summary = http.exchange("/api/dashboard/summary?period=CUSTOM&from=2026-08-27T12:00:00Z&to=2026-09-26T12:00:00Z",
                HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(summary.getStatusCode().value()).isEqualTo(200);
        var metrics = summary.getBody().path("metrics");
        var expected = DemoScenario.expected(DemoScenario.orders(42, reference), reference.minusSeconds(30L * 86400), reference);
        assertThat(metrics.path("netRevenue").decimalValue()).isEqualByComparingTo(expected.netRevenue());
        assertThat(metrics.path("orderCount").asLong()).isEqualTo(expected.orderCount());
        assertThat(metrics.path("customerCount").asInt()).isEqualTo(199);
        assertThat(metrics.path("lowStockProductCount").asInt()).isEqualTo(60);
        var products = http.exchange("/api/products?sku=DEMO-SKU-0", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(products.getBody().path("content").get(0).path("name").asText()).isEqualTo("Demo Product 0 Updated");
        var orders = http.exchange("/api/orders?status=PAID&sort=orderNumber,asc", HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
        assertThat(orders.getBody().path("totalElements").asInt()).isEqualTo(72);
        assertThat(orders.getBody().path("content").get(0).path("status").asText()).isEqualTo("PAID");
    }
}
