# Hexagon Boundary Conventions

## Purpose

Defines shared boundary conventions for the hexagon backend, including authenticated actor resolution, shared kernel ownership, identifier-based aggregate references, dedicated identity lookup boundaries, and isolation from legacy compatibility concerns.

## Requirements

### Requirement: Hexagon inbound adapters use one shared authenticated actor context
The hexagon SHALL provide one shared request-scoped authenticated actor context for inbound adapters. Hexagon REST adapters MUST resolve the acting user through that shared context and MUST pass explicit actor identifiers into use cases instead of introducing domain-specific actor resolver components.

#### Scenario: Month-end adapter resolves the acting user through the shared context
- **WHEN** a month-end REST adapter needs the acting user for a request
- **THEN** it obtains the actor identity from the shared authenticated actor context and passes the actor identifier into the month-end use case

#### Scenario: Worktime adapter resolves the acting user through the shared context
- **WHEN** a worktime REST adapter needs the acting user for a request
- **THEN** it uses the same shared authenticated actor context instead of a worktime-specific actor resolver

### Requirement: Shared kernel ownership is limited to stable cross-cutting concepts
The hexagon SHALL define a small shared kernel for concepts that are reused across multiple hexagon domains and express the same meaning everywhere. Identity value objects, shared authorization vocabulary, and authenticated actor abstractions MUST live in that shared kernel, while domain-specific snapshots and read models MUST remain in the consuming domain. `UserId`, `ProjectId`, and `Email` are designated shared kernel residents and MUST be defined in `shared/domain/model/` (`com.gepardec.mega.hexagon.shared.domain.model`). No module domain MAY define its own copy of these types.

#### Scenario: Shared identity concern is promoted to the shared kernel
- **WHEN** a concept is reused by multiple hexagon domains as the same identity or authorization concern
- **THEN** that concept is defined once in the shared kernel instead of being duplicated in each domain

#### Scenario: UserId is sourced from the shared kernel
- **WHEN** any hexagon module references a user identity value
- **THEN** it imports `UserId` from `com.gepardec.mega.hexagon.shared.domain.model`

#### Scenario: ProjectId is sourced from the shared kernel
- **WHEN** any hexagon module references a project identity value
- **THEN** it imports `ProjectId` from `com.gepardec.mega.hexagon.shared.domain.model`

#### Scenario: Email is sourced from the shared kernel
- **WHEN** any hexagon module references an email identity value
- **THEN** it imports `Email` from `com.gepardec.mega.hexagon.shared.domain.model`

#### Scenario: Domain-shaped read model remains local
- **WHEN** a type exists to express month-end or worktime specific context
- **THEN** that type remains in the consuming domain rather than being moved into the shared kernel

### Requirement: Inbound ports belong in the application layer
Driver port interfaces (`*UseCase`) define how the outside world drives the application. They are application boundary contracts and MUST be placed in the module's `application.port.inbound` package (`<module>.application.port.inbound`). Supporting boundary-specific contract types that exist only for that inbound API, such as sync result records, SHOULD live there as well. Implementations remain in the module's `application` package. No `*UseCase` interface SHALL reside in a `domain.port.inbound` package.

#### Scenario: Use case interface lives in the inbound application port package
- **WHEN** a new use case interface is defined for a hexagon module
- **THEN** it is placed in `<module>.application.port.inbound`

#### Scenario: Supporting inbound contract record lives next to the use case interface
- **WHEN** a module defines a boundary-specific record that exists only to support an inbound use case contract
- **THEN** it is placed in `<module>.application.port.inbound`

#### Scenario: No use case interface in domain.port.inbound
- **WHEN** the source tree is examined
- **THEN** no `*UseCase` interface exists under any `domain.port.inbound` package

### Requirement: Cross-aggregate persistence references remain identifier-based
Hexagon persistence models MUST represent references to other aggregates with identifiers or identifier collections unless both objects belong to the same aggregate boundary. Domains that need additional context for those foreign references MUST obtain it through explicit query or read-model ports and map the result into local domain projections.

#### Scenario: Month-end task persists cross-domain references as identifiers
- **WHEN** a month-end task refers to a project, subject employee, or eligible actors
- **THEN** the persistence model stores identifier values and the application resolves additional display or business context through explicit query ports

#### Scenario: New domain needs foreign aggregate details
- **WHEN** a hexagon domain needs project or user details owned by another domain
- **THEN** it defines or consumes an explicit read-model port instead of introducing a JPA relationship to the foreign aggregate

### Requirement: Cross-domain user identity resolution uses a dedicated lookup boundary
When a hexagon domain needs to resolve a `UserId` from a foreign identity such as a username supplied by another system, it MUST delegate that resolution to a dedicated `UserIdentityLookupPort` boundary instead of embedding ad hoc lookup logic in the consuming use case or unrelated adapter.

#### Scenario: Lead reconciliation resolves user identity through the dedicated boundary
- **WHEN** a project or reconciliation flow needs to resolve a `UserId` from a foreign username
- **THEN** it delegates to `UserIdentityLookupPort` instead of embedding direct lookup logic in the consuming use case

#### Scenario: New cross-domain identity lookup follows the same boundary
- **WHEN** another hexagon use case needs to resolve a `UserId` from a foreign identity
- **THEN** it uses the same dedicated identity lookup boundary rather than introducing a parallel lookup abstraction

### Requirement: Legacy compatibility is isolated from hexagon domain packages
When the hexagon must temporarily interoperate with legacy application concerns, the compatibility code MUST be isolated behind shared boundary components rather than being copied across hexagon domain packages. New shared hexagon concerns MUST be introduced in hexagon-owned packages instead of depending directly on legacy domain helpers.

#### Scenario: Shared authorization concern is reused by multiple hexagon domains
- **WHEN** multiple hexagon domains need the same authorization or actor-boundary concern
- **THEN** the concern is implemented in a shared hexagon-owned boundary component and any legacy bridge is isolated there

#### Scenario: New hexagon feature needs a shared helper already leaking from legacy code
- **WHEN** a new hexagon feature needs a shared cross-cutting helper that currently lives in legacy code
- **THEN** the implementation extracts or bridges that concern through a shared hexagon-owned boundary instead of adding another direct legacy import inside the feature domain
