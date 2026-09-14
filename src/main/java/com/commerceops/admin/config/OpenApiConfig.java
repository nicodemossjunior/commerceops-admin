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
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Operation;
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
        return openApi -> {
            addSchemaExamples(openApi);
            openApi.getPaths().forEach((path, pathItem) -> pathItem.readOperationsMap().forEach((method, operation) -> {
                if (path.startsWith("/api/")) {
                    addStandardErrorReferences(operation.getResponses(), path, method);
                    documentAccess(operation, path, method);
                }
                if (operation.getParameters() != null) {
                    operation.getParameters().forEach(this::documentPaginationParameter);
                }
            }));
        };
    }

    private void addStandardErrorReferences(
            io.swagger.v3.oas.models.responses.ApiResponses responses,
            String path,
            PathItem.HttpMethod method
    ) {
        responses.addApiResponse("400", responseReference("ValidationError"));
        responses.addApiResponse("401", responseReference("AuthenticationFailed"));
        responses.addApiResponse("403", responseReference("AccessDenied"));
        if (path.contains("{")) {
            responses.addApiResponse("404", responseReference("ResourceNotFound"));
        }
        if (method == PathItem.HttpMethod.POST || method == PathItem.HttpMethod.PUT) {
            responses.addApiResponse("409", responseReference("DuplicateResource"));
        }
        if (method != PathItem.HttpMethod.GET && !path.equals("/api/auth/login")) {
            responses.addApiResponse("422", responseReference("BusinessRuleViolation"));
        }
        responses.addApiResponse("500", responseReference("InternalServerError"));
    }

    private void documentAccess(Operation operation, String path, PathItem.HttpMethod method) {
        List<String> roles = requiredRoles(path, method);
        String access = roles.isEmpty() ? "Public endpoint." : "Required roles: " + String.join(", ", roles) + ".";
        String description = operation.getDescription();
        if (description == null || description.isBlank()) {
            description = operation.getSummary() + ". Path resource identifiers are public UUIDs.";
        }
        if (!description.contains("Required roles:") && !description.contains("Public endpoint.")) {
            operation.setDescription(description + "\n\n" + access);
        }
        operation.addExtension("x-required-roles", roles.isEmpty() ? List.of("PUBLIC") : roles);
    }

    private List<String> requiredRoles(String path, PathItem.HttpMethod method) {
        if (path.equals("/api/auth/login")) {
            return List.of();
        }
        if (path.equals("/api/auth/me")) {
            return List.of("AUTHENTICATED");
        }
        if (path.startsWith("/api/audit-logs")) {
            return List.of("ADMIN", "MANAGER");
        }
        if (path.startsWith("/api/dashboard")) {
            return List.of("ADMIN", "MANAGER", "READ_ONLY", "SUPPORT");
        }
        if (path.contains("/notes")) {
            return List.of("ADMIN", "SUPPORT");
        }
        if (path.startsWith("/api/categories") || path.startsWith("/api/products")) {
            if (method == PathItem.HttpMethod.GET) {
                return List.of("ADMIN", "MANAGER", "CATALOG", "READ_ONLY", "SUPPORT");
            }
            if (method == PathItem.HttpMethod.PUT) {
                return List.of("ADMIN", "MANAGER", "CATALOG");
            }
            return List.of("ADMIN", "CATALOG");
        }
        if (path.startsWith("/api/customers")) {
            if (method == PathItem.HttpMethod.GET) {
                return List.of("ADMIN", "MANAGER", "SUPPORT", "READ_ONLY", "CATALOG");
            }
            return List.of("ADMIN", "MANAGER");
        }
        if (path.startsWith("/api/orders")) {
            if (method == PathItem.HttpMethod.GET) {
                return List.of("ADMIN", "MANAGER", "SUPPORT", "READ_ONLY", "CATALOG");
            }
            return List.of("ADMIN", "MANAGER");
        }
        if (path.startsWith("/api/coupons")) {
            if (method == PathItem.HttpMethod.GET) {
                return List.of("ADMIN", "MANAGER", "SUPPORT", "READ_ONLY", "CATALOG");
            }
            return List.of("ADMIN", "MANAGER");
        }
        return List.of("AUTHENTICATED");
    }

    private void addSchemaExamples(OpenAPI openApi) {
        Map<String, Object> examples = schemaExamples();
        examples.forEach((name, example) -> {
            Schema<?> schema = openApi.getComponents().getSchemas().get(name);
            if (schema != null) {
                schema.setExample(example);
            }
        });

        openApi.getComponents().getSchemas().forEach((name, schema) -> {
            if (!name.startsWith("PageResponse") || schema.getProperties() == null) {
                return;
            }
            Schema<?> content = (Schema<?>) schema.getProperties().get("content");
            if (content == null || content.getItems() == null || content.getItems().get$ref() == null) {
                return;
            }
            String itemName = content.getItems().get$ref().substring(content.getItems().get$ref().lastIndexOf('/') + 1);
            Object itemExample = examples.get(itemName);
            schema.setExample(pageExample(itemExample));
        });
    }

    private Map<String, Object> schemaExamples() {
        Map<String, Object> examples = new LinkedHashMap<>();
        examples.put("LoginRequest", Map.of("email", "admin@commerceops.example", "password", "strong-password"));
        examples.put("LoginResponse", Map.of(
                "accessToken", "eyJhbGciOiJIUzI1NiJ9.example.signature",
                "tokenType", "Bearer",
                "expiresIn", 3600,
                "user", Map.of(
                        "publicId", "3cfd9bb8-6688-4fd6-a6ea-317cd8582cc0",
                        "name", "Operations Admin",
                        "email", "admin@commerceops.example",
                        "roles", List.of("ADMIN")
                )
        ));
        examples.put("AuthUserResponse", ((Map<?, ?>) ((Map<?, ?>) examples.get("LoginResponse")).get("user")));
        examples.put("CategoryRequest", Map.of(
                "name", "Electronics", "slug", "electronics", "description", "Electronic products", "status", "ACTIVE"
        ));
        examples.put("CategoryResponse", Map.of(
                "publicId", "13cf61ce-a9fd-4cb2-9636-df72dd4c6788", "name", "Electronics",
                "slug", "electronics", "description", "Electronic products", "status", "ACTIVE",
                "createdAt", "2026-08-12T16:21:00Z", "updatedAt", "2026-08-12T16:21:00Z"
        ));
        examples.put("ProductRequest", Map.of(
                "categoryPublicId", "13cf61ce-a9fd-4cb2-9636-df72dd4c6788", "sku", "KB-100",
                "name", "Mechanical Keyboard", "slug", "mechanical-keyboard", "description", "Hot-swappable keyboard",
                "price", 129.90, "imageUrl", "https://cdn.example.com/products/keyboard.jpg", "stockQuantity", 25, "status", "ACTIVE"
        ));
        examples.put("ProductResponse", resourceExample(
                "6d6772ff-99cc-4859-8783-e21b404ade8e", "sku", "KB-100", "name", "Mechanical Keyboard", "status", "ACTIVE"
        ));
        examples.put("CustomerRequest", Map.of(
                "name", "Taylor Morgan", "email", "taylor@example.com", "phone", "+1 202-555-0147",
                "document", "DOC-8821", "status", "ACTIVE"
        ));
        examples.put("CustomerResponse", resourceExample(
                "4db0a459-ad31-43bd-bd5e-12e2a2da88d1", "name", "Taylor Morgan", "email", "taylor@example.com", "status", "ACTIVE"
        ));
        examples.put("CustomerNoteRequest", Map.of("note", "Customer requested priority support follow-up."));
        examples.put("CustomerNoteResponse", resourceExample(
                "25fcd330-f2d3-4f01-9c74-ec6bed1755ea", "note", "Customer requested priority support follow-up."
        ));
        examples.put("OrderStatusUpdateRequest", Map.of("status", "PROCESSING", "reason", "Payment confirmed."));
        examples.put("OrderCancelRequest", Map.of("reason", "Customer requested cancellation."));
        examples.put("OrderRefundRequest", Map.of("reason", "Approved return received."));
        examples.put("OrderSummaryResponse", resourceExample(
                "f78b41e0-8c17-4e17-a50c-b5348097f436", "orderNumber", "ORD-2026-0042", "status", "PAID", "totalAmount", 149.90
        ));
        examples.put("OrderDetailResponse", examples.get("OrderSummaryResponse"));
        examples.put("CustomerOrderSummaryResponse", examples.get("OrderSummaryResponse"));
        examples.put("CouponRequest", Map.of(
                "code", "WELCOME10", "description", "Welcome discount", "discountType", "PERCENTAGE",
                "discountValue", 10, "startsAt", "2026-08-01T00:00:00Z", "endsAt", "2026-12-31T23:59:59Z",
                "usageLimit", 1000, "perCustomerLimit", 1, "status", "ACTIVE"
        ));
        examples.put("CouponResponse", resourceExample(
                "a24de9e9-8bdb-4b17-a928-c99b05fb76cd", "code", "WELCOME10", "discountType", "PERCENTAGE", "discountValue", 10, "status", "ACTIVE"
        ));
        examples.put("DashboardSummaryResponse", Map.of(
                "period", Map.of("from", "2026-07-14T00:00:00Z", "to", "2026-08-12T23:59:59Z"),
                "metrics", Map.of("grossRevenue", 12500.00, "netRevenue", 11800.00, "orderCount", 84, "averageOrderValue", 147.50,
                        "customerCount", 620, "cancelledOrderCount", 3, "refundedOrderCount", 2, "lowStockProductCount", 7),
                "recentOrders", List.of(), "lowStockProducts", List.of()
        ));
        examples.put("AuditLogSummaryResponse", resourceExample(
                "69cbb9a4-37d3-4059-bb1f-aa6804ea43b1", "actorEmail", "admin@commerceops.example",
                "action", "PRODUCT_UPDATED", "entityType", "PRODUCT", "entityPublicId", "6d6772ff-99cc-4859-8783-e21b404ade8e"
        ));
        examples.put("AuditLogDetailResponse", examples.get("AuditLogSummaryResponse"));
        return examples;
    }

    private Map<String, Object> resourceExample(String publicId, Object... properties) {
        Map<String, Object> example = new LinkedHashMap<>();
        example.put("publicId", publicId);
        for (int index = 0; index < properties.length; index += 2) {
            example.put(properties[index].toString(), properties[index + 1]);
        }
        example.put("createdAt", "2026-08-12T16:21:00Z");
        example.put("updatedAt", "2026-08-12T16:21:00Z");
        return example;
    }

    private Map<String, Object> pageExample(Object itemExample) {
        return Map.of(
                "content", itemExample == null ? List.of() : List.of(itemExample),
                "page", 0,
                "size", 20,
                "totalElements", itemExample == null ? 0 : 1,
                "totalPages", itemExample == null ? 0 : 1,
                "first", true,
                "last", true
        );
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
