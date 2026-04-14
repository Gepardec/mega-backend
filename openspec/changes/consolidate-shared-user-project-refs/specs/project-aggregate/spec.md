## MODIFIED Requirements

### Requirement: Project aggregate encapsulates identity and master data
The `Project` aggregate SHALL be modeled as an immutable, record-oriented type holding a stable internal `ProjectId` (UUID), a ZEP numeric id (`zepId`), a unique `name`, a `startDate`, an optional `endDate`, a `billable` boolean flag, and a set of `UserId` references representing project leads. State transitions such as ZEP resync and project lead sync SHALL return new Project instances instead of mutating existing state. The aggregate SHALL NOT hold any workflow state (that is the concern of a future capability).

`MonthEndProjectSnapshot` — the monthend-specific read model derived from `Project` — SHALL carry `{ ProjectId id, int zepId, String name, boolean billable, Set<UserId> leadIds }` only. It SHALL NOT carry `startDate` or `endDate`; activeness filtering for a given month SHALL be enforced in the adapter before returning the snapshot to the application layer.

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

#### Scenario: MonthEndProjectSnapshot does not carry date range fields
- **WHEN** `MonthEndProjectSnapshot` is constructed
- **THEN** it contains `id`, `zepId`, `name`, `billable`, and `leadIds` only
- **THEN** no `startDate` or `endDate` field exists on `MonthEndProjectSnapshot`
