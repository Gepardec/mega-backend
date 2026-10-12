## MODIFIED Requirements

### Requirement: Project aggregate encapsulates identity and master data
The `Project` aggregate SHALL be modeled as an immutable, record-oriented type holding a stable internal `ProjectId` (UUID), a ZEP numeric id (`zepId`), a unique `name`, a `startDate`, an optional `endDate`, a `billable` boolean flag, and a set of `UserId` references representing project leads. State transitions such as ZEP resync and project lead sync SHALL return new Project instances instead of mutating existing state. The aggregate SHALL NOT hold any workflow state.

#### Scenario: Project created from ZEP profile data
- **WHEN** `Project.create(ProjectId, ZepProjectProfile)` is called
- **THEN** a new immutable Project instance is returned with name, zepId, startDate, endDate, and billable populated from the profile
- **THEN** the leads set is empty

#### Scenario: Project reconstituted from persisted state
- **WHEN** `new Project(id, zepId, name, startDate, endDate, billable, leads)` is called
- **THEN** a new immutable Project instance is returned with all fields set as provided

#### Scenario: Existing project resynced from ZEP
- **WHEN** `Project.withSyncedZepData(ZepProjectProfile)` is called with updated profile data
- **THEN** a new Project instance is returned with updated name, startDate, endDate, and billable fields
- **THEN** the existing `ProjectId` and leads set are preserved

### Requirement: Project leads are a set of UserId references
The `leads` field SHALL be a `Set<UserId>` carried directly on the `Project` aggregate. The `Project` aggregate SHALL NOT hold raw UUID collections or references to full `User` objects. Lead replacement during reconciliation SHALL return a new Project instance with the resolved `UserId` values.

#### Scenario: Project has no leads after initial sync
- **WHEN** a project is first created via `SyncProjectsUseCase`
- **THEN** its leads set is empty

#### Scenario: Leads are set by project lead sync
- **WHEN** `SyncProjectLeadsUseCase` resolves lead usernames for a project
- **THEN** the project's leads set is replaced with the resolved `UserId` values on a new Project instance
