# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Development mode (hot reload)
mvn quarkus:dev

# Build and run all tests
mvn clean package

# Run all tests without building JAR
mvn test

# Run a single test class
mvn test -Dtest=ArchitectureTest

# Run a single test method
mvn test -Dtest=ArchitectureTest#entitiesInDbPackageShouldHaveEntitySuffix
```

## New Hexagonal Backend

The active development goal is to implement a **new backend** in the `com.gepardec.mega.hexagon` package that strictly follows DDD + Hexagonal (Ports & Adapters) design principles — separate from the legacy `com.gepardec.mega` code.

When working on anything in `com.gepardec.mega.hexagon`, always invoke the **`clean-ddd-hexagonal`** skill first for design guidance, pattern references, and layer conventions.

## Architecture (Legacy)

This is a **Quarkus 3** microservice (JDK 21) following a **layered architecture** with DDD-inspired domain isolation. The main package is `com.gepardec.mega`.

### Layer structure (strict dependency direction: rest → service → db, domain is isolated)

| Package | Role |
|---|---|
| `domain/` | Pure domain models and calculations — **no dependencies on other application packages** (enforced by ArchUnit) |
| `db/` | JPA entities (Hibernate Panache), repositories, entity↔domain mappers |
| `service/` | Business logic — interfaces in `api/`, implementations in `impl/` |
| `rest/` | REST resources — interfaces in `api/`, implementations in `impl/`, DTOs in `model/` |
| `application/` | Cross-cutting: config, CDI producers, interceptors, health checks, scheduling |
| `zep/` | ZEP timesheet system integration (SOAP/REST, includes WSDL-generated classes) |
| `personio/` | Personio HR system integration |
| `notification/` | Email notifications via Gmail API/SMTP |

### Key architectural rules (enforced by `ArchitectureTest.java`)
1. **Domain isolation** — `domain..` must not depend on any other application package
2. **No REST→Repository** — REST classes must use the service layer, not repositories directly
3. **Entity naming** — all JPA entities in `db/` must have the `Entity` suffix

### Persistence
- Hibernate ORM with **Quarkus Panache** — repositories extend `PanacheRepository<T>`
- PostgreSQL in production/dev; H2 (PostgreSQL mode) in tests
- Schema managed via **Liquibase** (YAML changelogs in `src/main/resources/db/`)
- Services are `@ApplicationScoped` + `@Transactional`

### Testing
- `@QuarkusTest` for integration tests (CDI container, real DB via H2)
- Mockito (`@InjectMock`, `@InjectSpy`) for unit tests
- REST-Assured for HTTP endpoint tests
- Test profile (`%test`) uses H2, disables scheduler, mocks mailer

### External integrations
- **ZEP** — timesheet system via SOAP (CXF-generated) and REST clients
- **Personio** — HR system via REST (OAuth2)
- **Google Workspace** — Gmail API for mail receive/send, OAuth2 credentials via CDI producer
- **Keycloak/OIDC** — authentication (`application/configuration/` holds all OAuth config)

### Caching
Caffeine cache configured for employees, projects, and project entries (see `application.yaml`).

## Logging

Use `org.jboss.logging.Logger` (Quarkus standard). Log at appropriate levels:
- `INFO` — significant lifecycle events (service startup, scheduled job execution, external system calls)
- `WARN` — recoverable issues, unexpected-but-handled states
- `ERROR` — failures that impact functionality (always include the exception)
- `DEBUG` — internal flow details useful for troubleshooting (not logged in production by default)

Guidelines:
- Log at service boundaries: incoming REST requests are handled by Quarkus automatically; log outgoing calls to ZEP, Personio, Gmail, etc.
- Log when scheduled jobs start and finish (with outcome summary)
- Log cache misses/loads only at `DEBUG`
- Do not log sensitive data (tokens, passwords, personal data)
- Prefer structured messages over string concatenation: `log.debugf("Loading employee %s", id)` etc.

## Testing

When writing tests, always invoke the **`java-junit`** skill for conventions and patterns.

Project-specific additions:
- **Unit tests** — plain JUnit 5 + Mockito, no CDI container
- **Integration tests** — `@QuarkusTest` with H2 (PostgreSQL mode), real CDI, real DB
- **REST tests** — `@QuarkusTest` + REST-Assured for HTTP endpoint assertions
- Use `@InjectMock` (Quarkus CDI mock) in `@QuarkusTest`; use plain `@Mock` + `@InjectMocks` in unit tests