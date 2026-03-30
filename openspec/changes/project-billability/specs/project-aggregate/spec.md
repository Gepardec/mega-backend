## MODIFIED Requirements

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

## ADDED Requirements

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
