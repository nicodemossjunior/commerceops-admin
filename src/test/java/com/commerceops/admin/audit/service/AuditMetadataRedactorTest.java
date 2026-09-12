package com.commerceops.admin.audit.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AuditMetadataRedactorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuditMetadataRedactor redactor = new AuditMetadataRedactor(objectMapper);

    @Test
    void redactsSensitiveValuesAtEveryDepth() throws Exception {
        String json = redactor.redact(Map.of(
                "email", "admin@example.com",
                "password", "plain-secret",
                "nested", Map.of("access_token", "jwt-value"),
                "items", List.of(Map.of("apiKey", "key-value"))
        ));

        var result = objectMapper.readTree(json);
        assertThat(result.get("email").asText()).isEqualTo("admin@example.com");
        assertThat(result.get("password").asText()).isEqualTo(AuditMetadataRedactor.REDACTED_VALUE);
        assertThat(result.at("/nested/access_token").asText()).isEqualTo(AuditMetadataRedactor.REDACTED_VALUE);
        assertThat(result.at("/items/0/apiKey").asText()).isEqualTo(AuditMetadataRedactor.REDACTED_VALUE);
        assertThat(json).doesNotContain("plain-secret", "jwt-value", "key-value");
    }

    @Test
    void omitsEmptyMetadata() {
        assertThat(redactor.redact(null)).isNull();
        assertThat(redactor.redact(Map.of())).isNull();
    }
}
