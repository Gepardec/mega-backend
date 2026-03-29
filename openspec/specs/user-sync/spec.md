# User Sync

## Purpose

Defines the `SyncUsersUseCase` and its `SyncUsersService` implementation within the hexagonal domain. The sync is responsible for keeping the `User` aggregate store in sync with ZEP (authoritative source for active employees) and Personio (best-effort enrichment), running automatically on a scheduled interval.

## Requirements

### Requirement: Sync runs automatically every 30 minutes
The system SHALL trigger the user sync use case automatically on a 30-minute interval via a unified `SyncScheduler` Quarkus `@Scheduled` adapter. The `SyncScheduler` SHALL sequence `SyncUsersUseCase`, `SyncProjectsUseCase`, and `ReconcileLeadsUseCase` in that order within the same scheduled invocation. The standalone `UserSyncScheduler` is removed. The `SyncScheduler` SHALL be the only trigger for all three sync steps — no manual or API-triggered sync is provided.

#### Scenario: Scheduler triggers all sync steps on interval
- **WHEN** 30 minutes have elapsed since the last sync
- **THEN** `SyncUsersUseCase.sync()` is called first
- **THEN** `SyncProjectsUseCase.sync()` is called second
- **THEN** `ReconcileLeadsUseCase.reconcile()` is called third

#### Scenario: ReconcileLeads is skipped if SyncProjects fails
- **WHEN** `SyncProjectsUseCase.sync()` throws an exception
- **THEN** `ReconcileLeadsUseCase.reconcile()` is NOT called in that cycle

### Requirement: Sync fetches all active employees from ZEP
The system SHALL fetch the complete list of employees from ZEP via `ZepEmployeePort.fetchAll()` at the start of each sync. The service SHALL then filter the result to only those profiles that have a non-null email AND an active `EmploymentPeriod` as of today (`EmploymentPeriods.active(LocalDate.now()).isPresent()`). This filtered list is the authoritative source for which Users should be active. The ZEP adapter SHALL wrap raw period and working-time lists into `EmploymentPeriods` and `RegularWorkingTimes` aggregates when constructing the `ZepProfile`.

#### Scenario: ZEP employees fetched at sync start
- **WHEN** `SyncUsersUseCase.sync()` is called
- **THEN** `ZepEmployeePort.fetchAll()` is called and returns a list of ZepProfile data

#### Scenario: Only profiles with active employment period and non-null email are processed
- **WHEN** `ZepEmployeePort.fetchAll()` returns profiles with mixed employment period states or null emails
- **THEN** only profiles where email is non-null AND `EmploymentPeriods.active(today).isPresent()` is true are included in the sync

#### Scenario: ZepProfile returned by adapter contains aggregate types
- **WHEN** `ZepEmployeeAdapter` maps a ZEP response to a `ZepProfile`
- **THEN** the `employmentPeriods` field is an `EmploymentPeriods` instance
- **THEN** the `regularWorkingTimes` field is a `RegularWorkingTimes` instance

### Requirement: Sync creates new Users for unknown ZEP employees
The system SHALL create a new `User` aggregate with a generated `UserId` for any ZEP employee whose username does not match an existing User in the repository.

#### Scenario: New ZEP employee results in new User
- **WHEN** a ZEP employee's username has no matching User in the repository
- **THEN** a new User is created with a generated UUID and saved to the repository

### Requirement: Sync updates existing Users from ZEP data
The system SHALL call `user.syncFromZep(zepProfile)` on each User that matches an incoming ZEP employee, updating their ZEP-sourced fields.

#### Scenario: Existing user updated with fresh ZEP data
- **WHEN** a ZEP employee's username matches an existing User
- **THEN** `user.syncFromZep(zepProfile)` is called with the latest ZEP data

### Requirement: Sync enriches Users with Personio data on a best-effort basis
The system SHALL attempt to fetch a `PersonioProfile` for each User by calling `PersonioEmployeePort.findByEmail(email)`. If Personio returns data, the system SHALL call `user.syncFromPersonio(personioProfile)`. If Personio returns no data or is unavailable, the system SHALL preserve the User's existing `personioProfile` without error.

#### Scenario: Personio data available for user
- **WHEN** `PersonioEmployeePort.findByEmail(email)` returns a PersonioProfile
- **THEN** `user.syncFromPersonio(personioProfile)` is called

#### Scenario: Personio data unavailable for user
- **WHEN** `PersonioEmployeePort.findByEmail(email)` returns empty
- **THEN** the User's existing `personioProfile` is preserved
- **THEN** no exception is thrown and sync continues for remaining users

### Requirement: Sync deactivates Users absent from ZEP or without an active employment period
The system SHALL set the status of any existing `ACTIVE` User to `INACTIVE` if their ZEP username is not present in the filtered (active employment period) sync response. This covers both complete removal from ZEP and employees whose employment period has ended.

#### Scenario: User deactivated when removed from ZEP
- **WHEN** a User with `ACTIVE` status has a ZEP username not present in the current ZEP response
- **THEN** the User's status is updated to `INACTIVE` and saved

#### Scenario: User deactivated when employment period has ended
- **WHEN** a User's ZEP profile has no active employment period as of today
- **THEN** the User's status is updated to `INACTIVE` and saved

#### Scenario: User deactivated when ZEP profile has no email
- **WHEN** a User's ZEP profile has a null email
- **THEN** the User's status is updated to `INACTIVE` and saved

### Requirement: Sync persists all changes atomically
The system SHALL persist all User changes (creates, updates, deactivations) via `UserRepository.saveAll()` after processing the full ZEP and Personio responses.

#### Scenario: All user changes saved at end of sync
- **WHEN** the sync has processed all ZEP employees and Personio enrichments
- **THEN** all modified and created Users are persisted via `UserRepository.saveAll()`

### Requirement: Use case is decoupled from Quarkus infrastructure
The `SyncUsersUseCase` interface and its `SyncUsersService` implementation SHALL NOT import or depend on any Quarkus, CDI, or JPA annotations. Configuration SHALL be passed via the `UserSyncConfig` record. The Quarkus scheduler SHALL call the use case through the inbound port interface only.

#### Scenario: SyncUsersService has no Quarkus imports in domain/application layer
- **WHEN** `SyncUsersService` is compiled
- **THEN** it imports only from `com.gepardec.mega.hexagon`, `java.*`, and standard libraries
