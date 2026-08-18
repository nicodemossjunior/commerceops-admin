# Tasks: Local Environment

## Docker

- [x] Create `docker-compose.yml` with PostgreSQL.
- [x] Define database name, user, password, and port through environment variables.
- [x] Add persistent volume for PostgreSQL local data.
- [x] Add healthcheck for PostgreSQL.

## Backend Configuration

- [x] Configure local datasource URL.
- [x] Configure local datasource credentials through environment variables.
- [x] Configure Hibernate validation mode without auto-generating schema in persistent environments.
- [x] Configure Flyway migration location.
- [x] Configure test profile for migration validation. Testcontainers repository wiring will be added when repository tests are introduced.

## Database And Flyway

- [x] Create initial migration folder under `src/main/resources/db/migration`.
- [x] Add bootstrap migration for Flyway history. Domain table migrations remain deferred to their owning specs.
- [x] Document migration naming conventions.

## Tests

- [x] Add Flyway validation coverage when migrations exist.
- [x] Defer Testcontainers base configuration until repository tests are introduced.

## Documentation

- [x] Document local startup commands.
- [x] Document required environment variables.
- [x] Document reset instructions for local database.

## Validation

- [x] Start PostgreSQL locally.
- [x] Start the backend with the local profile after the backend exists.
- [x] Confirm Flyway runs without errors.
