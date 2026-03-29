## Why

The `SyncScheduler` runs every 30 minutes orchestrating user sync, project sync, and lead reconciliation, but produces no structured output — making it impossible to tell from logs how many records changed, whether a run was a no-op, or how long each step took. This makes troubleshooting and monitoring the sync process unnecessarily difficult.

## What Changes

- Each sync use case (`SyncUsersUseCase`, `SyncProjectsUseCase`, `ReconcileLeadsUseCase`) returns a typed result object containing operation counts (added, updated, disabled/unchanged)
- `SyncScheduler` records wall-clock time for each step and logs a structured summary at the end of each cycle
- Log output covers: users added/updated/disabled, projects created/updated, leads resolved/skipped, and elapsed time per step and total

## Capabilities

### New Capabilities

_(none — all changes extend existing sync capabilities)_

### Modified Capabilities

- `user-sync`: `SyncUsersUseCase.sync()` now returns a `UserSyncResult` with counts of users added, updated, and disabled
- `project-sync`: `SyncProjectsUseCase.sync()` now returns a `ProjectSyncResult` with counts of projects created and updated
- `reconcile-leads`: `ReconcileLeadsUseCase.reconcile()` now returns a `ReconcileLeadsResult` with counts of leads resolved, skipped, and users whose `PROJECT_LEAD` role changed

## Impact

- `SyncUsersUseCase`, `SyncProjectsUseCase`, `ReconcileLeadsUseCase` interfaces — return types change from `void` to result records
- `SyncUsersService`, `SyncProjectsService`, `ReconcileLeadsService` implementations — must accumulate and return counts
- `SyncScheduler` adapter — consumes result objects and emits structured log lines with timing
- No external API changes; no persistence schema changes
