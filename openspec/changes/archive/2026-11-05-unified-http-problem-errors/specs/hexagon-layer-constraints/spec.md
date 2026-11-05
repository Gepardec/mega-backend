# Spec Delta

## ADDED Requirements

### Requirement: Domain and application layers are free of HTTP transport types
Types in `..hexagon..domain..` and `..hexagon..application..` SHALL NOT depend on any type in `jakarta.ws.rs..` or in the problem-details library's packages (`io.quarkiverse.httpproblem..`). HTTP status codes, responses and problem documents are inbound-adapter concerns. This rule SHALL be enforced by an ArchUnit test.

#### Scenario: Domain type does not reference JAX-RS
- **WHEN** a type resides in `..hexagon..domain..`
- **THEN** it SHALL NOT depend on any type in `jakarta.ws.rs..` or `io.quarkiverse.httpproblem..`

#### Scenario: Application type does not reference JAX-RS
- **WHEN** a type resides in `..hexagon..application..`
- **THEN** it SHALL NOT depend on any type in `jakarta.ws.rs..` or `io.quarkiverse.httpproblem..`
