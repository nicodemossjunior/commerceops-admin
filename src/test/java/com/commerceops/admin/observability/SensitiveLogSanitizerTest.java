package com.commerceops.admin.observability;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SensitiveLogSanitizerTest {

    private final SensitiveLogSanitizer sanitizer = new SensitiveLogSanitizer();

    @Test
    void redactsCredentialsFromStructuredAndPlainTextValues() {
        String sanitized = sanitizer.sanitize("""
                {"email":"admin@example.com","password":"plain-secret","access_token":"jwt-value"}
                authorization=Bearer eyJhbGciOiJIUzI1NiJ9.payload.signature apiKey=private-key
                """);

        assertThat(sanitized)
                .contains("admin@example.com", SensitiveLogSanitizer.REDACTED_VALUE)
                .doesNotContain("plain-secret", "jwt-value", "eyJhbGciOiJIUzI1NiJ9", "private-key");
    }

    @Test
    void preservesSafeMessagesAndNulls() {
        assertThat(sanitizer.sanitize("Order status changed to PAID."))
                .isEqualTo("Order status changed to PAID.");
        assertThat(sanitizer.sanitize(null)).isNull();
    }
}
