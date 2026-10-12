## ADDED Requirements

### Requirement: Sync returns a result with operation counts
`SyncProjectsUseCase.sync()` SHALL return a `ProjectSyncResult` record instead of `void`. `ProjectSyncResult` SHALL contain integer fields: `created` (new Projects persisted) and `updated` (existing Projects whose mutable fields were changed). The `SyncScheduler` SHALL use these counts when composing its log output.

#### Scenario: Result reflects projects created during sync
- **WHEN** `SyncProjectsUseCase.sync()` creates N new Projects
- **THEN** the returned `ProjectSyncResult.created()` equals N

#### Scenario: Result reflects projects updated during sync
- **WHEN** `SyncProjectsUseCase.sync()` updates M existing Projects
- **THEN** the returned `ProjectSyncResult.updated()` equals M
