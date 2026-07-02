# User Aggregate

## Purpose

Defines the `User` aggregate root within the hexagonal domain. A User represents an employee with a stable internal identity, locally owned user data, stable references to external providers, employment-period-based activity, and sync-derived roles.

## Requirements

### Requirement: User aggregate stores locally owned user data and stable provider references
The system SHALL model the hexagon `User` aggregate around locally owned data: internal `UserId`, `Email`, `FullName`, `ZepUsername`, nullable `PersonioId`, `EmploymentPeriods`, and a set of roles. `ZepUsername` and `PersonioId` SHALL be modeled as dedicated value objects. `FullName` and `ZepUsername` SHALL reside in `shared/domain/model/` rather than `user/domain/model/`, as they are shared kernel types used across modules. The aggregate SHALL NOT embed full ZEP or Personio profile snapshots as persisted state.

#### Scenario: New user created from synced provider references
- **WHEN** a new User is created from synced provider data
- **THEN** the User stores the synced ZEP username as a `ZepUsername` value object from `shared/domain/model/`
- **THEN** the User stores a nullable `PersonioId` reference without embedding full provider detail objects

#### Scenario: Existing user reconstituted from local persistence
- **WHEN** a User is reconstituted from the repository
- **THEN** the aggregate is restored from local identity fields, employment periods, roles, and stable provider references
- **THEN** no full ZEP or Personio profile snapshot is required to rebuild the User

#### Scenario: FullName and ZepUsername imported from shared kernel
- **WHEN** any class in any module references `FullName` or `ZepUsername`
- **THEN** the import originates from `com.gepardec.mega.hexagon.shared.domain.model`
- **THEN** no copy of `FullName` or `ZepUsername` exists in `user/domain/model/`

### Requirement: User has a stable domain identity
The system SHALL assign each User a UUID generated internally (`UserId`) that is independent of any external system identifier. ZEP username and nullable Personio ID SHALL be stored as `ZepUsername` and `PersonioId` value objects on the User aggregate and SHALL NOT replace the User's internal identity.

#### Scenario: New user created from ZEP data
- **WHEN** a ZEP employee is encountered that has no matching User in the repository
- **THEN** a new User is created with a freshly generated UUID as its `UserId`
- **THEN** the ZEP username is stored as a `ZepUsername` value object and not as the User's primary identity

#### Scenario: Existing user matched by ZEP username
- **WHEN** a ZEP employee's username matches an existing User's stored ZEP username
- **THEN** the existing User is updated rather than a new one being created
- **THEN** the User's `UserId` remains unchanged

#### Scenario: Personio reference added without changing domain identity
- **WHEN** a synced User receives a Personio identifier
- **THEN** the User stores that identifier as a `PersonioId` value object
- **THEN** the User's `UserId` remains unchanged

### Requirement: User activity is derived from employment periods
The system SHALL derive whether a User is active for a given date or payroll month from the User's persisted `EmploymentPeriods`. The system SHALL NOT require a separately persisted active/inactive status on the User aggregate.

#### Scenario: User is active when an employment period covers the reference date
- **WHEN** a User has an employment period active on the queried date
- **THEN** the User is treated as active for that date

#### Scenario: User is inactive when no employment period covers the reference month
- **WHEN** a User has no employment period active during the queried payroll month
- **THEN** the User is treated as inactive for that payroll month

### Requirement: User has a set of roles derived during sync
The system SHALL assign roles to users. The `Role` enum SHALL include `EMPLOYEE`, `OFFICE_MANAGEMENT`, `PROJECT_LEAD`, and `SYSTEM`. A user with `Role.SYSTEM` is the MEGA application actor and SHALL NOT be a human user. For users without `Role.SYSTEM`, `zepUsername` and `email` MUST NOT be null. For the system actor (`Role.SYSTEM`), `zepUsername` and `email` MAY be null. The system actor's `isActiveIn()` SHALL always return false because it has no employment periods.

Every synced human User SHALL have the `EMPLOYEE` role. The `OFFICE_MANAGEMENT` role SHALL be assigned if the User's email address appears in the configured office-management email list. The `PROJECT_LEAD` role MAY be managed by a separate lead-reconciliation capability.

#### Scenario: All synced users receive EMPLOYEE role
- **WHEN** a User is created or updated during sync
- **THEN** the User's roles set includes `EMPLOYEE`

#### Scenario: Office management role assigned from configured email
- **WHEN** a User's email address appears in the configured office-management email list
- **THEN** the User's roles set includes `OFFICE_MANAGEMENT`

#### Scenario: Username-only config match does not assign office management
- **WHEN** a configured office-management entry does not match the User's email address
- **THEN** the User does not receive `OFFICE_MANAGEMENT` from that config entry

#### Scenario: Regular user has mandatory zepUsername and email
- **WHEN** a `User` is constructed without `Role.SYSTEM`
- **THEN** construction fails if `zepUsername` or `email` is null

#### Scenario: System actor may have null zepUsername and email
- **WHEN** a `User` is constructed with only `Role.SYSTEM` and no `zepUsername` or `email`
- **THEN** construction succeeds

#### Scenario: System actor is never active in any month
- **WHEN** `isActiveIn(month)` is called on the system actor user
- **THEN** it returns false

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

### Requirement: User external classification is derived from ZEP username prefix
The system SHALL classify a `User` as external if their `ZepUsername` starts with the letter "e". This classification SHALL be a pure derivation from the stored `ZepUsername` value — no additional field is persisted. The `ZepUsername` value object SHALL expose an `isExternal()` predicate. The `User` aggregate SHALL expose an `isExternal()` convenience predicate that delegates to its `ZepUsername`.

#### Scenario: User with ZEP username starting with "e" is external
- **WHEN** a `User` has a `ZepUsername` whose value begins with the letter "e"
- **THEN** `User.isExternal()` returns `true`

#### Scenario: User with ZEP username not starting with "e" is internal
- **WHEN** a `User` has a `ZepUsername` whose value does not begin with the letter "e"
- **THEN** `User.isExternal()` returns `false`

#### Scenario: External classification is case-sensitive
- **WHEN** a `User` has a `ZepUsername` whose value begins with the uppercase letter "E"
- **THEN** `User.isExternal()` returns `false`

