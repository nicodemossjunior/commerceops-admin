package com.commerceops.admin.common.error;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiErrorResponseTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Test
    void serializesStandardErrorShape() throws Exception {
        ApiErrorResponse response = new ApiErrorResponse(
                java.time.Instant.parse("2026-08-12T16:21:00Z"),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ApiErrorCode.VALIDATION_ERROR,
                "Request validation failed.",
                "/api/products",
                "8f3a1c0e4c9b4b2a",
                List.of(new FieldErrorResponse("name", "Product name is required."))
        );

        JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

        assertThat(json.get("timestamp").asText()).isEqualTo("2026-08-12T16:21:00Z");
        assertThat(json.get("status").asInt()).isEqualTo(400);
        assertThat(json.get("error").asText()).isEqualTo("Bad Request");
        assertThat(json.get("code").asText()).isEqualTo("VALIDATION_ERROR");
        assertThat(json.get("message").asText()).isEqualTo("Request validation failed.");
        assertThat(json.get("path").asText()).isEqualTo("/api/products");
        assertThat(json.get("traceId").asText()).isEqualTo("8f3a1c0e4c9b4b2a");
        assertThat(json.get("fieldErrors")).hasSize(1);
        assertThat(json.get("fieldErrors").get(0).get("field").asText()).isEqualTo("name");
        assertThat(json.get("fieldErrors").get(0).get("message").asText()).isEqualTo("Product name is required.");
    }
}
