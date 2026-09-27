package com.commerceops.admin.journeys;

import static org.assertj.core.api.Assertions.assertThat;

import com.commerceops.admin.dashboard.service.JourneyClockConfiguration;
import com.commerceops.admin.demo.DemoScenario;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@Import(JourneyClockConfiguration.class)
@Testcontainers
class ApiJourneysIT {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("commerceops_journeys");
    private static final String PASSWORD = UUID.randomUUID().toString();
    private static final String SECRET = UUID.randomUUID().toString();
    private static final Instant NOW = JourneyClockConfiguration.NOW;
    private static final List<String> ROLES = List.of("ADMIN", "MANAGER", "SUPPORT", "CATALOG", "READ_ONLY");
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final Map<String, String> tokens = new HashMap<>();
    @Autowired com.commerceops.admin.audit.service.AuditRecorder auditRecorder;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired PasswordEncoder encoder;
    @LocalServerPort int port;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.open-in-view", () -> "false");
        registry.add("commerceops.security.jwt.secret", () -> SECRET);
        registry.add("commerceops.security.jwt.access-token-ttl-seconds", () -> "3600");
    }

    @BeforeEach
    void prepareIndependentDatabaseAndRoleAccounts() throws Exception {
        assertThat(jdbc.queryForObject("select current_database()", String.class)).isEqualTo("commerceops_journeys");
        jdbc.execute("truncate audit_log, order_status_history, order_item, sales_order, customer_note, coupon, product, category, customer, admin_user_role, admin_user restart identity cascade");
        String hash = encoder.encode(PASSWORD);
        for (String role : ROLES) {
            Long id = jdbc.queryForObject("""
                    insert into admin_user(public_id,name,email,password_hash,status,created_at,updated_at)
                    values(?, ?, ?, ?, 'ACTIVE', now(), now()) returning id
                    """, Long.class, UUID.randomUUID(), role + " User", email(role), hash);
            jdbc.update("insert into admin_user_role select ?, id from role where name=?", id, role);
            JsonNode login = request("POST", "/api/auth/login", Map.of("email", email(role), "password", PASSWORD), null, 200);
            assertThat(login.path("accessToken").asText().isBlank()).isFalse();
            tokens.put(role, login.path("accessToken").asText());
        }
    }

    private String email(String role) { return role.toLowerCase(java.util.Locale.ROOT) + "@example.com"; }
    private String id(JsonNode node) {
        assertThat(node.has("id")).isFalse();
        String value = node.path("publicId").asText();
        assertThat(UUID.fromString(value)).isNotNull();
        return value;
    }
    private JsonNode request(String method, String path, Object body, String token, int expected) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).timeout(Duration.ofSeconds(20));
        if (token != null) { builder.header("Authorization", "Bearer " + token); }
        builder.header("Content-Type", "application/json");
        builder.method(method, body == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
        var response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        // Only method/path/status appear in failures: never print login bodies or bearer tokens.
        assertThat(response.statusCode()).as("%s %s HTTP status", method, path).isEqualTo(expected);
        JsonNode result = response.body().isBlank() ? json.createObjectNode() : json.readTree(response.body());
        if (expected >= 400) {
            assertThat(result.path("status").asInt()).isEqualTo(expected);
            assertThat(result.path("path").asText()).isEqualTo(path.split("\\?")[0]);
            assertThat(result.path("message").asText()).isNotBlank();
            assertThat(result.path("timestamp").asText()).isNotBlank();
            assertThat(result.path("traceId").asText()).isNotBlank();
        }
        if (expected < 400) { assertThat(result.findValues("id")).as("Internal entity IDs must not be exposed").isEmpty(); }
        return result;
    }
    private JsonNode call(String method, String path, Object body, String role, int expected) throws Exception {
        return request(method, path, body, tokens.get(role), expected);
    }
    private JsonNode get(String path) throws Exception { return call("GET", path, null, "ADMIN", 200); }
    private JsonNode post(String path, Object body) throws Exception { return call("POST", path, body, "ADMIN", 201); }
    private void error(String method, String path, Object body, int status, String code) throws Exception {
        assertThat(call(method, path, body, "ADMIN", status).path("code").asText()).isEqualTo(code);
    }
    private Map<String, Object> category(String name) { return Map.of("name", name, "slug", name.toLowerCase(java.util.Locale.ROOT), "status", "ACTIVE"); }
    private Map<String, Object> product(String category, String name, int price, int stock, String status) {
        return Map.of("categoryPublicId", category, "sku", name, "name", name, "slug", name.toLowerCase(java.util.Locale.ROOT),
                "price", price, "stockQuantity", stock, "status", status);
    }
    private Map<String, Object> customer(String name, String status) {
        return Map.of("name", name, "email", name.toLowerCase(java.util.Locale.ROOT) + "@example.com", "phone", "15551234567", "status", status);
    }
    private Map<String, Object> coupon(String code, String type, String status) {
        return Map.of("code", code, "discountType", type, "discountValue", 10, "status", status, "usageLimit", 5, "perCustomerLimit", 1);
    }
    private void matches(String path, String... expectedIds) throws Exception {
        JsonNode page = get(path);
        assertThat(page.path("totalElements").asInt()).as(path).isEqualTo(expectedIds.length);
        List<String> actual = new ArrayList<>();
        page.path("content").forEach(node -> actual.add(id(node)));
        assertThat(actual).as(path).containsExactlyInAnyOrder(expectedIds);
    }

    @Test
    void authenticationRejectsMissingInvalidExpiredTokensAndBadCredentials() throws Exception {
        var me = get("/api/auth/me");
        assertThat(me.path("email").asText()).isEqualTo(email("ADMIN"));
        id(me);
        assertThat(request("POST", "/api/auth/login", Map.of("email", email("ADMIN"), "password", "wrong"), null, 401)
                .path("code").asText()).isEqualTo("AUTHENTICATION_FAILED");
        for (String token : List.of("invalid", expiredToken(tokens.get("ADMIN")))) {
            assertThat(request("GET", "/api/auth/me", null, token, 401).path("code").asText()).isEqualTo("AUTHENTICATION_FAILED");
        }
        assertThat(request("GET", "/api/auth/me", null, null, 401).path("code").asText()).isEqualTo("AUTHENTICATION_FAILED");
        error("POST", "/api/auth/login", Map.of("email", "invalid", "password", ""), 400, "VALIDATION_ERROR");
    }

    private String expiredToken(String token) throws Exception {
        String[] parts = token.split("\\.");
        ObjectNode payload = (ObjectNode) json.readTree(Base64.getUrlDecoder().decode(parts[1]));
        payload.put("exp", 1);
        String unsigned = parts[0] + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(json.writeValueAsBytes(payload));
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return unsigned + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(unsigned.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void catalogRegistrationFiltersPaginationAndSoftDeletion() throws Exception {
        String a = id(post("/api/categories", category("Alpha")));
        String b = id(post("/api/categories", category("Beta")));
        error("POST", "/api/categories", category("Alpha"), 409, "DUPLICATE_RESOURCE");
        String p = id(post("/api/products", product(a, "Keyboard", 100, 10, "ACTIVE")));
        String q = id(post("/api/products", product(b, "Mouse", 200, 11, "INACTIVE")));
        for (String filter : List.of("categoryId=" + a, "status=ACTIVE", "sku=key", "name=BOARD", "minPrice=100&maxPrice=100", "maxPrice=100", "lowStock=true")) {
            matches("/api/products?" + filter, p);
        }
        matches("/api/products?minPrice=200", q);
        matches("/api/products?lowStock=false", q);
        matches("/api/products?categoryId=" + a + "&status=ACTIVE&sku=key&name=board&minPrice=100&maxPrice=100&lowStock=true", p);
        assertPages("/api/products", "name", List.of(p, q));
        assertPages("/api/categories", "name", List.of(a, b));
        assertThat(get("/api/products/" + p).path("price").decimalValue()).isEqualByComparingTo("100");
        error("POST", "/api/products", product(a, "Keyboard", 100, 10, "ACTIVE"), 409, "DUPLICATE_RESOURCE");
        error("DELETE", "/api/categories/" + a, null, 422, "BUSINESS_RULE_VIOLATION");
        assertThat(get("/api/categories/" + a).path("status").asText()).isEqualTo("ACTIVE");
        call("PUT", "/api/products/" + p, product(a, "Keyboard", 120, 0, "OUT_OF_STOCK"), "MANAGER", 200);
        assertThat(get("/api/products/" + p).path("stockQuantity").asInt()).isZero();
        call("PUT", "/api/categories/" + a, Map.of("name", "Alpha Updated", "slug", "alpha", "status", "INACTIVE"), "MANAGER", 200);
        assertThat(get("/api/categories/" + a).path("name").asText()).isEqualTo("Alpha Updated");
        call("DELETE", "/api/products/" + p, null, "CATALOG", 204);
        error("GET", "/api/products/" + p, null, 404, "RESOURCE_NOT_FOUND");
        matches("/api/products", q);
        String replacement = id(post("/api/products", product(a, "Keyboard", 100, 10, "DRAFT")));
        assertThat(replacement).isNotEqualTo(p);
        call("DELETE", "/api/categories/" + a, null, "CATALOG", 204);
        matches("/api/categories", b);
        error("GET", "/api/categories/" + a, null, 404, "RESOURCE_NOT_FOUND");
        assertThat(id(post("/api/categories", category("Alpha")))).isNotEqualTo(a);
        for (String action : List.of("CATEGORY_CREATED", "CATEGORY_UPDATED", "CATEGORY_DELETED")) { audited(action, a); }
        for (String action : List.of("PRODUCT_CREATED", "PRODUCT_UPDATED", "PRODUCT_DELETED")) { audited(action, p); }
    }

    private void assertPages(String path, String sort, List<String> expected) throws Exception {
        List<String> actual = new ArrayList<>();
        for (int i = 0; i < expected.size(); i++) {
            var page = get(path + (path.contains("?") ? "&" : "?") + "page=" + i + "&size=1&sort=" + sort + ",asc");
            assertThat(page.path("page").asInt()).isEqualTo(i);
            assertThat(page.path("size").asInt()).isEqualTo(1);
            assertThat(page.path("totalElements").asInt()).isEqualTo(expected.size());
            assertThat(page.path("totalPages").asInt()).isEqualTo(expected.size());
            assertThat(page.path("first").asBoolean()).isEqualTo(i == 0);
            assertThat(page.path("last").asBoolean()).isEqualTo(i == expected.size() - 1);
            actual.add(id(page.path("content").get(0)));
        }
        assertThat(actual).containsExactlyElementsOf(expected).doesNotHaveDuplicates();
    }

    @Test
    void customersNotesAndSoftDeletedEmailReuse() throws Exception {
        String a = id(post("/api/customers", customer("Alice", "ACTIVE")));
        String b = id(post("/api/customers", Map.of("name", "Bob", "email", "bob@example.com", "phone", "15559998888", "status", "BLOCKED")));
        for (String filter : List.of("name=ALI", "email=alice@", "phone=1234567", "status=ACTIVE", "name=Alice&email=alice@&phone=1234567&status=ACTIVE")) {
            matches("/api/customers?" + filter, a);
        }
        assertPages("/api/customers", "name", List.of(a, b));
        error("POST", "/api/customers", customer("Alice", "ACTIVE"), 409, "DUPLICATE_RESOURCE");
        call("PUT", "/api/customers/" + a, customer("Alice", "INACTIVE"), "MANAGER", 200);
        assertThat(get("/api/customers/" + a).path("status").asText()).isEqualTo("INACTIVE");
        String notes = "/api/customers/" + a + "/notes";
        String n = id(call("POST", notes, Map.of("note", "Follow up tomorrow."), "SUPPORT", 201));
        String n2 = id(post(notes, Map.of("note", "Second note.")));
        assertPages(notes, "createdAt", List.of(n, n2));
        assertThat(get(notes).path("content").get(0).path("note").asText()).isNotBlank();
        error("POST", notes, Map.of("note", ""), 400, "VALIDATION_ERROR");
        error("DELETE", "/api/customers/" + b + "/notes/" + n, null, 404, "RESOURCE_NOT_FOUND");
        call("DELETE", notes + "/" + n, null, "SUPPORT", 204);
        matches(notes, n2);
        matches("/api/customers/" + a + "/orders");
        call("DELETE", "/api/customers/" + a, null, "MANAGER", 204);
        error("GET", "/api/customers/" + a, null, 404, "RESOURCE_NOT_FOUND");
        matches("/api/customers", b);
        assertThat(id(post("/api/customers", customer("Alice", "ACTIVE")))).isNotEqualTo(a);
        for (String action : List.of("CUSTOMER_CREATED", "CUSTOMER_UPDATED", "CUSTOMER_DELETED")) { audited(action, a); }
    }

    @Test
    void couponLifecycleValidityFiltersAndCodeReuse() throws Exception {
        String a = id(post("/api/coupons", coupon(" alpha ", "PERCENTAGE", "ACTIVE")));
        String b = id(post("/api/coupons", coupon("BETA", "FIXED_AMOUNT", "INACTIVE")));
        var expired = new HashMap<>(coupon("OLD", "FIXED_AMOUNT", "ACTIVE"));
        expired.put("startsAt", "2000-01-01T00:00:00Z"); expired.put("endsAt", "2001-01-01T00:00:00Z");
        String old = id(post("/api/coupons", expired));
        assertThat(get("/api/coupons/" + a).path("code").asText()).isEqualTo("ALPHA");
        assertThat(get("/api/coupons/" + old).path("status").asText()).isEqualTo("EXPIRED");
        for (String filter : List.of("code=alp", "status=ACTIVE", "discountType=PERCENTAGE", "activeAt=2026-09-26T12:00:00Z",
                "code=alp&status=ACTIVE&discountType=PERCENTAGE&activeAt=2026-09-26T12:00:00Z")) {
            matches("/api/coupons?" + filter, a);
        }
        matches("/api/coupons?status=EXPIRED", old);
        assertPages("/api/coupons", "code", List.of(a, b, old));
        error("POST", "/api/coupons", coupon("ALPHA", "PERCENTAGE", "ACTIVE"), 409, "DUPLICATE_RESOURCE");
        call("PUT", "/api/coupons/" + a, coupon("ALPHA", "FIXED_AMOUNT", "ACTIVE"), "MANAGER", 200);
        assertThat(get("/api/coupons/" + a).path("discountType").asText()).isEqualTo("FIXED_AMOUNT");
        call("PATCH", "/api/coupons/" + a + "/deactivate", null, "MANAGER", 200);
        matches("/api/coupons?status=INACTIVE", a, b);
        call("PATCH", "/api/coupons/" + a + "/activate", null, "MANAGER", 200);
        assertThat(get("/api/coupons/" + a).path("status").asText()).isEqualTo("ACTIVE");
        error("PATCH", "/api/coupons/" + old + "/activate", null, 422, "BUSINESS_RULE_VIOLATION");
        assertThat(get("/api/coupons/" + old).path("status").asText()).isEqualTo("EXPIRED");
        call("DELETE", "/api/coupons/" + a, null, "MANAGER", 204);
        error("GET", "/api/coupons/" + a, null, 404, "RESOURCE_NOT_FOUND");
        matches("/api/coupons", b, old);
        assertThat(id(post("/api/coupons", coupon("ALPHA", "FIXED_AMOUNT", "ACTIVE")))).isNotEqualTo(a);
        for (String action : List.of("COUPON_CREATED", "COUPON_UPDATED", "COUPON_ACTIVATED", "COUPON_DEACTIVATED", "COUPON_DELETED")) { audited(action, a); }
    }

    private void audited(String action, String target) throws Exception {
        var rows = get("/api/audit-logs?action=" + action + "&entityPublicId=" + target).path("content");
        assertThat(rows.size()).as(action).isPositive();
        for (JsonNode row : rows) {
            assertThat(row.path("entityPublicId").asText()).isEqualTo(target);
            assertThat(row.path("action").asText()).isEqualTo(action);
            assertThat(row.path("actorUserId").asText()).isNotBlank();
        }
    }

    @Test
    void couponEligibilityIncludesStartExcludesEndAndRespectsUsageLimit() throws Exception {
        var payload = new HashMap<>(coupon("WINDOW", "PERCENTAGE", "ACTIVE"));
        payload.put("startsAt", "2026-09-26T00:00:00Z"); payload.put("endsAt", "2026-09-27T00:00:00Z");
        String coupon = id(post("/api/coupons", payload));
        matches("/api/coupons?activeAt=2026-09-26T00:00:00Z", coupon);
        matches("/api/coupons?activeAt=2026-09-25T23:59:59.999999Z");
        matches("/api/coupons?activeAt=2026-09-27T00:00:00Z");
        // Redemption has no endpoint; seed only its prerequisite counter.
        jdbc.update("update coupon set usage_count=usage_limit where public_id=?", UUID.fromString(coupon));
        matches("/api/coupons?activeAt=2026-09-26T12:00:00Z");
    }

    /** There is no order creation endpoint: only this prerequisite is inserted directly. */
    private String pendingOrder(String customer, String product, String number, Instant at) {
        UUID uuid = UUID.randomUUID();
        BigDecimal price = DemoScenario.price(0); // Shared pure scenario convention, no persistent demo dependency.
        Long order = jdbc.queryForObject("""
                insert into sales_order(public_id,customer_id,order_number,status,subtotal_amount,discount_amount,
                shipping_amount,total_amount,payment_status,delivery_status,created_at,updated_at)
                select ?,id,?,'PENDING',?,0,0,?,'PENDING','PENDING',?,? from customer where public_id=? returning id
                """, Long.class, uuid, number, price, price, Timestamp.from(at), Timestamp.from(at), UUID.fromString(customer));
        jdbc.update("""
                insert into order_item(public_id,sales_order_id,product_id,product_sku,product_name,unit_price,quantity,total_amount,created_at,updated_at)
                select ?,?,id,sku,name,?,1,?,?,? from product where public_id=?
                """, UUID.randomUUID(), order, price, price, Timestamp.from(at), Timestamp.from(at), UUID.fromString(product));
        return uuid.toString();
    }

    private void transition(String order, String status) throws Exception {
        var result = call("PATCH", "/api/orders/" + order + "/status", Map.of("status", status, "reason", "Journey operation."), "MANAGER", 200);
        assertThat(result.path("status").asText()).isEqualTo(status);
    }

    @Test
    void orderLifecycleFiltersHistoryAndHistoricalSnapshots() throws Exception {
        String c = id(post("/api/categories", category("Orders")));
        String p = id(post("/api/products", product(c, "Original", 10, 5, "ACTIVE")));
        String customer = id(post("/api/customers", customer("Buyer", "ACTIVE")));
        String other = id(post("/api/customers", customer("Other", "ACTIVE")));
        String a = pendingOrder(customer, p, "ORDER-A", NOW);
        String b = pendingOrder(other, p, "ORDER-B", NOW.minusSeconds(1));
        transition(a, "PAID");
        for (String filter : List.of("orderNumber=ER-A", "customerId=" + customer, "status=PAID", "paymentStatus=PAID",
                "createdFrom=" + NOW, "createdTo=" + NOW.minusSeconds(1))) {
            matches("/api/orders?" + filter, filter.startsWith("createdTo") ? b : a);
        }
        matches("/api/orders?deliveryStatus=PENDING", a, b);
        matches("/api/orders?orderNumber=ORDER-A&customerId=" + customer + "&status=PAID&paymentStatus=PAID&deliveryStatus=PENDING&createdFrom=" + NOW + "&createdTo=" + NOW, a);
        assertPages("/api/orders", "orderNumber", List.of(a, b));
        matches("/api/customers/" + customer + "/orders", a);
        error("PATCH", "/api/orders/" + b + "/status", Map.of("status", "SHIPPED"), 422, "BUSINESS_RULE_VIOLATION");
        assertThat(get("/api/orders/" + b).path("statusHistory").size()).isZero();
        for (String status : List.of("PROCESSING", "SHIPPED", "DELIVERED")) { transition(a, status); }
        var detail = get("/api/orders/" + a);
        assertThat(detail.path("statusHistory").size()).isEqualTo(4);
        assertThat(detail.path("paymentStatus").asText()).isEqualTo("PAID");
        assertThat(detail.path("deliveryStatus").asText()).isEqualTo("DELIVERED");
        assertThat(detail.path("totalAmount").decimalValue()).isEqualByComparingTo("10.00");
        call("PUT", "/api/products/" + p, product(c, "Renamed", 999, 5, "ACTIVE"), "ADMIN", 200);
        call("DELETE", "/api/products/" + p, null, "ADMIN", 204);
        assertThat(get("/api/orders/" + a).path("items").get(0).path("productName").asText()).isEqualTo("Original");
        assertThat(get("/api/orders/" + a).path("items").get(0).path("unitPrice").decimalValue()).isEqualByComparingTo("10.00");
        error("POST", "/api/orders/" + b + "/cancel", Map.of("reason", ""), 400, "VALIDATION_ERROR");
        call("POST", "/api/orders/" + b + "/cancel", Map.of("reason", "Customer request."), "MANAGER", 200);
        assertThat(get("/api/orders/" + b).path("cancelledAt").asText()).isNotBlank();
        error("PATCH", "/api/orders/" + b + "/status", Map.of("status", "PAID"), 422, "BUSINESS_RULE_VIOLATION");
        error("POST", "/api/orders/" + a + "/refund", Map.of("reason", ""), 400, "VALIDATION_ERROR");
        call("POST", "/api/orders/" + a + "/refund", Map.of("reason", "Approved return."), "MANAGER", 200);
        detail = get("/api/orders/" + a);
        assertThat(detail.path("refundedAt").asText()).isNotBlank();
        assertThat(detail.path("paymentStatus").asText()).isEqualTo("REFUNDED");
        assertThat(detail.path("statusHistory").size()).isEqualTo(5);
        List<String> history = new ArrayList<>();
        detail.path("statusHistory").forEach(row -> history.add(row.path("toStatus").asText()));
        assertThat(history).containsExactly("PAID", "PROCESSING", "SHIPPED", "DELIVERED", "REFUNDED");
        audited("ORDER_STATUS_CHANGED", a); audited("ORDER_REFUNDED", a); audited("ORDER_CANCELLED", b);
        error("PATCH", "/api/orders/" + a + "/status", Map.of("status", "PAID"), 422, "BUSINESS_RULE_VIOLATION");
        assertThat(get("/api/orders/" + a).path("statusHistory").size()).isEqualTo(5);
        String next = pendingOrder(customer, p, "ORDER-C", NOW);
        assertPages("/api/customers/" + customer + "/orders", "orderNumber", List.of(a, next));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "MANAGER", "SUPPORT", "CATALOG", "READ_ONLY"})
    void rolePermissionsUseRealTokensAndRejectedWritesPreserveState(String role) throws Exception {
        String c = id(post("/api/categories", category("Rolecategory")));
        String p = id(post("/api/products", product(c, "Roleproduct", 10, 10, "DRAFT")));
        String customer = id(post("/api/customers", customer("Rolecustomer", "ACTIVE")));
        String coupon = id(post("/api/coupons", coupon("ROLE", "FIXED_AMOUNT", "ACTIVE")));
        String order = pendingOrder(customer, p, "ROLE-ORDER", NOW);
        String refund = pendingOrder(customer, p, "ROLE-REFUND", NOW);
        transition(refund, "PAID");
        String cancel = pendingOrder(customer, p, "ROLE-CANCEL", NOW);
        String notes = "/api/customers/" + customer + "/notes";
        String note = id(post(notes, Map.of("note", "Role note.")));
        boolean catalogWrite = List.of("ADMIN", "CATALOG").contains(role);
        boolean catalogUpdate = List.of("ADMIN", "MANAGER", "CATALOG").contains(role);
        boolean operations = List.of("ADMIN", "MANAGER").contains(role);
        boolean support = List.of("ADMIN", "SUPPORT").contains(role);
        for (String path : List.of("/api/categories", "/api/products", "/api/customers", "/api/orders", "/api/coupons",
                "/api/categories/" + c, "/api/products/" + p, "/api/customers/" + customer,
                "/api/orders/" + order, "/api/coupons/" + coupon, "/api/customers/" + customer + "/orders")) {
            call("GET", path, null, role, 200);
        }
        permission("GET", "/api/dashboard/summary", null, role, !role.equals("CATALOG"), 200);
        permission("GET", "/api/audit-logs", null, role, operations, 200);
        String auditId = id(get("/api/audit-logs?size=1").path("content").get(0));
        permission("GET", "/api/audit-logs/" + auditId, null, role, operations, 200);
        permission("GET", notes, null, role, support, 200);
        permission("POST", notes, Map.of("note", "Additional note."), role, support, 201);
        permission("DELETE", notes + "/" + note, null, role, support, 204);
        if (!support) { matches(notes, note); }
        permission("POST", "/api/categories", category("Createdcategory"), role, catalogWrite, 201);
        permission("POST", "/api/products", product(c, "Createdproduct", 10, 10, "DRAFT"), role, catalogWrite, 201);
        permission("PUT", "/api/categories/" + c, Map.of("name", "Changed", "slug", "rolecategory", "status", "INACTIVE"), role, catalogUpdate, 200);
        permission("PUT", "/api/products/" + p, product(c, "Roleproduct", 20, 10, "DRAFT"), role, catalogUpdate, 200);
        assertThat(get("/api/products/" + p).path("price").asInt()).isEqualTo(catalogUpdate ? 20 : 10);
        assertThat(get("/api/categories/" + c).path("status").asText()).isEqualTo(catalogUpdate ? "INACTIVE" : "ACTIVE");
        permission("POST", "/api/customers", customer("Createdcustomer", "ACTIVE"), role, operations, 201);
        permission("PUT", "/api/customers/" + customer, customer("Rolecustomer", "BLOCKED"), role, operations, 200);
        assertThat(get("/api/customers/" + customer).path("status").asText()).isEqualTo(operations ? "BLOCKED" : "ACTIVE");
        permission("POST", "/api/coupons", coupon("CREATED", "FIXED_AMOUNT", "ACTIVE"), role, operations, 201);
        permission("PUT", "/api/coupons/" + coupon, coupon("ROLE", "PERCENTAGE", "ACTIVE"), role, operations, 200);
        assertThat(get("/api/coupons/" + coupon).path("discountType").asText()).isEqualTo(operations ? "PERCENTAGE" : "FIXED_AMOUNT");
        permission("PATCH", "/api/coupons/" + coupon + "/deactivate", null, role, operations, 200);
        assertThat(get("/api/coupons/" + coupon).path("status").asText()).isEqualTo(operations ? "INACTIVE" : "ACTIVE");
        permission("PATCH", "/api/coupons/" + coupon + "/activate", null, role, operations, 200);
        permission("PATCH", "/api/orders/" + order + "/status", Map.of("status", "PAID"), role, operations, 200);
        permission("POST", "/api/orders/" + cancel + "/cancel", Map.of("reason", "Role cancellation."), role, operations, 200);
        permission("POST", "/api/orders/" + refund + "/refund", Map.of("reason", "Role refund."), role, operations, 200);
        assertThat(get("/api/orders/" + order).path("status").asText()).isEqualTo(operations ? "PAID" : "PENDING");
        assertThat(get("/api/orders/" + cancel).path("status").asText()).isEqualTo(operations ? "CANCELLED" : "PENDING");
        assertThat(get("/api/orders/" + refund).path("status").asText()).isEqualTo(operations ? "REFUNDED" : "PAID");
        permission("DELETE", "/api/products/" + p, null, role, catalogWrite, 204);
        permission("DELETE", "/api/categories/" + c, null, role, catalogWrite, 204);
        permission("DELETE", "/api/customers/" + customer, null, role, operations, 204);
        permission("DELETE", "/api/coupons/" + coupon, null, role, operations, 204);
        if (!catalogWrite) { get("/api/products/" + p); get("/api/categories/" + c); }
        if (!operations) { get("/api/customers/" + customer); get("/api/coupons/" + coupon); }
    }

    private void permission(String method, String path, Object body, String role, boolean allowed, int success) throws Exception {
        JsonNode response = call(method, path, body, role, allowed ? success : 403);
        if (!allowed) { assertThat(response.path("code").asText()).isEqualTo("ACCESS_DENIED"); }
    }

    @Test
    void invalidRequestsHaveStableErrorsAndDoNotCreateBusinessRecords() throws Exception {
        for (String resource : List.of("categories", "products", "customers", "coupons")) {
            String path = "/api/" + resource;
            JsonNode error = call("POST", path, Map.of(), "ADMIN", 400);
            assertThat(error.path("code").asText()).isEqualTo("VALIDATION_ERROR");
            assertThat(error.path("fieldErrors").size()).isPositive();
            error("GET", path + "/not-a-uuid", null, 400, "VALIDATION_ERROR");
            error("GET", path + "/" + UUID.randomUUID(), null, 404, "RESOURCE_NOT_FOUND");
            matches(path);
        }
        for (String resource : List.of("products", "customers", "coupons", "orders")) {
            error("GET", "/api/" + resource + "?status=UNKNOWN", null, 400, "VALIDATION_ERROR");
        }
        error("GET", "/api/orders/" + UUID.randomUUID(), null, 404, "RESOURCE_NOT_FOUND");
        error("GET", "/api/audit-logs/" + UUID.randomUUID(), null, 404, "RESOURCE_NOT_FOUND");
        String category = id(post("/api/categories", category("Valid")));
        error("POST", "/api/products", product(UUID.randomUUID().toString(), "Orphan", 10, 1, "ACTIVE"), 404, "RESOURCE_NOT_FOUND");
        error("POST", "/api/products", product(category, "Invalid", -1, -1, "ACTIVE"), 400, "VALIDATION_ERROR");
        error("POST", "/api/products", product(category, "Invalid", 1, 1, "UNKNOWN"), 400, "VALIDATION_ERROR");
        var excessive = new HashMap<>(coupon("INVALID", "PERCENTAGE", "ACTIVE")); excessive.put("discountValue", 101);
        error("POST", "/api/coupons", excessive, 422, "BUSINESS_RULE_VIOLATION");
        matches("/api/products"); matches("/api/coupons");
    }

    @Test
    void dashboardTracksOperationsAndRetainsGlobalCountersForEmptyPeriods() throws Exception {
        var empty = get("/api/dashboard/summary");
        assertMetric(empty, "orderCount", "0"); assertMetric(empty, "averageOrderValue", "0.00");
        String category = id(post("/api/categories", category("Dashboard")));
        String product = id(post("/api/products", product(category, "Low", 10, 10, "ACTIVE")));
        String customer = id(post("/api/customers", customer("Dashboardbuyer", "ACTIVE")));
        String a = pendingOrder(customer, product, "DASH-A", NOW);
        String b = pendingOrder(customer, product, "DASH-B", NOW.minusSeconds(1));
        String c = pendingOrder(customer, product, "DASH-C", NOW.minusSeconds(2));
        var before = get("/api/dashboard/summary");
        assertMetric(before, "grossRevenue", "30.00"); assertMetric(before, "netRevenue", "30.00");
        assertMetric(before, "averageOrderValue", "10.00"); assertMetric(before, "customerCount", "1");
        assertMetric(before, "lowStockProductCount", "1"); assertMetric(before, "orderCount", "3");
        transition(a, "PAID"); transition(c, "PAID");
        call("POST", "/api/orders/" + b + "/cancel", Map.of("reason", "Dashboard cancellation."), "ADMIN", 200);
        call("POST", "/api/orders/" + c + "/refund", Map.of("reason", "Dashboard refund."), "ADMIN", 200);
        var after = get("/api/dashboard/summary");
        assertMetric(after, "grossRevenue", "20.00"); assertMetric(after, "netRevenue", "10.00");
        assertMetric(after, "cancelledOrderCount", "1"); assertMetric(after, "refundedOrderCount", "1");
        assertMetric(after, "averageOrderValue", "10.00");
        assertThat(after.path("recentOrders").get(0).path("publicId").asText()).isEqualTo(a);
        assertThat(after.path("lowStockProducts").get(0).path("publicId").asText()).isEqualTo(product);
        var none = get("/api/dashboard/summary?period=CUSTOM&from=2027-01-01T00:00:00Z&to=2027-01-02T00:00:00Z");
        assertMetric(none, "orderCount", "0"); assertMetric(none, "netRevenue", "0.00");
        assertMetric(none, "customerCount", "1"); assertMetric(none, "lowStockProductCount", "1");
        assertThat(none.path("recentOrders").size()).isZero();
        call("DELETE", "/api/products/" + product, null, "ADMIN", 204);
        call("DELETE", "/api/customers/" + customer, null, "ADMIN", 204);
        var deleted = get("/api/dashboard/summary");
        assertMetric(deleted, "customerCount", "0"); assertMetric(deleted, "lowStockProductCount", "0");
        assertMetric(deleted, "netRevenue", "10.00");
        assertThat(deleted.path("lowStockProducts").size()).isZero();
    }

    private void assertMetric(JsonNode summary, String metric, String expected) {
        assertThat(summary.path("metrics").path(metric).decimalValue()).as(metric).isEqualByComparingTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"TODAY", "LAST_7_DAYS", "LAST_30_DAYS", "THIS_MONTH", "CUSTOM"})
    void dashboardPeriodsIncludeBoundariesAndExcludeAdjacentRecords(String period) throws Exception {
        Instant from = switch (period) {
            case "TODAY" -> Instant.parse("2026-09-26T00:00:00Z");
            case "LAST_7_DAYS" -> NOW.minusSeconds(7L * 86400);
            case "THIS_MONTH" -> Instant.parse("2026-09-01T00:00:00Z");
            default -> NOW.minusSeconds(30L * 86400);
        };
        String category = id(post("/api/categories", category("Periods")));
        String product = id(post("/api/products", product(category, "Periodproduct", 10, 11, "ACTIVE")));
        String customer = id(post("/api/customers", customer("Periodbuyer", "ACTIVE")));
        String first = pendingOrder(customer, product, "FIRST", from);
        String last = pendingOrder(customer, product, "LAST", NOW);
        pendingOrder(customer, product, "BEFORE", from.minusNanos(1000));
        pendingOrder(customer, product, "AFTER", NOW.plusNanos(1000));
        String query = "?period=" + period + (period.equals("CUSTOM") ? "&from=" + from + "&to=" + NOW : "");
        var summary = get("/api/dashboard/summary" + query);
        assertMetric(summary, "orderCount", "2"); assertMetric(summary, "netRevenue", "20.00");
        assertThat(summary.path("period").path("from").asText()).isEqualTo(from.toString());
        assertThat(summary.path("period").path("to").asText()).isEqualTo(NOW.toString());
        assertThat(summary.path("recentOrders").get(0).path("publicId").asText()).isEqualTo(last);
        assertThat(summary.path("recentOrders").get(1).path("publicId").asText()).isEqualTo(first);
        if (period.equals("LAST_30_DAYS")) { assertThat(get("/api/dashboard/summary")).isEqualTo(summary); }
        matches("/api/orders?createdFrom=" + from + "&createdTo=" + NOW, first, last);
    }

    @Test
    void dashboardValidatesRangesAndLimitsSupportingLists() throws Exception {
        for (String query : List.of("period=UNKNOWN", "period=CUSTOM", "period=CUSTOM&from=2026-09-26T12:00:00Z",
                "period=CUSTOM&from=2026-09-27T00:00:00Z&to=2026-09-26T00:00:00Z")) {
            error("GET", "/api/dashboard/summary?" + query, null, 400, "VALIDATION_ERROR");
        }
        String category = id(post("/api/categories", category("Lists")));
        String customer = id(post("/api/customers", customer("Listbuyer", "ACTIVE")));
        List<String> products = new ArrayList<>();
        List<String> orders = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String product = id(post("/api/products", product(category, "Product" + i, 10, i, "ACTIVE")));
            products.add(product);
            orders.add(pendingOrder(customer, product, "LIST-" + i, NOW.minusSeconds(i)));
        }
        var summary = get("/api/dashboard/summary");
        assertMetric(summary, "orderCount", "7"); assertMetric(summary, "lowStockProductCount", "7");
        assertThat(summary.path("recentOrders").size()).isEqualTo(5);
        assertThat(summary.path("lowStockProducts").size()).isEqualTo(5);
        for (int i = 0; i < 5; i++) {
            assertThat(summary.path("recentOrders").get(i).path("publicId").asText()).isEqualTo(orders.get(i));
            assertThat(summary.path("lowStockProducts").get(i).path("publicId").asText()).isEqualTo(products.get(i));
        }
    }

    @Test
    void auditDetailRedactsSensitiveMetadataFromInternalEvents() throws Exception {
        // No API accepts arbitrary audit metadata. Prepare this event through the real recorder.
        Long actor = jdbc.queryForObject("select id from admin_user where email=?", Long.class, email("ADMIN"));
        UUID target = UUID.randomUUID();
        auditRecorder.recordAs(actor, email("ADMIN"), com.commerceops.admin.audit.model.AuditAction.ADMIN_USER_UPDATED,
                "ADMIN_USER", target, Map.of("password", "sensitive-fixture", "nested", Map.of("accessToken", "sensitive-token"), "reason", "Safe context"));
        var page = get("/api/audit-logs?entityPublicId=" + target);
        assertThat(page.path("totalElements").asInt()).isEqualTo(1);
        var detail = get("/api/audit-logs/" + id(page.path("content").get(0)));
        assertThat(detail.path("metadata").path("password").asText()).isEqualTo("[REDACTED]");
        assertThat(detail.path("metadata").path("nested").path("accessToken").asText()).isEqualTo("[REDACTED]");
        assertThat(detail.path("metadata").path("reason").asText()).isEqualTo("Safe context");
        assertThat(detail.toString().contains("sensitive-fixture")).isFalse();
        String persisted = jdbc.queryForObject("select metadata_json from audit_log where entity_public_id=?", String.class, target);
        assertThat(persisted.contains("sensitive-token")).isFalse();
        assertThat(persisted.contains("sensitive-fixture")).isFalse();
    }

    @Test
    void auditRecordsActorsTargetsFiltersAndSafeMetadata() throws Exception {
        String category = id(post("/api/categories", category("Audited")));
        String product = id(post("/api/products", product(category, "Auditedproduct", 10, 10, "ACTIVE")));
        call("PUT", "/api/products/" + product, product(category, "Auditedproduct", 20, 10, "ACTIVE"), "MANAGER", 200);
        String actor = id(call("GET", "/api/auth/me", null, "MANAGER", 200));
        var page = get("/api/audit-logs?action=PRODUCT_UPDATED");
        String audit = id(page.path("content").get(0));
        var detail = get("/api/audit-logs/" + audit);
        assertThat(detail.path("actorUserId").asText()).isEqualTo(actor);
        assertThat(detail.path("actorEmail").asText()).isEqualTo(email("MANAGER"));
        assertThat(detail.path("entityPublicId").asText()).isEqualTo(product);
        assertThat(detail.path("entityType").asText()).isEqualTo("PRODUCT");
        assertThat(detail.path("action").asText()).isEqualTo("PRODUCT_UPDATED");
        assertThat(detail.path("requestMethod").asText()).isEqualTo("PUT");
        assertThat(detail.path("requestPath").asText()).isEqualTo("/api/products/" + product);
        assertThat(detail.path("metadata").isObject()).isTrue();
        String at = detail.path("createdAt").asText();
        for (String filter : List.of("actorUserId=" + actor, "actorEmail=" + email("MANAGER"), "entityType=product", "entityPublicId=" + product,
                "createdFrom=" + at, "createdTo=" + at)) {
            var filtered = get("/api/audit-logs?" + filter + "&size=100");
            List<String> ids = new ArrayList<>(); filtered.path("content").forEach(node -> ids.add(id(node)));
            assertThat(ids).contains(audit);
            for (JsonNode node : filtered.path("content")) {
                if (filter.startsWith("actorUserId")) { assertThat(node.path("actorUserId").asText()).isEqualTo(actor); }
                if (filter.startsWith("actorEmail")) { assertThat(node.path("actorEmail").asText()).isEqualTo(email("MANAGER")); }
                if (filter.startsWith("entityType")) { assertThat(node.path("entityType").asText()).isEqualTo("PRODUCT"); }
                if (filter.startsWith("entityPublicId")) { assertThat(node.path("entityPublicId").asText()).isEqualTo(product); }
                if (filter.startsWith("createdFrom")) { assertThat(Instant.parse(node.path("createdAt").asText())).isAfterOrEqualTo(Instant.parse(at)); }
                if (filter.startsWith("createdTo")) { assertThat(Instant.parse(node.path("createdAt").asText())).isBeforeOrEqualTo(Instant.parse(at)); }
            }
        }
        matches("/api/audit-logs?actorUserId=" + actor + "&actorEmail=" + email("MANAGER")
                + "&action=PRODUCT_UPDATED&entityType=PRODUCT&entityPublicId=" + product + "&createdFrom=" + at + "&createdTo=" + at, audit);
        var productAudits = get("/api/audit-logs?entityPublicId=" + product + "&sort=createdAt,asc");
        assertPages("/api/audit-logs?entityPublicId=" + product, "createdAt",
                List.of(id(productAudits.path("content").get(0)), audit));
        var loginAudits = get("/api/audit-logs?action=AUTH_LOGIN_SUCCESS&size=100");
        for (JsonNode row : loginAudits.path("content")) {
            String serialized = get("/api/audit-logs/" + id(row)).toString();
            assertThat(serialized.contains(PASSWORD)).isFalse();
            for (String token : tokens.values()) { assertThat(serialized.contains(token)).isFalse(); }
            assertThat(serialized.contains("passwordHash")).isFalse();
        }
        error("GET", "/api/audit-logs?createdFrom=2027-01-01T00:00:00Z&createdTo=2026-01-01T00:00:00Z", null, 400, "VALIDATION_ERROR");
    }
}
