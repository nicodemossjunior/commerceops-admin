package com.commerceops.admin.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class AuditMetadataRedactor {

    public static final String REDACTED_VALUE = "[REDACTED]";

    private static final Set<String> SENSITIVE_TERMS = Set.of(
            "password", "passwordhash", "secret", "token", "accesstoken", "refreshtoken",
            "jwt", "authorization", "credential", "apikey"
    );

    private final ObjectMapper objectMapper;

    public AuditMetadataRedactor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String redact(Map<String, ?> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }

        JsonNode root = objectMapper.valueToTree(metadata);
        redactNode(root);
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Audit metadata could not be serialized.", exception);
        }
    }

    private void redactNode(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (isSensitive(field.getKey())) {
                    objectNode.put(field.getKey(), REDACTED_VALUE);
                } else {
                    redactNode(field.getValue());
                }
            }
        } else if (node instanceof ArrayNode arrayNode) {
            arrayNode.forEach(this::redactNode);
        }
    }

    private boolean isSensitive(String key) {
        String normalized = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return SENSITIVE_TERMS.stream().anyMatch(normalized::contains);
    }
}
