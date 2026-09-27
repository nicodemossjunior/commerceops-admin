package com.commerceops.admin.demo;

import com.commerceops.admin.orders.model.OrderStatus;
import com.commerceops.admin.orders.service.OrderStatusTransitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/** Explicit local tool; deliberately not a Spring component. All fixture writes are one transaction. */
public final class DemoDatabase {
    private static final List<String> TABLES = List.of("audit_log", "order_status_history", "order_item",
            "sales_order", "customer_note", "coupon", "product", "category", "customer", "admin_user");
    private final Connection connection;
    private final JdbcTemplate jdbc;
    private final ObjectMapper json = JsonMapper.builder().findAndAddModules().build();
    private Instant reference;
    private long seed;

    public DemoDatabase(Connection connection) throws Exception {
        this.connection = connection;
        this.jdbc = new JdbcTemplate(new SingleConnectionDataSource(connection, true));
        String url = connection.getMetaData().getURL();
        if (!url.matches("jdbc:postgresql://(localhost|127\\.0\\.0\\.1):[0-9]+/commerceops_demo(\\?.*)?")
                || !"commerceops_demo".equals(jdbc.queryForObject("select current_database()", String.class))) {
            throw new IllegalArgumentException("Demo operations require the dedicated loopback commerceops_demo database.");
        }
    }

    public void migrate() {
        Flyway.configure().dataSource(new SingleConnectionDataSource(connection, true))
                .placeholders(Map.of("activeUniquePredicate", "WHERE deleted = FALSE",
                        "customerEmailUniquePredicate", "WHERE deleted = FALSE AND email IS NOT NULL",
                        "couponCodeUniqueKeyword", "UNIQUE")).load().migrate();
    }

    public String execute(String operation, long requestedSeed, Instant requestedReference, String password,
                          int threshold) throws Exception {
        if (!List.of("load", "inspect", "reset").contains(operation) || requestedReference == null || threshold < 0) {
            throw new IllegalArgumentException("Invalid demo operation, reference timestamp, or stock threshold.");
        }
        seed = requestedSeed;
        reference = requestedReference;
        connection.setAutoCommit(false);
        try {
            jdbc.execute("select pg_advisory_xact_lock(120013)");
            jdbc.execute("create schema if not exists demo_support");
            jdbc.execute("create table if not exists demo_support.manifest (id integer primary key check(id=1), payload text not null)");
            jdbc.execute("create table if not exists demo_support.owned (table_name text not null, public_id uuid not null, primary key(table_name, public_id))");
            List<String> existing = jdbc.queryForList("select payload from demo_support.manifest where id=1", String.class);
            String result;
            if (operation.equals("reset")) {
                if (existing.isEmpty() && ownedCount() > 0) { throw new IllegalStateException("Missing scenario manifest; reset refused."); }
                if (!existing.isEmpty()) { verifyOwnership(existing.getFirst()); reset(); }
                result = "Demo dataset reset. Unrelated records were preserved.";
            } else if (!existing.isEmpty()) {
                verifyOwnership(existing.getFirst());
                var manifest = json.readTree(existing.getFirst());
                if (operation.equals("load") && (manifest.path("version").asInt() != DemoScenario.VERSION
                        || manifest.path("seed").asLong() != seed
                        || !manifest.path("referenceTime").asText().equals(reference.toString())
                        || manifest.path("lowStockThreshold").asInt() != threshold)) {
                    throw new IllegalStateException("Scenario inputs conflict. Inspect and reset the demo dataset first.");
                }
                result = existing.getFirst();
            } else if (operation.equals("inspect")) {
                if (ownedCount() > 0) { throw new IllegalStateException("Missing scenario manifest."); }
                result = "No demo dataset is loaded.";
            } else {
                if (ownedCount() > 0) { throw new IllegalStateException("Stale ownership metadata; load refused."); }
                if (password == null || password.length() < 12) {
                    throw new IllegalArgumentException("DEMO_ADMIN_PASSWORD must contain at least 12 characters.");
                }
                result = load(password, threshold);
                jdbc.update("insert into demo_support.manifest values (1, ?)", result);
            }
            connection.commit();
            return result;
        } catch (Exception exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private int ownedCount() { return jdbc.queryForObject("select count(*) from demo_support.owned", Integer.class); }

    private void verifyOwnership(String manifest) throws Exception {
        var counts = json.readTree(manifest).path("counts");
        for (String table : TABLES) {
            int expected = counts.path(table).asInt(-1);
            int owned = jdbc.queryForObject("select count(*) from demo_support.owned where table_name=?", Integer.class, table);
            int persisted = jdbc.queryForObject("select count(*) from " + table
                    + " t join demo_support.owned o on o.public_id=t.public_id where o.table_name=?", Integer.class, table);
            if (expected != owned || persisted != owned) {
                throw new IllegalStateException("Scenario ownership is stale for " + table + "; operation refused.");
            }
        }
        if (ownedCount() != counts.properties().stream().mapToInt(e -> e.getValue().asInt()).sum()) {
            throw new IllegalStateException("Unknown scenario ownership entries; operation refused.");
        }
    }

    private void reset() {
        // Audit target UUIDs and deleted_by are logical references without foreign keys.
        int externalAudit = jdbc.queryForObject("""
                select count(*) from audit_log a where
                  not exists (select 1 from demo_support.owned o where o.table_name='audit_log' and o.public_id=a.public_id)
                  and (exists (select 1 from demo_support.owned o where o.public_id=a.entity_public_id)
                    or a.actor_user_id in (select id from admin_user where public_id in
                      (select public_id from demo_support.owned where table_name='admin_user')))
                """, Integer.class);
        if (externalAudit > 0) { throw new IllegalStateException("Unrelated audit records reference demo data; reset refused."); }
        for (String table : List.of("category", "product", "customer", "customer_note", "coupon", "sales_order", "admin_user")) {
            int external = jdbc.queryForObject("select count(*) from " + table + " t where deleted_by in "
                    + "(select id from admin_user where public_id in (select public_id from demo_support.owned where table_name='admin_user')) "
                    + "and not exists (select 1 from demo_support.owned o where o.table_name=? and o.public_id=t.public_id)", Integer.class, table);
            if (external > 0) { throw new IllegalStateException("Unrelated deletion metadata references demo data; reset refused."); }
        }
        // Foreign keys reject any other unowned dependent records; the transaction rolls back all deletions.
        jdbc.update("delete from admin_user_role where admin_user_id in (select id from admin_user where public_id in "
                + "(select public_id from demo_support.owned where table_name='admin_user'))");
        for (String table : TABLES) {
            jdbc.update("delete from " + table + " where public_id in (select public_id from demo_support.owned where table_name=?)", table);
        }
        jdbc.update("delete from demo_support.owned");
        jdbc.update("delete from demo_support.manifest");
    }

    private long insert(String table, String key, Instant at, Object... fields) {
        UUID uuid = UUID.nameUUIDFromBytes(("demo-v1:" + seed + ":" + table + ":" + key).getBytes(StandardCharsets.UTF_8));
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("public_id", uuid);
        values.put("created_at", Timestamp.from(at));
        if (!List.of("audit_log", "order_status_history").contains(table)) { values.put("updated_at", Timestamp.from(at)); }
        for (int i = 0; i < fields.length; i += 2) { values.put((String) fields[i], fields[i + 1]); }
        String placeholders = String.join(",", java.util.Collections.nCopies(values.size(), "?"));
        Long id = jdbc.queryForObject("insert into " + table + " (" + String.join(",", values.keySet())
                + ") values (" + placeholders + ") returning id", Long.class, values.values().toArray());
        jdbc.update("insert into demo_support.owned values (?, ?)", table, uuid);
        return id;
    }

    private String load(String password, int threshold) throws Exception {
        Instant start = reference.minusSeconds(90L * 86400);
        long actor = insert("admin_user", "admin", start, "name", "Demo Administrator", "email", "demo.admin@example.com",
                "password_hash", new BCryptPasswordEncoder().encode(password), "status", "ACTIVE");
        jdbc.update("insert into admin_user_role select ?, id from role where name='ADMIN'", actor);
        List<Long> categories = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            categories.add(insert("category", "" + i, start, "name", "Demo Category " + i, "slug", "demo-category-" + i,
                    "status", i == 9 ? "INACTIVE" : "ACTIVE"));
        }
        List<Long> products = new ArrayList<>();
        int lowStock = 0;
        for (int i = 0; i < 100; i++) {
            int stock = switch (i % 5) { case 0 -> 0; case 1 -> Math.max(0, threshold - 1); case 2 -> threshold; default -> threshold + 20; };
            if (stock <= threshold && i != 99) { lowStock++; }
            products.add(insert("product", "" + i, start, "category_id", categories.get(i % 10), "sku", "DEMO-SKU-" + i,
                    "name", "Demo Product " + i, "slug", "demo-product-" + i, "price", DemoScenario.price(i),
                    "stock_quantity", stock, "status", new String[]{"OUT_OF_STOCK", "ACTIVE", "DRAFT", "INACTIVE", "ACTIVE"}[i % 5],
                    "deleted", i == 99, "deleted_at", i == 99 ? Timestamp.from(reference) : null, "deleted_by", i == 99 ? actor : null));
        }
        List<Long> customers = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            long customer = insert("customer", "" + i, start, "name", "Demo Customer " + i,
                    "email", "demo.customer." + i + "@example.com", "phone", "+1555000" + String.format("%04d", i),
                    "document", "DEMO-" + i, "status", new String[]{"ACTIVE", "INACTIVE", "BLOCKED"}[i % 3],
                    "deleted", i == 199, "deleted_at", i == 199 ? Timestamp.from(reference) : null, "deleted_by", i == 199 ? actor : null);
            customers.add(customer);
            if (i < 30) { insert("customer_note", "" + i, start, "customer_id", customer, "created_by", actor, "note", "Synthetic customer follow-up " + i); }
        }
        for (int i = 0; i < 20; i++) {
            insert("coupon", "" + i, start, "code", "DEMO-COUPON-" + i, "description", "Synthetic promotion",
                    "discount_type", i % 2 == 0 ? "FIXED_AMOUNT" : "PERCENTAGE", "discount_value", 10 + i,
                    "starts_at", Timestamp.from(reference.plusSeconds(i % 3 == 0 ? 86400 : -86400 * 30L)),
                    "ends_at", Timestamp.from(reference.plusSeconds(i % 3 == 1 ? -86400 : 86400 * 30L)),
                    "usage_limit", 100, "usage_count", i, "per_customer_limit", 2, "status", i % 4 == 0 ? "INACTIVE" : "ACTIVE");
        }
        var orders = DemoScenario.orders(seed, reference);
        OrderStatusTransitionService transitions = new OrderStatusTransitionService();
        for (var order : orders) {
            var path = DemoScenario.transitions(order.status());
            String payment = order.status() == OrderStatus.REFUNDED ? "REFUNDED"
                    : List.of(OrderStatus.PENDING, OrderStatus.CANCELLED).contains(order.status()) ? "PENDING" : "PAID";
            String delivery = switch (order.status()) {
                case PROCESSING -> "PREPARING"; case SHIPPED -> "SHIPPED"; case DELIVERED -> "DELIVERED";
                case CANCELLED -> "CANCELLED"; default -> "PENDING";
            };
            long id = insert("sales_order", "" + order.index(), order.createdAt(), "customer_id", customers.get(order.customer()),
                    "order_number", "DEMO-" + order.index(), "status", order.status().name(), "subtotal_amount", order.subtotal(),
                    "discount_amount", order.discount(), "shipping_amount", order.shipping(), "total_amount", order.total(),
                    "payment_status", payment, "delivery_status", delivery,
                    "cancelled_at", order.status() == OrderStatus.CANCELLED ? Timestamp.from(order.createdAt()) : null,
                    "refunded_at", order.status() == OrderStatus.REFUNDED ? Timestamp.from(order.createdAt()) : null);
            insert("order_item", "" + order.index(), order.createdAt(), "sales_order_id", id, "product_id", products.get(order.product()),
                    "product_sku", "DEMO-SKU-" + order.product(), "product_name", "Demo Product " + order.product(),
                    "unit_price", order.price(), "quantity", order.quantity(), "total_amount", order.subtotal());
            OrderStatus previous = OrderStatus.PENDING;
            for (OrderStatus status : path) {
                transitions.validate(previous, status);
                insert("order_status_history", order.index() + "-" + status, order.createdAt(), "sales_order_id", id,
                        "from_status", previous.name(), "to_status", status.name(), "changed_by", actor, "reason", "Demo lifecycle operation.");
                UUID orderUuid = jdbc.queryForObject("select public_id from sales_order where id=?", UUID.class, id);
                insert("audit_log", order.index() + "-" + status, order.createdAt(), "actor_user_id", actor,
                        "actor_email", "demo.admin@example.com", "entity_type", "ORDER", "entity_public_id", orderUuid,
                        "action", status == OrderStatus.CANCELLED ? "ORDER_CANCELLED" : status == OrderStatus.REFUNDED ? "ORDER_REFUNDED" : "ORDER_STATUS_CHANGED",
                        "metadata_json", "{\"source\":\"demo\"}");
                previous = status;
            }
        }
        // Editing a product leaves its order item snapshots intact.
        jdbc.update("update product set name='Demo Product 0 Updated' where id=?", products.getFirst());
        Map<String, Object> manifest = new LinkedHashMap<>();
        manifest.put("version", DemoScenario.VERSION);
        manifest.put("seed", seed);
        manifest.put("referenceTime", reference.toString());
        manifest.put("lowStockThreshold", threshold);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String table : TABLES) { counts.put(table, jdbc.queryForObject("select count(*) from demo_support.owned where table_name=?", Integer.class, table)); }
        manifest.put("counts", counts);
        manifest.put("records", jdbc.queryForList("select table_name, public_id from demo_support.owned order by table_name, public_id"));
        manifest.put("customerCount", 199);
        manifest.put("lowStockProductCount", lowStock);
        Instant from = reference.minusSeconds(30L * 86400);
        manifest.put("period", Map.of("from", from.toString(), "to", reference.toString()));
        manifest.put("expectedMetrics", DemoScenario.expected(orders, from, reference));
        manifest.put("emptyPeriod", Map.of("from", reference.plusSeconds(86400).toString(), "to", reference.plusSeconds(172800).toString()));
        manifest.put("examples", List.of("/api/orders?status=PAID&sort=orderNumber,asc", "/api/products?sku=DEMO-SKU-0",
                "/api/customers?email=demo.customer.0@example.com", "/api/coupons?code=DEMO-COUPON-1",
                "/api/dashboard/summary?period=CUSTOM&from=" + from + "&to=" + reference));
        return json.writerWithDefaultPrettyPrinter().writeValueAsString(manifest);
    }
}
