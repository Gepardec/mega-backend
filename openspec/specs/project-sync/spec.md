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
The system SHALL upsert each project using `zepId` as the stable lookup key. If no project with the given `zepId` exists, a new `Project` aggregate is created with a generated `ProjectId`. If a project with the given `zepId` already exists, its mutable fields (name, startDate, endDate) SHALL be updated.

#### Scenario: New project is created on first sync
- **WHEN** ZEP returns a project whose `zepId` is not in the repository
- **THEN** a new Project is created with a generated UUID and persisted

#### Scenario: Existing project is updated on subsequent sync
- **WHEN** ZEP returns a project whose `zepId` already exists in the repository
- **THEN** the existing Project's name, startDate, and endDate are updated
- **THEN** the existing ProjectId (UUID) is preserved

### Requirement: Sync does not assign leads
The `SyncProjectsUseCase` SHALL NOT fetch or assign project leads. Lead assignment is the sole responsibility of `ReconcileLeadsUseCase`. After `SyncProjectsUseCase` completes, all projects in the repository have an empty or unchanged leads set.

#### Scenario: Project leads are not touched during sync
- **WHEN** `SyncProjectsUseCase.sync()` completes
- **THEN** no call is made to any ZEP endpoint for project employees
- **THEN** the leads set of each project is not modified

### Requirement: Sync persists all changes in one batch
The system SHALL persist all created and updated projects via `ProjectRepository.saveAll()` after processing the full ZEP response.

#### Scenario: All project changes saved at end of sync
- **WHEN** all ZEP projects have been mapped to domain objects
- **THEN** all created and updated Projects are persisted via `ProjectRepository.saveAll()`

### Requirement: SyncProjectsUseCase is decoupled from Quarkus infrastructure
The `SyncProjectsUseCase` interface and its `SyncProjectsService` implementation SHALL NOT import or depend on any Quarkus, CDI, or JPA annotations. The Quarkus scheduler SHALL call the use case through the inbound port interface only.

#### Scenario: SyncProjectsService has no Quarkus imports
- **WHEN** `SyncProjectsService` is compiled
- **THEN** it imports only from `com.gepardec.mega.hexagon`, `java.*`, and standard libraries
