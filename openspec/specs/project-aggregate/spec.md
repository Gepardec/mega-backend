# Project Aggregate

## Purpose

Defines the `Project` aggregate and its supporting value objects within the hexagonal domain. The aggregate represents a ZEP project with stable internal identity, master data, and a set of lead references — decoupled from all framework infrastructure.

## Requirements

### Requirement: Project aggregate encapsulates identity and master data
The `Project` aggregate SHALL hold a stable internal `ProjectId` (UUID), a ZEP numeric id (`zepId`), a unique `name`, a `startDate`, an optional `endDate`, a `billable` boolean flag, and a set of `UserId` references representing project leads. The aggregate SHALL NOT hold any workflow state (that is the concern of a future capability).

#### Scenario: Project created from ZEP profile data
- **WHEN** `Project.create(ProjectId, ZepProjectProfile)` is called
- **THEN** a Project instance is returned with name, zepId, startDate, endDate, and billable populated from the profile
- **THEN** the leads set is empty

#### Scenario: Project reconstituted from persisted state
- **WHEN** `Project.reconstitute(id, zepId, name, startDate, endDate, billable, leads)` is called
- **THEN** a Project instance is returned with all fields set as provided

#### Scenario: Billable flag updated on re-sync
- **WHEN** `Project.syncFromZep(ZepProjectProfile)` is called with an updated profile
- **THEN** the project's `billable` field is updated to reflect the new profile value

### Requirement: ZepProjectProfile carries billability
The `ZepProjectProfile` value object SHALL include a `billable` boolean field derived from the ZEP `billingType.id`. A project is billable when `billingType.id` is 1 (BILLABLE) or 2 (BILLABLE_FIXED). A null `billingType` SHALL be treated as non-billable.

#### Scenario: Billable project profile derived from ZEP billing type id 1
- **WHEN** `ZepProject.billingType.id()` is 1
- **THEN** `ZepProjectProfile.billable` is true

#### Scenario: Billable project profile derived from ZEP billing type id 2
- **WHEN** `ZepProject.billingType.id()` is 2
- **THEN** `ZepProjectProfile.billable` is true

#### Scenario: Non-billable project profile derived from ZEP billing type id 3 or 4
- **WHEN** `ZepProject.billingType.id()` is 3 or 4
- **THEN** `ZepProjectProfile.billable` is false

#### Scenario: Missing billing type defaults to non-billable
- **WHEN** `ZepProject.billingType` is null
- **THEN** `ZepProjectProfile.billable` is false

### Requirement: Project identity is stable across syncs
The `ProjectId` (UUID) SHALL be generated once on first creation and never changed by subsequent syncs. `zepId` SHALL serve as the lookup key when checking whether a project already exists.

#### Scenario: Existing project retains its UUID on re-sync
- **WHEN** a project with an existing `zepId` is synced again
- **THEN** the `ProjectId` is not regenerated
- **THEN** only mutable fields (name, endDate) are updated

### Requirement: Project leads are a set of UserId references
The `leads` field SHALL be a `Set<UserId>`. The `Project` aggregate SHALL NOT hold references to full `User` objects. Leads are populated exclusively by `ReconcileLeadsUseCase`, not by `SyncProjectsUseCase`.

#### Scenario: Project has no leads after initial sync
- **WHEN** a project is first created via `SyncProjectsUseCase`
- **THEN** its leads set is empty

#### Scenario: Leads are set by reconciliation
- **WHEN** `ReconcileLeadsUseCase` resolves lead usernames for a project
- **THEN** the project's leads set is replaced with the resolved `UserId` values

### Requirement: Project is decoupled from Quarkus and JPA infrastructure
The `Project` aggregate and its supporting value objects (`ProjectId`, `ZepProjectProfile`) SHALL NOT import or depend on any Quarkus, CDI, or JPA annotations.

#### Scenario: Project class has no framework imports
- **WHEN** `Project.java` is compiled
- **THEN** it imports only from `com.gepardec.mega.hexagon.project`, `java.*`, and standard libraries
