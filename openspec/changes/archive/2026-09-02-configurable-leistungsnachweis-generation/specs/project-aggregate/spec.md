## MODIFIED Requirements

### Requirement: Project aggregate encapsulates identity and master data
The `Project` aggregate SHALL be modeled as an immutable, record-oriented type holding a stable internal `ProjectId` (UUID), a ZEP numeric id (`zepId`), a unique `name`, a `startDate`, an optional `endDate`, a `billable` boolean flag, a `leistungsnachweisEnabled` boolean flag, and a set of `UserId` references representing project leads. State transitions such as ZEP resync and project lead sync SHALL return new Project instances instead of mutating existing state. The aggregate SHALL NOT hold any workflow state (that is the concern of a future capability).

The `leistungsnachweisEnabled` flag is MEGA-managed (not sourced from ZEP) and SHALL default to `true` on project creation. It SHALL be preserved across ZEP resync and project lead sync. A dedicated state transition SHALL return a new Project instance with the flag set to a caller-provided value.

`MonthEndProjectSnapshot` — the monthend-specific read model derived from `Project` — SHALL carry `{ ProjectId id, int zepId, String name, boolean billable, boolean leistungsnachweisEnabled, Set<UserId> leadIds }` only. It SHALL NOT carry `startDate` or `endDate`; activeness filtering for a given month SHALL be enforced in the adapter before returning the snapshot to the application layer.

#### Scenario: Project created from ZEP profile data
- **WHEN** `Project.create(ProjectId, ZepProjectProfile)` is called
- **THEN** a new immutable Project instance is returned with name, zepId, startDate, endDate, and billable populated from the profile
- **THEN** the leads set is empty
- **THEN** `leistungsnachweisEnabled` is `true`

#### Scenario: Project reconstituted from persisted state
- **WHEN** `new Project(id, zepId, name, startDate, endDate, billable, leistungsnachweisEnabled, leads)` is called
- **THEN** a new immutable Project instance is returned with all fields set as provided

#### Scenario: Existing project resynced from ZEP
- **WHEN** `Project.withSyncedZepData(ZepProjectProfile)` is called with updated profile data
- **THEN** a new Project instance is returned with updated name, startDate, endDate, and billable fields
- **THEN** the existing `ProjectId`, leads set, and `leistungsnachweisEnabled` flag are preserved

#### Scenario: Leistungsnachweis flag is toggled
- **WHEN** `Project.withLeistungsnachweisEnabled(false)` is called on a project whose flag is `true`
- **THEN** a new Project instance is returned with `leistungsnachweisEnabled` set to `false`
- **THEN** all other fields, including `ProjectId` and leads, are preserved

#### Scenario: MonthEndProjectSnapshot does not carry date range fields
- **WHEN** `MonthEndProjectSnapshot` is constructed
- **THEN** it contains `id`, `zepId`, `name`, `billable`, `leistungsnachweisEnabled`, and `leadIds` only
- **THEN** no `startDate` or `endDate` field exists on `MonthEndProjectSnapshot`

### Requirement: Project leads are a set of UserId references
The `leads` field SHALL be a `Set<UserId>` carried directly on the `Project` aggregate. The `Project` aggregate SHALL NOT hold raw UUID collections or references to full `User` objects. Lead replacement during project lead sync SHALL return a new Project instance with the resolved `UserId` values. Lead replacement SHALL preserve the `leistungsnachweisEnabled` flag.

#### Scenario: Project has no leads after initial sync
- **WHEN** a project is first created via `SyncProjectsUseCase`
- **THEN** its leads set is empty

#### Scenario: Leads are set by project lead sync
- **WHEN** `SyncProjectLeadsUseCase` resolves lead usernames for a project
- **THEN** the project's leads set is replaced with the resolved `UserId` values on a new Project instance
- **THEN** the `leistungsnachweisEnabled` flag is preserved on the new instance
