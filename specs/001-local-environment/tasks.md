# Tasks: Local Environment

## Docker

- [ ] Create `docker-compose.yml` with PostgreSQL.
- [ ] Define database name, user, password, and port through environment variables.
- [ ] Add persistent volume for PostgreSQL local data.
- [ ] Add healthcheck for PostgreSQL.

## Backend Configuration

- [ ] Configure local datasource URL.
- [ ] Configure local datasource credentials through environment variables.
- [ ] Configure Hibernate validation mode without auto-generating schema in persistent environments.
- [ ] Configure Flyway migration location.
- [ ] Configure test profile for Testcontainers usage.

## Database And Flyway

- [ ] Create initial migration folder under `src/main/resources/db/migration`.
- [ ] Add first migration when the first persisted feature requires tables.
- [ ] Document migration naming conventions.

## Tests

- [ ] Add Flyway validation coverage when migrations exist.
- [ ] Add Testcontainers base configuration when repository tests are introduced.

## Documentation

- [ ] Document local startup commands.
- [ ] Document required environment variables.
- [ ] Document reset instructions for local database.

## Validation

- [ ] Start PostgreSQL locally.
- [ ] Start the backend with the local profile after the backend exists.
- [ ] Confirm Flyway runs without errors.

