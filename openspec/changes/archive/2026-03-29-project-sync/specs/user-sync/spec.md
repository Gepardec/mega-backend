## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: UserSyncScheduler is the sole scheduler for user sync
**Reason**: Replaced by the unified `SyncScheduler` that also drives `SyncProjectsUseCase` and `ReconcileLeadsUseCase`. The standalone `UserSyncScheduler` class is deleted.
**Migration**: `SyncScheduler` in `application/schedule/` calls `SyncUsersUseCase.sync()` as its first step, preserving all existing sync behavior.
