## MODIFIED Requirements

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

## ADDED Requirements

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
