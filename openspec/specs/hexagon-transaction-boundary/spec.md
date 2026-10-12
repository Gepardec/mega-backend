# Hexagon Transaction Boundary

## Purpose

Defines where transaction boundaries are allowed in the hexagon backend and enforces that application services own `@Transactional`.

## Requirements

### Requirement: Transaction boundary owned exclusively by application services
Within the `com.gepardec.mega.hexagon` package, `@Transactional` SHALL be applied exclusively to classes residing in an `application` sub-package. No class outside an `application` sub-package (including outbound adapters, inbound adapters, and domain classes) SHALL carry a `@Transactional` annotation. This rule SHALL be enforced by an ArchUnit architecture test.

#### Scenario: Outbound adapter is not annotated
- **WHEN** a class resides in `..hexagon..adapter.outbound..`
- **THEN** the class SHALL NOT be annotated with `@Transactional` at either class or method level

#### Scenario: Inbound adapter is not annotated
- **WHEN** a class resides in `..hexagon..adapter.inbound..`
- **THEN** the class SHALL NOT be annotated with `@Transactional` at either class or method level

#### Scenario: Domain class is not annotated
- **WHEN** a class resides in `..hexagon..domain..`
- **THEN** the class SHALL NOT be annotated with `@Transactional` at either class or method level

#### Scenario: ArchUnit rule blocks annotation in wrong layer
- **WHEN** a developer adds `@Transactional` to an outbound adapter class
- **THEN** the ArchUnit test SHALL fail, preventing the build from passing

### Requirement: All hexagon application services must declare a transaction boundary
Every class in `..hexagon..application..` that implements a `*UseCase` interface SHALL be annotated with `@Transactional`. This ensures no use case implementation silently executes outside a transaction scope.

#### Scenario: Application service implements UseCase and is annotated
- **WHEN** a class in `..hexagon..application..` implements a `*UseCase` interface
- **THEN** the class SHALL be annotated with `@Transactional`

#### Scenario: ArchUnit rule blocks missing annotation on application service
- **WHEN** a developer adds a new application service implementing a `*UseCase` interface without `@Transactional`
- **THEN** the ArchUnit test SHALL fail, preventing the build from passing
