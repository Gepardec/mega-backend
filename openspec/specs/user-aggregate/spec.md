# User Aggregate

## Purpose

Defines the `User` aggregate root within the hexagonal domain. A User represents an employee with a stable internal identity, locally owned user data, stable references to external providers, employment-period-based activity, and sync-derived roles.

## Requirements

### Requirement: User aggregate stores locally owned user data and stable provider references
The system SHALL model the hexagon `User` aggregate around locally owned data: internal `UserId`, `Email`, `FullName`, `ZepUsername`, nullable `PersonioId`, `EmploymentPeriods`, and a set of roles. `ZepUsername` and `PersonioId` SHALL be modeled as dedicated value objects. The aggregate SHALL NOT embed full ZEP or Personio profile snapshots as persisted state.

#### Scenario: New user created from synced provider references
- **WHEN** a new User is created from synced provider data
- **THEN** the User stores the synced ZEP username as a `ZepUsername` value object
- **THEN** the User stores a nullable `PersonioId` reference without embedding full provider detail objects

#### Scenario: Existing user reconstituted from local persistence
- **WHEN** a User is reconstituted from the repository
- **THEN** the aggregate is restored from local identity fields, employment periods, roles, and stable provider references
- **THEN** no full ZEP or Personio profile snapshot is required to rebuild the User

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
The system SHALL assign roles to each User during sync. Every synced User SHALL have the `EMPLOYEE` role. The `OFFICE_MANAGEMENT` role SHALL be assigned if the User's email address appears in the configured office-management email list. The `PROJECT_LEAD` role MAY be managed by a separate lead-reconciliation capability.

#### Scenario: All synced users receive EMPLOYEE role
- **WHEN** a User is created or updated during sync
- **THEN** the User's roles set includes `EMPLOYEE`

#### Scenario: Office management role assigned from configured email
- **WHEN** a User's email address appears in the configured office-management email list
- **THEN** the User's roles set includes `OFFICE_MANAGEMENT`

#### Scenario: Username-only config match does not assign office management
- **WHEN** a configured office-management entry does not match the User's email address
- **THEN** the User does not receive `OFFICE_MANAGEMENT` from that config entry
