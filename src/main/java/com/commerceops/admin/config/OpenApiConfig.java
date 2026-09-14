package com.commerceops.admin.config;

import com.commerceops.admin.common.error.ApiErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
    private static final String ERROR_SCHEMA_REF = "#/components/schemas/ApiErrorResponse";

    @Bean
    OpenAPI commerceOpsOpenApi(BuildProperties buildProperties) {
        Components components = new Components()
                .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("JWT access token returned by POST /api/auth/login."));

        ModelConverters.getInstance().read(ApiErrorResponse.class).forEach(components::addSchemas);
        standardErrors().forEach((name, definition) -> components.addResponses(
                name,
                errorResponse(definition.status(), definition.description(), definition.code(), definition.message())
        ));

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("CommerceOps Admin API")
                        .description("Administrative e-commerce operations backend for catalog, customers, orders, "
                                + "coupons, dashboards, and immutable audit records. Resource identifiers exposed by "
                                + "the API are public UUIDs, and all timestamps use ISO 8601 UTC.")
                        .version(buildProperties.getVersion()))
                .tags(List.of(
                        new Tag().name("Authentication").description("Administrative authentication and current-user identity"),
                        new Tag().name("Categories").description("Catalog category and subcategory management"),
                        new Tag().name("Products").description("Catalog product management and search"),
                        new Tag().name("Customers").description("Customer records, search, and purchase history"),
                        new Tag().name("Customer Notes").description("Restricted internal notes for customer support"),
                        new Tag().name("Orders").description("Order lookup, fulfillment status, cancellation, and refunds"),
                        new Tag().name("Coupons").description("Promotional coupon configuration and lifecycle management"),
                        new Tag().name("Dashboard").description("Operational sales, customer, order, and stock indicators"),
                        new Tag().name("Audit Logs").description("Immutable sensitive-action and domain-change records")
                ));
    }

    @Bean
    OpenApiCustomizer apiStandardsCustomizer() {
        return openApi -> openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperations().forEach(operation -> {
            if (path.startsWith("/api/")) {
                addStandardErrorReferences(operation.getResponses(), path);
            }
            if (operation.getParameters() != null) {
                operation.getParameters().forEach(this::documentPaginationParameter);
            }
        }));
    }

    private void addStandardErrorReferences(io.swagger.v3.oas.models.responses.ApiResponses responses, String path) {
        responses.addApiResponse("400", responseReference("ValidationError"));
        responses.addApiResponse("401", responseReference("AuthenticationFailed"));
        responses.addApiResponse("403", responseReference("AccessDenied"));
        if (path.contains("{")) {
            responses.addApiResponse("404", responseReference("ResourceNotFound"));
        }
        responses.addApiResponse("500", responseReference("InternalServerError"));
    }

    private void documentPaginationParameter(Parameter parameter) {
        if ("page".equals(parameter.getName())) {
            parameter.setDescription("Zero-based page index.");
            parameter.setExample(0);
        } else if ("size".equals(parameter.getName())) {
            parameter.setDescription("Number of resources per page.");
            parameter.setExample(20);
        } else if ("sort".equals(parameter.getName())) {
            parameter.setDescription("Sort expression as property,(asc|desc). May be repeated.");
            parameter.setExample(List.of("createdAt,desc"));
        }
    }

    private ApiResponse responseReference(String componentName) {
        return new ApiResponse().$ref("#/components/responses/" + componentName);
    }

    private ApiResponse errorResponse(int status, String description, String code, String message) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("timestamp", "2026-08-12T16:21:00Z");
        value.put("status", status);
        value.put("error", description);
        value.put("code", code);
        value.put("message", message);
        value.put("path", "/api/resources");
        value.put("traceId", "8f3a1c0e4c9b4b2a");
        value.put("fieldErrors", List.of());

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType()
                        .schema(new Schema<>().$ref(ERROR_SCHEMA_REF))
                        .addExamples("default", new Example().summary(description).value(value))));
    }

    private Map<String, ErrorDefinition> standardErrors() {
        return Map.of(
                "ValidationError", new ErrorDefinition(400, "Bad Request", "VALIDATION_ERROR", "Request validation failed."),
                "AuthenticationFailed", new ErrorDefinition(401, "Unauthorized", "AUTHENTICATION_FAILED", "Authentication is required."),
                "AccessDenied", new ErrorDefinition(403, "Forbidden", "ACCESS_DENIED", "Access is denied."),
                "ResourceNotFound", new ErrorDefinition(404, "Not Found", "RESOURCE_NOT_FOUND", "Resource was not found."),
                "DuplicateResource", new ErrorDefinition(409, "Conflict", "DUPLICATE_RESOURCE", "Resource already exists."),
                "BusinessRuleViolation", new ErrorDefinition(422, "Unprocessable Entity", "BUSINESS_RULE_VIOLATION", "A business rule was violated."),
                "InternalServerError", new ErrorDefinition(500, "Internal Server Error", "INTERNAL_SERVER_ERROR", "An unexpected error occurred.")
        );
    }

    private record ErrorDefinition(int status, String description, String code, String message) {
    }
}
