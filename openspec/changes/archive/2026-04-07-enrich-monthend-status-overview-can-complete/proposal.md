## Why

The month-end status overview currently surfaces only tasks where the authenticated actor is eligible to complete them. Employees need to also see tasks they are the **subject** of (e.g., a `PROJECT_LEAD_REVIEW` about them) so they have a full picture of their month-end status — even for checks they cannot act on themselves.

## What Changes

- Extend the status overview query to return tasks where the actor is eligible **OR** is the subject employee, not only eligible tasks.
- Add `canComplete: boolean` to each overview entry so the frontend can distinguish between tasks the actor can act on (complete) and tasks they can only observe.
- Rename `findTasksForActor` → `findVisibleTasksForActor` on `MonthEndTaskRepository` and its adapter to reflect the broader scope.
- Update the repository query to also match tasks where `subjectEmployeeId = actorId`.
- Compute `canComplete` in `GetMonthEndStatusOverviewService` as `task.eligibleActorIds().contains(actorId)`.
- **BREAKING** Add required `canComplete` field to `MonthEndStatusOverviewEntry` in the OpenAPI spec.

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `monthend-status-overview`: overview now includes tasks where actor is the subject employee; each entry exposes `canComplete`.
- `monthend-rest-api`: `MonthEndStatusOverviewEntry` gains a required `canComplete: boolean` field.

## Impact

- `MonthEndTaskRepository` port and `MonthEndTaskRepositoryAdapter` (renamed method + updated query).
- `MonthEndStatusOverviewItem` domain read model (new `canComplete` field).
- `GetMonthEndStatusOverviewService` (passes `canComplete` to mapper).
- `MonthEndStatusOverviewMapper` (maps `canComplete`).
- `MonthEndRestMapper` (passes `canComplete` through to response).
- `monthend.openapi.yaml` (`MonthEndStatusOverviewEntry` schema).
- Tests for service, REST mapper, and endpoint covering the `canComplete` field and subject-employee visibility.
