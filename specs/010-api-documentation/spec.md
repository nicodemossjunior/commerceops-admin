# Spec: API Documentation

## Objective

Provide clear OpenAPI documentation for CommerceOps Admin APIs, including endpoint contracts, request and response examples, authentication requirements, roles, pagination, filtering, and standard error responses.

## Scope

- Configure OpenAPI/Swagger for the backend.
- Document authentication scheme.
- Document global error response format.
- Document pagination conventions.
- Document endpoint examples for each implemented domain.
- Document role requirements for protected endpoints.
- Keep documentation in English.

## Out Of Scope

- Public marketing documentation site.
- Generated SDKs.
- External API gateway documentation.
- Frontend user guide.

## OpenAPI Requirements

- API title: `CommerceOps Admin API`.
- API description should explain that this is an administrative e-commerce operations backend.
- API version should follow project version.
- JWT Bearer authentication must be documented.
- Error responses must use the global error model.
- All domain endpoints must include summary and description.

## API Standards To Document

- Authentication: JWT Bearer token.
- ID exposure: `publicId` UUID.
- Pagination: `page`, `size`, `sort`.
- Filtering: query parameters per endpoint.
- Error format: global API error response.
- Date/time format: ISO 8601 UTC.
- Language: English.

## Example Error Documentation

Standard errors:

```text
400 VALIDATION_ERROR
401 AUTHENTICATION_FAILED
403 ACCESS_DENIED
404 RESOURCE_NOT_FOUND
409 DUPLICATE_RESOURCE
422 BUSINESS_RULE_VIOLATION
500 INTERNAL_SERVER_ERROR
```

## Acceptance Criteria

- Swagger UI is available in local development.
- OpenAPI JSON is generated.
- JWT Bearer authentication is documented.
- Standard errors are documented once and reused across endpoints where practical.
- Endpoint docs include request and response examples.
- Domain status enums are documented.
- API docs do not expose internal database IDs as public contracts.

## Expected Tests

- Application context test with OpenAPI configuration.
- Documentation generation smoke test where practical.
- Contract consistency review during each domain implementation.

## Dependencies

- `000-project-foundation`.
- Domain specs as endpoints are implemented.

