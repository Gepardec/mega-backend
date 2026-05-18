## ADDED Requirements

### Requirement: User aggregate carries a release date
The `User` aggregate SHALL store a nullable release date representing the date through which the employee's timesheet data has been approved in ZEP. The release date SHALL be locally persisted so it can be read without calling ZEP.

#### Scenario: User with a known release date exposes it
- **WHEN** a User has had their release date set
- **THEN** the User's release date is available from the local repository without an external call

#### Scenario: User with no release date has a null value
- **WHEN** a User has never had a release date set
- **THEN** the User's release date is null and no error is raised

### Requirement: Release date is written to ZEP and immediately reflected locally
When an employee's release date is updated, the system SHALL write it to ZEP first. On success, the system SHALL also persist the new release date locally so that subsequent reads reflect the update without waiting for the next sync. If the ZEP write fails, the local value SHALL remain unchanged.

#### Scenario: Successful ZEP write is reflected locally
- **WHEN** a release date update for an employee succeeds in ZEP
- **THEN** the employee's locally stored release date is updated to the new value

#### Scenario: Failed ZEP write leaves local value unchanged
- **WHEN** a release date update for an employee fails in ZEP
- **THEN** the employee's locally stored release date is not modified

### Requirement: ZEP release date writes are scoped to a single field
When writing a release date to ZEP, the system SHALL send only the release date — no other employee fields. This prevents accidental overwrites of unrelated ZEP data.

#### Scenario: ZEP write contains only the release date
- **WHEN** a release date update is sent to ZEP
- **THEN** the request body contains only the release date field

### Requirement: User BC queries month-end task completion via an outbound port
The user BC SHALL access month-end task completion state through a dedicated outbound port that returns the set of employee IDs for whom all tasks in a given month are complete. The port contract SHALL use only shared domain types and SHALL NOT couple the user BC to monthend internals.

#### Scenario: Port returns employees with all tasks done
- **WHEN** the port is queried for a given month
- **THEN** only employees whose every task for that month is complete are returned

#### Scenario: Port excludes employees with open tasks
- **WHEN** an employee has at least one open task in the queried month
- **THEN** that employee is not included in the result
