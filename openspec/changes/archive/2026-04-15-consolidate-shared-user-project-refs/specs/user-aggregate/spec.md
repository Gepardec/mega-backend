## MODIFIED Requirements

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
