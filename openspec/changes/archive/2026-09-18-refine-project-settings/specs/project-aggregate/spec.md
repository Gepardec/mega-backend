## MODIFIED Requirements

### Requirement: Project aggregate encapsulates identity and master data
The `Project` aggregate SHALL be modeled as an immutable, record-oriented type holding a stable internal `ProjectId` (UUID), a ZEP numeric id (`zepId`), a unique `name`, a `startDate`, an optional `endDate`, a `billable` boolean flag, a `leistungsnachweisEnabled` boolean flag, and a set of `UserId` references representing project leads. State transitions such as ZEP resync and project lead sync SHALL return new Project instances instead of mutating existing state. The aggregate SHALL NOT hold any workflow state (that is the concern of a future capability).

The `leistungsnachweisEnabled` flag is MEGA-managed (not sourced from ZEP). Its value on creation and its behavior on ZEP resync are governed by the requirement "Leistungsnachweis can only be enabled on billable projects". It SHALL be preserved across project lead sync. A dedicated state transition SHALL return a new Project instance with the flag set to a caller-provided value, subject to the same requirement.

`MonthEndProjectSnapshot` — the monthend-specific read model derived from `Project` — SHALL carry `{ ProjectId id, int zepId, String name, boolean billable, boolean leistungsnachweisEnabled, Set<UserId> leadIds }` only. It SHALL NOT carry `startDate` or `endDate`; activeness filtering for a given month SHALL be enforced in the adapter before returning the snapshot to the application layer.

#### Scenario: Project created from ZEP profile data
- **WHEN** `Project.create(ProjectId, ZepProjectProfile)` is called
- **THEN** a new immutable Project instance is returned with name, zepId, startDate, endDate, and billable populated from the profile
- **THEN** the leads set is empty
- **THEN** `leistungsnachweisEnabled` equals the profile's billability

#### Scenario: Project reconstituted from persisted state
- **WHEN** `new Project(id, zepId, name, startDate, endDate, billable, leistungsnachweisEnabled, leads)` is called with values that satisfy the billability rule for Leistungsnachweis
- **THEN** a new immutable Project instance is returned with all fields set as provided

#### Scenario: Existing project resynced from ZEP
- **WHEN** `Project.withSyncedZepData(ZepProjectProfile)` is called with updated profile data
- **THEN** a new Project instance is returned with updated name, startDate, endDate, and billable fields
- **THEN** the existing `ProjectId` and leads set are preserved

#### Scenario: Leistungsnachweis flag is toggled
- **WHEN** `Project.withLeistungsnachweisEnabled(false)` is called on a project whose flag is `true`
- **THEN** a new Project instance is returned with `leistungsnachweisEnabled` set to `false`
- **THEN** all other fields, including `ProjectId` and leads, are preserved

#### Scenario: MonthEndProjectSnapshot does not carry date range fields
- **WHEN** `MonthEndProjectSnapshot` is constructed
- **THEN** it contains `id`, `zepId`, `name`, `billable`, `leistungsnachweisEnabled`, and `leadIds` only
- **THEN** no `startDate` or `endDate` field exists on `MonthEndProjectSnapshot`

## ADDED Requirements

### Requirement: Leistungsnachweis can only be enabled on billable projects
A non-billable project SHALL always have Leistungsnachweis disabled. No project SHALL exist with Leistungsnachweis enabled while it is non-billable, either in memory or in storage.

The flag SHALL follow billability as follows:
- **Creation:** a billable project starts with Leistungsnachweis enabled (opt-out default). A non-billable project starts with it disabled.
- **ZEP resync, project stays billable:** the flag keeps its current value.
- **ZEP resync, billable project becomes non-billable:** the flag SHALL be disabled.
- **ZEP resync, non-billable project becomes billable again:** the flag SHALL be enabled (opt-out default). An opt-out made before the project became non-billable is not remembered.

Enabling Leistungsnachweis on a non-billable project SHALL be rejected with a project domain error. Disabling it on a non-billable project SHALL be accepted and leave the flag disabled.

Changes to the flag caused by these rules follow the month-end generation-time rule: they only affect later generation runs and never change month-end tasks that were already generated.

#### Scenario: New billable project starts with Leistungsnachweis enabled
- **WHEN** project sync creates a project from ZEP data that marks it billable
- **THEN** the stored project has Leistungsnachweis enabled

#### Scenario: New non-billable project starts with Leistungsnachweis disabled
- **WHEN** project sync creates a project from ZEP data that marks it non-billable
- **THEN** the stored project has Leistungsnachweis disabled

#### Scenario: Lead's opt-out survives resync while the project stays billable
- **WHEN** a lead has disabled Leistungsnachweis on a billable project and a later ZEP resync still marks the project billable
- **THEN** the project keeps Leistungsnachweis disabled

#### Scenario: Project becoming non-billable loses Leistungsnachweis
- **WHEN** a billable project with Leistungsnachweis enabled is resynced from ZEP data that marks it non-billable
- **THEN** the stored project has Leistungsnachweis disabled

#### Scenario: Project becoming billable again gets Leistungsnachweis re-enabled
- **WHEN** a non-billable project is resynced from ZEP data that marks it billable
- **THEN** the stored project has Leistungsnachweis enabled
- **THEN** this holds even if a lead had disabled Leistungsnachweis before the project became non-billable

#### Scenario: Enabling Leistungsnachweis on a non-billable project is rejected
- **WHEN** Leistungsnachweis is enabled on a non-billable project
- **THEN** the change is rejected with a project domain error
- **THEN** the project keeps Leistungsnachweis disabled

#### Scenario: Disabling Leistungsnachweis on a non-billable project is accepted
- **WHEN** Leistungsnachweis is disabled on a non-billable project
- **THEN** the change is accepted and the project keeps Leistungsnachweis disabled

#### Scenario: Stored data follows the rule
- **WHEN** the stored projects are inspected after this change is deployed, including projects stored before it
- **THEN** no non-billable project is stored with Leistungsnachweis enabled
- **THEN** every billable project stored before this change keeps its existing Leistungsnachweis value
