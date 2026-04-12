# User Sync

## Purpose

Defines the `SyncUsersUseCase` and its `SyncUsersService` implementation within the hexagonal domain. The sync keeps the local `User` aggregate store aligned with ZEP employee identity and employment history, best-effort Personio references, and scheduler-driven operational reporting.

## Requirements

### Requirement: Sync runs automatically every 30 minutes
The system SHALL trigger the user sync use case automatically on a 30-minute interval via a unified `SyncScheduler` Quarkus `@Scheduled` adapter. The `SyncScheduler` SHALL sequence `SyncUsersUseCase`, `SyncProjectsUseCase`, and `SyncProjectLeadsUseCase` in that order within the same scheduled invocation. The standalone `UserSyncScheduler` is removed. The `SyncScheduler` SHALL be the only trigger for all three sync steps and no manual or API-triggered sync is provided.

For each use case invocation, `SyncScheduler` SHALL record the wall-clock time before and after the call using `Instant.now()` and compute the elapsed duration. After each step and after the full cycle, `SyncScheduler` SHALL emit structured `INFO` log lines reporting per-step operation counts and elapsed time.

#### Scenario: Scheduler triggers all sync steps on interval
- **WHEN** 30 minutes have elapsed since the last sync
- **THEN** `SyncUsersUseCase.sync()` is called first
- **THEN** `SyncProjectsUseCase.sync()` is called second
- **THEN** `SyncProjectLeadsUseCase.sync()` is called third

#### Scenario: ReconcileLeads is skipped if SyncProjects fails
- **WHEN** `SyncProjectsUseCase.sync()` throws an exception
- **THEN** `SyncProjectLeadsUseCase.sync()` is NOT called in that cycle

#### Scenario: Scheduler logs per-step summary after each step
- **WHEN** a sync step completes successfully
- **THEN** `SyncScheduler` logs an `INFO` line containing the step name, its operation counts, and elapsed time in milliseconds

#### Scenario: Scheduler logs total cycle duration after all steps complete
- **WHEN** all sync steps in a cycle have finished successfully or with a partial failure
- **THEN** `SyncScheduler` logs an `INFO` line containing the total elapsed time for the full cycle in milliseconds

### Requirement: Sync fetches all employees from ZEP
The system SHALL fetch the complete list of employees from ZEP via `ZepEmployeePort.fetchAll()` at the start of each sync. The service SHALL then filter the result only to profiles that have a non-null email address. This filtered list is the authoritative source for which users are created or updated in local persistence, including employees whose employment periods are no longer active. The ZEP sync input SHALL include typed employment-period data required for local history persistence.

#### Scenario: ZEP employees fetched at sync start
- **WHEN** `SyncUsersUseCase.sync()` is called
- **THEN** `ZepEmployeePort.fetchAll()` is called and returns a list of ZEP employee sync data

#### Scenario: Only profiles with non-null email are processed
- **WHEN** `ZepEmployeePort.fetchAll()` returns profiles with mixed email availability
- **THEN** only profiles where email is non-null are included in the sync input

#### Scenario: Inactive employment period does not exclude user from sync
- **WHEN** a fetched employee has a non-null email but no employment period active today
- **THEN** that employee is still included in the sync input

### Requirement: Sync retains users with historical employment periods
The system SHALL synchronize every ZEP employee with a non-null email address, regardless of whether that employee has an active employment period today. The sync SHALL persist the full employment-period history returned by ZEP so downstream use cases can evaluate activity for current and historical dates from local data.

#### Scenario: Employee with ended employment remains synchronized
- **WHEN** the ZEP sync response contains an employee with a non-null email and only past employment periods
- **THEN** the employee is still created or updated in the local user store
- **THEN** the ended employment periods are persisted for later date-based activity checks

### Requirement: Sync creates new Users for unknown ZEP employees
The system SHALL create a new `User` aggregate with a generated `UserId` for any ZEP employee whose username does not match an existing User in the repository by stored ZEP username.

#### Scenario: New ZEP employee results in new User
- **WHEN** a ZEP employee's username has no matching User in the repository
- **THEN** a new User is created with a generated UUID and saved to the repository

### Requirement: Sync updates existing Users from ZEP data
The system SHALL update each User that matches an incoming ZEP employee with the latest locally owned user fields, roles, and employment periods from the sync input.

#### Scenario: Existing user updated with fresh ZEP data
- **WHEN** a ZEP employee's username matches an existing User
- **THEN** the User is updated with the latest synced local fields, roles, and employment periods

### Requirement: Sync enriches Users with Personio data on a best-effort basis
The system SHALL use Personio during routine sync only to resolve a stable Personio reference for Users that do not already have one. It SHALL attempt lookup by email, store the resolved Personio identifier when available, and skip the Personio lookup for Users whose Personio identifier is already known. If Personio returns no match or is unavailable, sync SHALL continue without error.

#### Scenario: Missing Personio reference resolved during sync
- **WHEN** a synced User does not yet have a stored Personio identifier and Personio lookup by email succeeds
- **THEN** the User stores the resolved Personio identifier

#### Scenario: Existing Personio reference skips sync-time lookup
- **WHEN** a synced User already has a stored Personio identifier
- **THEN** routine user sync does not call Personio again for that User

#### Scenario: Missing Personio match does not fail sync
- **WHEN** Personio lookup by email returns no match or fails for a User without a stored Personio identifier
- **THEN** sync continues for the remaining Users without throwing an error

### Requirement: Sync persists only local user state and stable provider references
Routine user sync SHALL persist only locally owned user state and stable provider references. It SHALL NOT persist regular working times or full provider-owned detail snapshots in the local user store.

#### Scenario: Sync stores only a Personio reference
- **WHEN** sync resolves a Personio match for a user
- **THEN** it stores only the stable Personio identifier needed for later lookups
- **THEN** Personio-owned detail fields are not persisted on the User aggregate

#### Scenario: Sync does not persist regular working times
- **WHEN** sync processes ZEP employee data
- **THEN** it persists core user fields and employment periods
- **THEN** regular working times are not synchronized into local user persistence

### Requirement: Sync persists all changes atomically
The system SHALL persist all User changes via one transactional CDI-managed application-service boundary after processing the full ZEP response and any selective Personio lookups.

#### Scenario: All user changes saved at end of sync
- **WHEN** sync has processed all ZEP employees and selective Personio enrichments
- **THEN** all modified and created Users are persisted within one transactional sync invocation

### Requirement: Sync returns a result with operation counts
`SyncUsersUseCase.sync()` SHALL return a `UserSyncResult` record containing integer fields `added`, `updated`, `unchanged`, `skippedNoEmail`, and `personioLinked`. `skippedNoEmail` counts ZEP employees that were not processed because no email address was available. `personioLinked` counts users that received a newly resolved Personio reference during the run and MAY overlap with `added` or `updated`. The scheduler SHALL use these counts when composing its log output.

#### Scenario: Result reflects users added during sync
- **WHEN** `SyncUsersUseCase.sync()` creates N new Users
- **THEN** the returned `UserSyncResult.added()` equals N

#### Scenario: Result reflects users updated during sync
- **WHEN** `SyncUsersUseCase.sync()` updates M existing Users with synced local data or a newly resolved Personio identifier
- **THEN** the returned `UserSyncResult.updated()` equals M

#### Scenario: Result reflects unchanged users
- **WHEN** `SyncUsersUseCase.sync()` evaluates U existing Users whose persisted state does not change
- **THEN** the returned `UserSyncResult.unchanged()` equals U

#### Scenario: Result reflects users skipped without email
- **WHEN** `SyncUsersUseCase.sync()` encounters K ZEP employees without an email address
- **THEN** the returned `UserSyncResult.skippedNoEmail()` equals K

#### Scenario: Result reflects newly linked Personio references
- **WHEN** `SyncUsersUseCase.sync()` resolves P previously missing Personio references
- **THEN** the returned `UserSyncResult.personioLinked()` equals P
