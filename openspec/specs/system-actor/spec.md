# System Actor

## Purpose

Defines the MEGA application's first-class system actor identity: its stable UUID constant, its `Role.SYSTEM` designation, and the rules governing how it is excluded from human-user queries and rendered in REST responses.

## Requirements

### Requirement: The MEGA application has a first-class system actor identity
The system SHALL define a `Role.SYSTEM` value in the shared `Role` enum. A well-known system actor row SHALL be seeded in `hexagon_users` via Liquibase with a stable, well-known UUID. The system actor's `UserId` SHALL be exposed as `SystemActor.USER_ID` in `com.gepardec.mega.hexagon.shared.domain.model`. The system actor SHALL have no ZEP username, no email, no Personio ID, and no employment periods.

#### Scenario: System actor row is present after migration
- **WHEN** the Liquibase changelog has been applied
- **THEN** `hexagon_users` contains exactly one row with `Role.SYSTEM`
- **THEN** that row's UUID matches the `SystemActor.USER_ID` constant

### Requirement: System actor is excluded from active user snapshot queries
The system SHALL never return the system actor from any query that resolves active users for a given month. Because the system actor has no employment periods, `isActiveIn()` SHALL return false, causing snapshot adapters to naturally exclude it.

#### Scenario: System actor does not appear in findActiveIn results
- **WHEN** `MonthEndUserSnapshotPort.findActiveIn(month)` is called
- **THEN** the result does not contain a `UserRef` with `id` equal to `SystemActor.USER_ID`

### Requirement: REST adapters resolve SystemActor.USER_ID to a display name without a user snapshot lookup
The system SHALL handle `SystemActor.USER_ID` as a special display case in REST adapters that resolve actor identifiers (e.g., `completedBy`, `createdBy`). The display name SHALL be "MEGA System". No call to `MonthEndUserSnapshotPort.findByIds` SHALL be made for `SystemActor.USER_ID`.

#### Scenario: completedBy SystemActor.USER_ID renders as MEGA System
- **WHEN** a REST adapter resolves the `completedBy` field of a task completed by the system
- **THEN** the response contains display name "MEGA System"
- **THEN** no user snapshot query is issued for `SystemActor.USER_ID`
