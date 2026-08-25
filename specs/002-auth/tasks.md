# Tasks: Authentication

## Backend

- [x] Create `AdminUser` entity.
- [x] Create `Role` entity.
- [x] Create user-role relationship mapping.
- [x] Create authentication DTOs.
- [x] Create login service.
- [x] Configure BCrypt password encoder.
- [x] Create JWT generation component.
- [x] Create JWT validation filter.
- [x] Configure Spring Security filter chain.
- [x] Add authenticated user context helper.
- [x] Add role-based authorization support.
- [x] Add `/api/auth/login`.
- [x] Add `/api/auth/me`.

## Database And Flyway

- [x] Create `admin_user` table.
- [x] Create `role` table.
- [x] Create `admin_user_role` table.
- [x] Add unique constraint for `admin_user.email`.
- [x] Add unique constraint for `admin_user.public_id`.
- [x] Add unique constraint for `role.name`.
- [x] Seed initial roles.

## Tests

- [x] Test successful login.
- [x] Test invalid credentials.
- [x] Test disabled user cannot log in.
- [x] Test deleted user cannot log in.
- [x] Test endpoint without token returns unauthorized.
- [x] Test endpoint with insufficient role returns forbidden.

## Documentation

- [x] Document authentication endpoints in OpenAPI.
- [x] Document role meanings.
- [x] Document required JWT environment variables.

## Validation

- [x] Run unit tests.
- [x] Run security tests.
- [x] Run migration validation.
