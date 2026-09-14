package com.commerceops.admin.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"local", "test"})
class ApiDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatesProjectMetadataTagsAndExamples() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("CommerceOps Admin API"))
                .andExpect(jsonPath("$.info.version").value("0.0.1-SNAPSHOT"))
                .andExpect(jsonPath("$.info.description", containsString("Administrative")))
                .andExpect(jsonPath("$.tags[*].name", hasItems(
                        "Authentication", "Categories", "Products", "Customers", "Orders",
                        "Coupons", "Dashboard", "Audit Logs"
                )))
                .andExpect(jsonPath("$.components.schemas.LoginRequest.example.email")
                        .value("admin@commerceops.example"))
                .andExpect(jsonPath("$.components.schemas.ProductRequest.example.name")
                        .value("Mechanical Keyboard"))
                .andExpect(jsonPath("$.components.schemas.CustomerRequest.example.name")
                        .value("Taylor Morgan"))
                .andExpect(jsonPath("$.components.schemas.OrderStatusUpdateRequest.example.reason")
                        .value("Payment confirmed."))
                .andExpect(jsonPath("$.components.schemas.CouponRequest.example.description")
                        .value("Welcome discount"))
                .andExpect(jsonPath("$.components.schemas.DashboardSummaryResponse.example.metrics.orderCount")
                        .value(84))
                .andExpect(jsonPath("$.components.schemas.AuditLogSummaryResponse.example.action")
                        .value("PRODUCT_UPDATED"));
    }

    @Test
    void exposesSwaggerUiOnlyWithLocalConfigurationEnabled() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));

        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
    }

    @Test
    void documentsJwtSecurityAndReusableStandardErrors() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.bearerFormat").value("JWT"))
                .andExpect(jsonPath("$.components.schemas.ApiErrorResponse.properties.traceId").exists())
                .andExpect(jsonPath("$.components.responses.ValidationError.content.application/json.examples.default.value.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.components.responses.AuthenticationFailed").exists())
                .andExpect(jsonPath("$.components.responses.AccessDenied").exists())
                .andExpect(jsonPath("$.components.responses.ResourceNotFound").exists())
                .andExpect(jsonPath("$.components.responses.DuplicateResource").exists())
                .andExpect(jsonPath("$.components.responses.BusinessRuleViolation").exists())
                .andExpect(jsonPath("$.components.responses.InternalServerError").exists());
    }

    @Test
    void keepsEveryBusinessOperationDiscoverableAndFreeOfInternalIds() throws Exception {
        String document = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode root = objectMapper.readTree(document);

        root.path("paths").properties().stream()
                .filter(entry -> entry.getKey().startsWith("/api/"))
                .forEach(path -> path.getValue().properties().forEach(operation -> {
                    if (!isHttpMethod(operation.getKey())) {
                        return;
                    }
                    assertThat(operation.getValue().path("summary").asText()).isNotBlank();
                    assertThat(operation.getValue().path("description").asText()).isNotBlank();
                    assertThat(operation.getValue().path("x-required-roles").isArray()).isTrue();
                    assertThat(operation.getValue().path("responses").path("400").path("$ref").asText())
                            .isEqualTo("#/components/responses/ValidationError");
                }));

        Iterator<Map.Entry<String, JsonNode>> schemas = root.path("components").path("schemas").fields();
        while (schemas.hasNext()) {
            Map.Entry<String, JsonNode> schema = schemas.next();
            assertThat(schema.getValue().path("properties").has("id"))
                    .as("schema %s must not expose an internal id", schema.getKey())
                    .isFalse();
        }

        JsonNode productParameters = root.path("paths").path("/api/products").path("get").path("parameters");
        assertParameterExample(productParameters, "page", "0");
        assertParameterExample(productParameters, "size", "20");
        assertParameterExample(productParameters, "sort", "createdAt,desc");
    }

    private void assertParameterExample(JsonNode parameters, String name, String expectedValue) {
        JsonNode parameter = parameters.valueStream()
                .filter(candidate -> name.equals(candidate.path("name").asText()))
                .findFirst()
                .orElseThrow();
        assertThat(parameter.path("description").asText()).isNotBlank();
        assertThat(parameter.path("example").toString()).contains(expectedValue);
    }

    private boolean isHttpMethod(String candidate) {
        return switch (candidate) {
            case "get", "post", "put", "patch", "delete" -> true;
            default -> false;
        };
    }
}
