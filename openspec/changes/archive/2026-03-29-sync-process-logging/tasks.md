## 1. Result Records

- [x] 1.1 Create `UserSyncResult` record in the user domain (fields: `added`, `updated`, `disabled`)
- [x] 1.2 Create `ProjectSyncResult` record in the project domain (fields: `created`, `updated`)
- [x] 1.3 Create `ReconcileLeadsResult` record in the reconcile-leads domain (fields: `resolved`, `skipped`, `rolesAdded`, `rolesRevoked`)

## 2. Use Case Interface Changes

- [x] 2.1 Change `SyncUsersUseCase.sync()` return type from `void` to `UserSyncResult`
- [x] 2.2 Change `SyncProjectsUseCase.sync()` return type from `void` to `ProjectSyncResult`
- [x] 2.3 Change `ReconcileLeadsUseCase.reconcile()` return type from `void` to `ReconcileLeadsResult`

## 3. Service Implementations

- [x] 3.1 Update `SyncUsersService` to accumulate `added`, `updated`, `disabled` counters and return `UserSyncResult`
- [x] 3.2 Update `SyncProjectsService` to accumulate `created`, `updated` counters and return `ProjectSyncResult`
- [x] 3.3 Update `ReconcileLeadsService` to accumulate `resolved`, `skipped`, `rolesAdded`, `rolesRevoked` counters and return `ReconcileLeadsResult`

## 4. SyncScheduler Adapter

- [x] 4.1 Add `Instant`-based timing around each use case call in `SyncScheduler`
- [x] 4.2 Log `INFO` line for user-sync step: step name, `added`, `updated`, `disabled`, elapsed ms
- [x] 4.3 Log `INFO` line for project-sync step: step name, `created`, `updated`, elapsed ms
- [x] 4.4 Log `INFO` line for reconcile-leads step: step name, `resolved`, `skipped`, `rolesAdded`, `rolesRevoked`, elapsed ms
- [x] 4.5 Log `INFO` line for total cycle duration after all steps complete (or partial failure)

## 5. Tests

- [x] 5.1 Unit test `SyncUsersService`: verify correct counts in `UserSyncResult` for add/update/disable scenarios
- [x] 5.2 Unit test `SyncProjectsService`: verify correct counts in `ProjectSyncResult` for create/update scenarios
- [x] 5.3 Unit test `ReconcileLeadsService`: verify correct counts in `ReconcileLeadsResult` for resolved/skipped/role-change scenarios
- [x] 5.4 Update existing `SyncScheduler` tests to handle non-void return types from use cases
