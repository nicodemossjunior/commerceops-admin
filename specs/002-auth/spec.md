# Spec: Authentication

## Objective

Implement secure JWT-based authentication for CommerceOps Admin, including administrative users, roles, password hashing, token generation, token validation, and protected backend endpoints.

## Scope

- Create administrative user and role data model.
- Support login with email and password.
- Store passwords using BCrypt.
- Issue JWT access tokens after successful authentication.
- Protect API endpoints by authentication and role-based authorization.
- Return authenticated user profile information.
- Apply the global API error format for authentication and authorization failures.
- Keep all API messages and documentation in English.

## Out Of Scope

- Frontend login screen.
- Password recovery flow.
- Multi-factor authentication.
- Refresh token rotation.
- External identity providers.
- Public customer authentication.

## Roles

Initial roles:

```text
ADMIN
MANAGER
SUPPORT
CATALOG
READ_ONLY
```

Role intent:

- `ADMIN`: full system access.
- `MANAGER`: operational management access.
- `SUPPORT`: customer and order support access.
- `CATALOG`: catalog management access.
- `READ_ONLY`: read-only administrative access.

## Data Model

Tables:

```text
admin_user
role
admin_user_role
```

`admin_user` fields:

```text
id
public_id
name
email
password_hash
status
last_login_at
created_at
updated_at
deleted
deleted_at
deleted_by
```

`role` fields:

```text
id
name
description
created_at
updated_at
```

`admin_user_role` fields:

```text
admin_user_id
role_id
```

## API Contracts

```text
POST /api/auth/login
GET  /api/auth/me
```

Login request:

```json
{
  "email": "admin@example.com",
  "password": "password"
}
```

Login response:

```json
{
  "accessToken": "jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "user": {
    "publicId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Admin User",
    "email": "admin@example.com",
    "roles": ["ADMIN"]
  }
}
```

## Validation

- Email is required and must be valid.
- Password is required.
- Disabled or deleted users cannot authenticate.
- Authentication failures must not reveal whether the email exists.

## Authorization

- All administrative endpoints are protected by default.
- Public endpoints must be explicitly declared.
- Role checks must be applied at controller or method level.

## Expected Errors

- `VALIDATION_ERROR` for invalid login payloads.
- `AUTHENTICATION_FAILED` for invalid credentials.
- `ACCESS_DENIED` for missing or insufficient roles.
- `RESOURCE_NOT_FOUND` when authenticated user context references a missing user.

## Acceptance Criteria

- Admin users can authenticate with valid credentials.
- Invalid credentials return a stable error response.
- JWT tokens are signed and validated by the backend.
- Protected endpoints reject missing, invalid, or expired tokens.
- Role-based access can be enforced for domain endpoints.
- Password hashes are never exposed in API responses.

## Expected Tests

- Login success test.
- Login failure test.
- Disabled user login rejection test.
- JWT validation test.
- Protected endpoint without token test.
- Protected endpoint with insufficient role test.

## Dependencies

- `000-project-foundation`.
- `001-local-environment`.

