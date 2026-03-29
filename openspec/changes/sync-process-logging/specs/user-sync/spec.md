## MODIFIED Requirements

### Requirement: Sync runs automatically every 30 minutes
The system SHALL trigger the user sync use case automatically on a 30-minute interval via a unified `SyncScheduler` Quarkus `@Scheduled` adapter. The `SyncScheduler` SHALL sequence `SyncUsersUseCase`, `SyncProjectsUseCase`, and `ReconcileLeadsUseCase` in that order within the same scheduled invocation. The standalone `UserSyncScheduler` is removed. The `SyncScheduler` SHALL be the only trigger for all three sync steps — no manual or API-triggered sync is provided.

For each use case invocation, `SyncScheduler` SHALL record the wall-clock time before and after the call using `Instant.now()` and compute the elapsed duration. After each step and after the full cycle, `SyncScheduler` SHALL emit structured `INFO` log lines reporting per-step operation counts and elapsed time.

#### Scenario: Scheduler triggers all sync steps on interval
- **WHEN** 30 minutes have elapsed since the last sync
- **THEN** `SyncUsersUseCase.sync()` is called first
- **THEN** `SyncProjectsUseCase.sync()` is called second
- **THEN** `ReconcileLeadsUseCase.reconcile()` is called third

#### Scenario: ReconcileLeads is skipped if SyncProjects fails
- **WHEN** `SyncProjectsUseCase.sync()` throws an exception
- **THEN** `ReconcileLeadsUseCase.reconcile()` is NOT called in that cycle

#### Scenario: Scheduler logs per-step summary after each step
- **WHEN** a sync step completes successfully
- **THEN** `SyncScheduler` logs an `INFO` line containing the step name, its operation counts, and elapsed time in milliseconds

#### Scenario: Scheduler logs total cycle duration after all steps complete
- **WHEN** all sync steps in a cycle have finished (successfully or with a partial failure)
- **THEN** `SyncScheduler` logs an `INFO` line containing the total elapsed time for the full cycle in milliseconds

## ADDED Requirements

### Requirement: Sync returns a result with operation counts
`SyncUsersUseCase.sync()` SHALL return a `UserSyncResult` record instead of `void`. `UserSyncResult` SHALL contain integer fields: `added` (new Users created), `updated` (existing Users whose ZEP or Personio data was applied), and `disabled` (Users set to `INACTIVE`). The `SyncScheduler` SHALL use these counts when composing its log output.

#### Scenario: Result reflects users added during sync
- **WHEN** `SyncUsersUseCase.sync()` creates N new Users
- **THEN** the returned `UserSyncResult.added()` equals N

#### Scenario: Result reflects users updated during sync
- **WHEN** `SyncUsersUseCase.sync()` calls `syncFromZep` or `syncFromPersonio` on M existing Users
- **THEN** the returned `UserSyncResult.updated()` equals M

#### Scenario: Result reflects users disabled during sync
- **WHEN** `SyncUsersUseCase.sync()` sets K Users to INACTIVE
- **THEN** the returned `UserSyncResult.disabled()` equals K
