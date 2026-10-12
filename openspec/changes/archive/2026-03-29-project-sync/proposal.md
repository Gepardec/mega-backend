## Why

The hexagonal backend currently syncs users from ZEP and Personio but has no equivalent for projects. Without projects in the hexagonal database, the approval workflow cannot reference project leads, and the `PROJECT_LEAD` role cannot be assigned to users.

## What Changes

- New `hexagon/project/` domain with its own ports, adapters, and domain model
- New `SyncProjectsUseCase` — upserts project master data from ZEP into `hexagon_projects`
- New `ReconcileLeadsUseCase` — resolves ZEP lead usernames to user IDs and writes the `hexagon_project_leads` join table; also sets the `PROJECT_LEAD` role on the user aggregate
- **BREAKING**: `UserSyncScheduler` is replaced by a unified `SyncScheduler` that sequences all three steps (sync users → sync projects → reconcile leads) every 30 minutes

## Capabilities

### New Capabilities

- `project-aggregate`: The `Project` domain aggregate covering identity, master data (name, zepId, dates), and its leads as `UserId` references
- `project-sync`: Use case that fetches all active projects from ZEP and upserts them into the local database (no lead data)
- `reconcile-leads`: Use case that fetches lead assignments per project from ZEP, resolves usernames to `UserId` values, and persists the project↔user relationship; also triggers `PROJECT_LEAD` role assignment on the user aggregate

### Modified Capabilities

- `user-sync`: `UserSyncScheduler` is removed; user sync is now triggered by the unified `SyncScheduler`. The `PROJECT_LEAD` role (previously deferred) is now set by `ReconcileLeadsUseCase` after project data is available.

## Impact

- New Liquibase migration: `hexagon_projects` table and `hexagon_project_leads` join table (FK to `hexagon_users`)
- `hexagon/user/adapter/inbound/UserSyncScheduler.java` is deleted
- New `SyncScheduler` introduced outside the user domain (e.g. `application/schedule/` or `hexagon/adapter/inbound/`)
- Depends on `hexagon_users` being populated before `ReconcileLeadsUseCase` runs (ordering guaranteed by sequential execution in `SyncScheduler`)
- ZEP REST client: reuses existing `ZepProjectRestClient` and `ProjectService` for project list and per-project employee fetch
