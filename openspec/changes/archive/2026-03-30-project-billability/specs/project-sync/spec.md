## MODIFIED Requirements

### Requirement: Sync upserts project master data by zepId
The system SHALL upsert each project using `zepId` as the stable lookup key. If no project with the given `zepId` exists, a new `Project` aggregate is created with a generated `ProjectId`. If a project with the given `zepId` already exists, its mutable fields (name, startDate, endDate, billable) SHALL be updated.

#### Scenario: New project is created on first sync
- **WHEN** ZEP returns a project whose `zepId` is not in the repository
- **THEN** a new Project is created with a generated UUID and persisted
- **THEN** the new Project's `billable` field reflects the ZEP billing type

#### Scenario: Existing project is updated on subsequent sync
- **WHEN** ZEP returns a project whose `zepId` already exists in the repository
- **THEN** the existing Project's name, startDate, endDate, and billable are updated
- **THEN** the existing ProjectId (UUID) is preserved
