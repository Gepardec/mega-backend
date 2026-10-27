# Spec Delta

## MODIFIED Requirements

### Requirement: User has a set of roles derived during sync
The system SHALL assign roles to users. The `Role` enum SHALL include `EMPLOYEE`, `OFFICE_MANAGEMENT`, `PROJECT_LEAD`, `ADMIN`, and `SYSTEM`. A user with `Role.SYSTEM` is the MEGA application actor and SHALL NOT be a human user. For users without `Role.SYSTEM`, `zepUsername` and `email` MUST NOT be null. For the system actor (`Role.SYSTEM`), `zepUsername` and `email` MAY be null. The system actor's `isActiveIn()` SHALL always return false because it has no employment periods.

Every synced human User SHALL have the `EMPLOYEE` role. The `OFFICE_MANAGEMENT` role SHALL be assigned if the User's email address appears in the configured office-management email list. The `ADMIN` role SHALL be assigned if the User's email address appears in the configured admin email list. The `PROJECT_LEAD` role MAY be managed by a separate lead-reconciliation capability.

#### Scenario: All synced users receive EMPLOYEE role
- **WHEN** a User is created or updated during sync
- **THEN** the User's roles set includes `EMPLOYEE`

#### Scenario: Office management role assigned from configured email
- **WHEN** a User's email address appears in the configured office-management email list
- **THEN** the User's roles set includes `OFFICE_MANAGEMENT`

#### Scenario: Admin role assigned from configured email
- **WHEN** a User's email address appears in the configured admin email list
- **THEN** after the next sync the User's roles set includes `ADMIN`

#### Scenario: Admin role removed when email leaves the admin list
- **WHEN** a User holding `ADMIN` is synced after their email address was removed from the configured admin email list
- **THEN** the User's roles set no longer includes `ADMIN`

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
