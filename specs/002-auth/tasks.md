# Tasks: Authentication

## Backend

- [ ] Create `AdminUser` entity.
- [ ] Create `Role` entity.
- [ ] Create user-role relationship mapping.
- [ ] Create authentication DTOs.
- [ ] Create login service.
- [ ] Configure BCrypt password encoder.
- [ ] Create JWT generation component.
- [ ] Create JWT validation filter.
- [ ] Configure Spring Security filter chain.
- [ ] Add authenticated user context helper.
- [ ] Add role-based authorization support.
- [ ] Add `/api/auth/login`.
- [ ] Add `/api/auth/me`.

## Database And Flyway

- [ ] Create `admin_user` table.
- [ ] Create `role` table.
- [ ] Create `admin_user_role` table.
- [ ] Add unique constraint for `admin_user.email`.
- [ ] Add unique constraint for `admin_user.public_id`.
- [ ] Add unique constraint for `role.name`.
- [ ] Seed initial roles.

## Tests

- [ ] Test successful login.
- [ ] Test invalid credentials.
- [ ] Test disabled user cannot log in.
- [ ] Test deleted user cannot log in.
- [ ] Test endpoint without token returns unauthorized.
- [ ] Test endpoint with insufficient role returns forbidden.

## Documentation

- [ ] Document authentication endpoints in OpenAPI.
- [ ] Document role meanings.
- [ ] Document required JWT environment variables.

## Validation

- [ ] Run unit tests.
- [ ] Run security tests.
- [ ] Run migration validation.

