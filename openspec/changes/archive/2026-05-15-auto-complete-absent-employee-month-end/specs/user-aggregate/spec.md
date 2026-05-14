## MODIFIED Requirements

### Requirement: User has a set of roles derived during sync
The system SHALL assign roles to users. The `Role` enum SHALL include `EMPLOYEE`, `OFFICE_MANAGEMENT`, `PROJECT_LEAD`, and `SYSTEM`. A user with `Role.SYSTEM` is the MEGA application actor and SHALL NOT be a human user. For users without `Role.SYSTEM`, `zepUsername` and `email` MUST NOT be null. For the system actor (`Role.SYSTEM`), `zepUsername` and `email` MAY be null. The system actor's `isActiveIn()` SHALL always return false because it has no employment periods.

#### Scenario: Regular user has mandatory zepUsername and email
- **WHEN** a `User` is constructed without `Role.SYSTEM`
- **THEN** construction fails if `zepUsername` or `email` is null

#### Scenario: System actor may have null zepUsername and email
- **WHEN** a `User` is constructed with only `Role.SYSTEM` and no `zepUsername` or `email`
- **THEN** construction succeeds

#### Scenario: System actor is never active in any month
- **WHEN** `isActiveIn(month)` is called on the system actor user
- **THEN** it returns false
