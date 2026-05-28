# Project Sync

## Purpose

Defines the `SyncProjectsUseCase` and its `SyncProjectsService` implementation within the hexagonal domain. The sync is responsible for keeping the `Project` aggregate store in sync with ZEP (authoritative source for projects), running automatically as part of the unified sync schedule.

## Requirements

### Requirement: Sync fetches all active projects from ZEP
The system SHALL fetch all projects from ZEP via `ZepProjectPort.fetchAll()`. The port implementation SHALL handle pagination internally. No `YearMonth` filter SHALL be applied — all projects returned by ZEP are candidates for upsert.

#### Scenario: All ZEP projects are fetched regardless of month
- **WHEN** `SyncProjectsUseCase.sync()` is called
- **THEN** `ZepProjectPort.fetchAll()` is called and returns all available projects
- **THEN** no month-based filtering is applied to the result

#### Scenario: ZepProjectPort handles pagination internally
- **WHEN** ZEP returns multiple pages of projects
- **THEN** `ZepProjectPort.fetchAll()` returns a single flat list of all projects

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

### Requirement: Sync does not assign leads
The `SyncProjectsUseCase` SHALL NOT fetch or assign project leads. Lead assignment is the sole responsibility of `SyncProjectLeadsUseCase`. After `SyncProjectsUseCase` completes, all projects in the repository have an empty or unchanged leads set.

#### Scenario: Project leads are not touched during sync
- **WHEN** `SyncProjectsUseCase.sync()` completes
- **THEN** no call is made to any ZEP endpoint for project employees
- **THEN** the leads set of each project is not modified

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

#### Scenario: Result reflects projects created during sync
- **WHEN** `SyncProjectsUseCase.sync()` creates N new Projects
- **THEN** the returned `ProjectSyncResult.created()` equals N

#### Scenario: Result reflects projects updated during sync
- **WHEN** `SyncProjectsUseCase.sync()` updates M existing Projects
- **THEN** the returned `ProjectSyncResult.updated()` equals M

#### Scenario: Result reflects unchanged projects
- **WHEN** `SyncProjectsUseCase.sync()` encounters U projects whose synchronized state equals the persisted state
- **THEN** the returned `ProjectSyncResult.unchanged()` equals U

### Requirement: SyncProjectsService is a CDI-managed application-service boundary
The system SHALL implement `SyncProjectsUseCase` with a CDI-managed application service that owns the transaction boundary for a sync invocation. `SyncScheduler` SHALL inject the use case via the inbound port instead of manually constructing the service implementation.

#### Scenario: Scheduler injects the sync use case
- **WHEN** the unified `SyncScheduler` starts
- **THEN** it receives `SyncProjectsUseCase` via CDI injection
- **THEN** it does not construct `SyncProjectsService` manually

#### Scenario: Project sync runs in one application-service transaction
- **WHEN** `SyncProjectsUseCase.sync()` is invoked
- **THEN** project creation and update persistence occurs within the service-owned transaction boundary
