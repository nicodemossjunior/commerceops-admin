package com.commerceops.admin.demo;

import java.sql.DriverManager;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/** Entry point used only by scripts/demo.sh, never by the application context. */
public final class DemoCommand {
    private DemoCommand() { }

    public static void main(String[] args) {
        try {
            Map<String, String> env = System.getenv();
            if (!"demo".equals(env.get("SPRING_PROFILES_ACTIVE")) || args.length == 0) {
                throw new IllegalArgumentException("Select the demo profile and an explicit load, inspect, or reset command.");
            }
            String operation = args[0];
            if (!java.util.List.of("load", "inspect", "reset").contains(operation)) {
                throw new IllegalArgumentException("Expected load, inspect, or reset.");
            }
            long seed = 42;
            Instant reference = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            for (int i = 1; i < args.length; i += 2) {
                if (!operation.equals("load") || i + 1 >= args.length) { throw new IllegalArgumentException("Invalid arguments."); }
                switch (args[i]) {
                    case "--seed" -> seed = Long.parseLong(args[i + 1]);
                    case "--reference-time" -> {
                        if (!args[i + 1].endsWith("Z")) { throw new IllegalArgumentException("Reference time must use UTC Z."); }
                        reference = Instant.parse(args[i + 1]);
                        if (reference.getNano() % 1000 != 0) { throw new IllegalArgumentException("Use microsecond precision or coarser."); }
                    }
                    default -> throw new IllegalArgumentException("Unknown argument.");
                }
            }
            int port = Integer.parseInt(env.getOrDefault("DEMO_DB_PORT", "5433"));
            int threshold = Integer.parseInt(env.getOrDefault("DASHBOARD_LOW_STOCK_THRESHOLD", "10"));
            if (port < 1 || port > 65535 || threshold < 0 || threshold > Integer.MAX_VALUE - 20) {
                throw new IllegalArgumentException("Invalid port or stock threshold.");
            }
            String password = env.get("DEMO_DB_PASSWORD");
            if (password == null || password.isBlank()) { throw new IllegalArgumentException("Set DEMO_DB_PASSWORD."); }
            if (operation.equals("load") && (env.get("DEMO_ADMIN_PASSWORD") == null || env.get("DEMO_ADMIN_PASSWORD").length() < 12)) {
                throw new IllegalArgumentException("DEMO_ADMIN_PASSWORD must contain at least 12 characters.");
            }
            try (var connection = DriverManager.getConnection("jdbc:postgresql://localhost:" + port + "/commerceops_demo",
                    env.getOrDefault("DEMO_DB_USERNAME", "commerceops_demo"), password)) {
                DemoDatabase database = new DemoDatabase(connection);
                database.migrate();
                System.out.println(database.execute(operation, seed, reference, env.get("DEMO_ADMIN_PASSWORD"), threshold));
            }
        } catch (Exception exception) {
            // SQL errors can contain complete rows, including password hashes; never print nested exceptions.
            String message = exception instanceof IllegalArgumentException || exception instanceof IllegalStateException
                    ? exception.getMessage() : "Database operation failed. Check connectivity, constraints, and unrelated references; changes were rolled back.";
            System.err.println("Demo command failed: " + message);
            System.exit(1);
        }
    }
}
