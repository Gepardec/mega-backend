## MODIFIED Requirements

### Requirement: Sync upserts project master data by zepId
The system SHALL upsert each project using `zepId` as the stable lookup key. If no project with the given `zepId` exists, a new `Project` aggregate is created with a generated `ProjectId`. If a project with the given `zepId` already exists, sync SHALL derive a new immutable `Project` aggregate from the latest `ZepProjectProfile` while preserving the existing `ProjectId` and leads set.

#### Scenario: New project is created on first sync
- **WHEN** ZEP returns a project whose `zepId` is not in the repository
- **THEN** a new Project is created with a generated UUID and persisted
- **THEN** the new Project's `billable` field reflects the ZEP billing type

#### Scenario: Existing project is updated on subsequent sync
- **WHEN** ZEP returns a project whose `zepId` already exists in the repository
- **THEN** sync derives a new Project instance with updated name, startDate, endDate, and billable
- **THEN** the existing `ProjectId` and leads set are preserved

### Requirement: Sync persists all changes in one batch
The system SHALL process the full ZEP response, determine which Projects are newly created or changed, and persist those Projects via `ProjectRepository.saveAll()` within one transactional sync invocation.

#### Scenario: All project changes saved at end of sync
- **WHEN** all ZEP projects have been mapped to domain objects
- **THEN** all created and changed Projects are persisted via `ProjectRepository.saveAll()`

#### Scenario: Unchanged project is not counted as an update
- **WHEN** a ZEP project maps to the same Project state that is already persisted
- **THEN** it is not counted in `ProjectSyncResult.updated()`

### Requirement: Sync returns a result with operation counts
`SyncProjectsUseCase.sync()` SHALL return a `ProjectSyncResult` record containing integer fields `created`, `updated`, and `unchanged`. `unchanged` counts ZEP projects whose synchronized aggregate state equals the already persisted state and that therefore do not require persistence. The scheduler SHALL use these counts when composing its log output.

#### Scenario: Result reflects unchanged projects
- **WHEN** `SyncProjectsUseCase.sync()` encounters U projects whose synchronized state equals the persisted state
- **THEN** the returned `ProjectSyncResult.unchanged()` equals U

## ADDED Requirements

### Requirement: SyncProjectsService is a CDI-managed application-service boundary
The system SHALL implement `SyncProjectsUseCase` with a CDI-managed application service that owns the transaction boundary for a sync invocation. `SyncScheduler` SHALL inject the use case via the inbound port instead of manually constructing the service implementation.

#### Scenario: Scheduler injects the sync use case
- **WHEN** the unified `SyncScheduler` starts
- **THEN** it receives `SyncProjectsUseCase` via CDI injection
- **THEN** it does not construct `SyncProjectsService` manually

#### Scenario: Project sync runs in one application-service transaction
- **WHEN** `SyncProjectsUseCase.sync()` is invoked
- **THEN** project creation and update persistence occurs within the service-owned transaction boundary

## REMOVED Requirements

### Requirement: SyncProjectsUseCase is decoupled from Quarkus infrastructure
**Reason**: The project sync use case now follows the same CDI-managed application-service pattern as the reshaped user domain and the existing month-end services.
**Migration**: Keep Quarkus annotations and transaction management on the service implementation, while leaving the `Project` aggregate and `SyncProjectsUseCase` interface framework-free.
