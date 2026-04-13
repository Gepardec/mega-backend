## MODIFIED Requirements

### Requirement: Resolved leads replace the project's existing leads set
The system SHALL replace the full leads set on each project with the freshly resolved `UserId` set from ZEP by deriving a new immutable `Project` aggregate for that project. Leads from a previous reconcile cycle that are no longer returned by ZEP SHALL be removed, and unchanged lead sets MAY be skipped from persistence.

#### Scenario: Stale lead is removed on next reconcile
- **WHEN** a username that was previously a lead is no longer returned by ZEP for that project
- **THEN** the corresponding `UserId` is removed from the project's leads set after reconciliation

#### Scenario: New lead is added on reconcile
- **WHEN** a username is returned by ZEP as a lead for a project
- **THEN** the resolved `UserId` is present in the project's leads set after reconciliation

#### Scenario: Unchanged lead set does not alter project master data
- **WHEN** the resolved lead set for a project matches the currently persisted leads set
- **THEN** reconciliation keeps the project's master data unchanged

### Requirement: Project lead sync boundary uses sync-oriented names
The system SHALL expose project lead synchronization through `SyncProjectLeadsUseCase` and `SyncProjectLeadsService`. The boundary SHALL use a `sync()` entrypoint and SHALL return `ProjectLeadSyncResult`.

#### Scenario: Project lead sync returns a named sync result
- **WHEN** `SyncProjectLeadsUseCase.sync()` completes
- **THEN** it returns `ProjectLeadSyncResult`
- **THEN** the result exposes `resolved`, `skipped`, `rolesAdded`, and `rolesRevoked`

## ADDED Requirements

### Requirement: SyncProjectLeadsService is a CDI-managed application-service boundary
The system SHALL implement `SyncProjectLeadsUseCase` with a CDI-managed application service that owns the transaction boundary for project lead sync and role derivation. `SyncScheduler` SHALL inject the use case via the inbound port instead of manually constructing the service implementation.

#### Scenario: Scheduler injects the project lead sync use case
- **WHEN** the unified `SyncScheduler` starts
- **THEN** it receives `SyncProjectLeadsUseCase` via CDI injection
- **THEN** it does not construct `SyncProjectLeadsService` manually

#### Scenario: Project lead sync runs in one application-service transaction
- **WHEN** `SyncProjectLeadsUseCase.sync()` is invoked
- **THEN** project lead replacement and user role reconciliation occur within the service-owned transaction boundary

## REMOVED Requirements

### Requirement: UserLookupPort is defined in the project domain
**Reason**: The reshape no longer imposes a project-local anti-import rule for stable user identity value objects while the wider shared-boundary work is still in progress.
**Migration**: Continue exposing a lead-lookup capability for reconciliation, but allow the implementation to use the stable `UserId` and `ZepUsername` types that back the wider hexagon until the shared-boundary refactor finalizes their long-term home.

### Requirement: SyncProjectLeadsUseCase is decoupled from Quarkus infrastructure
**Reason**: Project lead sync now follows the same CDI-managed application-service pattern as project sync and the reshaped user domain.
**Migration**: Keep Quarkus annotations and transaction management on the service implementation, while leaving the `Project` aggregate, `SyncProjectLeadsUseCase`, and result records framework-free.
