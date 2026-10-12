## 1. Project Aggregate Reshape

- [x] 1.1 Convert `Project` to an immutable record with native record accessors, typed `UserId` leads, and state-transition methods for ZEP sync and lead replacement
- [x] 1.2 Update project persistence mapping and repository code to translate `UserId` leads to the existing UUID-based schema without introducing a database migration
- [x] 1.3 Update downstream project consumers in monthend, worktime, and test fixtures to use the reshaped aggregate API

## 2. Application-Service Alignment

- [x] 2.1 Refactor `SyncProjectsService` into a CDI-managed transactional application service that derives immutable project updates and persists only created or changed projects
- [x] 2.2 Refactor `SyncProjectLeadsService` into a CDI-managed transactional application service that derives immutable lead updates and role reconciliation from the resolved lead sets
- [x] 2.3 Update `SyncScheduler` to inject `SyncProjectsUseCase` and `SyncProjectLeadsUseCase` instead of manually constructing project services

## 3. Verification

- [x] 3.1 Rewrite project domain and application tests to match the record-oriented model and direct record accessor usage
- [x] 3.2 Run targeted tests for the `project`, `monthend`, and `worktime` slices affected by the reshape
