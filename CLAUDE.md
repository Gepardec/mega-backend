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

## Architecture

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