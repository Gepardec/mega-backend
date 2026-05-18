## ADDED Requirements

### Requirement: Sync populates release date from ZEP employee data
The system SHALL read each employee's release date from ZEP during the sync and persist it locally. If an employee has no release date in ZEP, the locally stored value SHALL be set to null. The sync SHALL NOT skip an employee solely because their release date is absent.

#### Scenario: Release date present in ZEP is persisted during sync
- **WHEN** a ZEP employee has a release date set
- **THEN** that release date is persisted locally for that employee after the sync runs

#### Scenario: Release date absent in ZEP is stored as null
- **WHEN** a ZEP employee has no release date
- **THEN** the employee's locally stored release date is null after the sync runs
- **THEN** sync proceeds normally without error

## MODIFIED Requirements

### Requirement: Sync persists only local user state and stable provider references
Routine user sync SHALL persist only locally owned user state and stable provider references. It SHALL NOT persist regular working times or full provider-owned detail snapshots in the local user store.

#### Scenario: Sync stores only a Personio reference
- **WHEN** sync resolves a Personio match for a user
- **THEN** it stores only the stable Personio identifier needed for later lookups
- **THEN** Personio-owned detail fields are not persisted on the User aggregate

#### Scenario: Sync does not persist regular working times
- **WHEN** sync processes ZEP employee data
- **THEN** it persists core user fields, employment periods, and release date
- **THEN** regular working times are not synchronized into local user persistence

### Requirement: Sync updates existing Users from ZEP data
The system SHALL update each User that matches an incoming ZEP employee with the latest locally owned user fields, roles, employment periods, and release date from the sync input.

#### Scenario: Existing user updated with fresh ZEP data
- **WHEN** a ZEP employee's username matches an existing User
- **THEN** the User is updated with the latest synced local fields, roles, employment periods, and release date
