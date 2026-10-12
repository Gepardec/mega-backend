## 1. Domain Port

- [x] 1.1 Rename `findTasksForActor` to `findVisibleTasksForActor` on `MonthEndTaskRepository` port interface

## 2. Domain Read Model

- [x] 2.1 Add `canComplete: boolean` field to `MonthEndStatusOverviewItem` record

## 3. Repository Adapter

- [x] 3.1 Rename `findTasksForActor` to `findVisibleTasksForActor` in `MonthEndTaskRepositoryAdapter`
- [x] 3.2 Update the JPQL query to use `LEFT JOIN` and add `OR task.subjectEmployeeId = ?2` so subject-only tasks are returned

## 4. Application Service

- [x] 4.1 Update `GetMonthEndStatusOverviewService` to call `findVisibleTasksForActor` instead of `findTasksForActor`
- [x] 4.2 Compute `canComplete = task.eligibleActorIds().contains(actorId)` and pass it to the overview item mapper

## 5. Overview Mapper

- [x] 5.1 Update `MonthEndStatusOverviewMapper` to accept and map `canComplete` into `MonthEndStatusOverviewItem`

## 6. OpenAPI Contract

- [x] 6.1 Add required `canComplete: boolean` field to `MonthEndStatusOverviewEntry` schema in `monthend.openapi.yaml`
- [x] 6.2 Regenerate Java API interfaces and response models from the updated contract

## 7. REST Mapper

- [x] 7.1 Update `MonthEndRestMapper` to map `canComplete` from `MonthEndStatusOverviewItem` to the generated `MonthEndStatusOverviewEntry` response model

## 8. Tests

- [x] 8.1 Update `GetMonthEndStatusOverviewService` unit test: verify `canComplete = true` for eligible actor entries
- [x] 8.2 Add unit test: verify `canComplete = false` for subject-only entries (e.g., `PROJECT_LEAD_REVIEW` where actor is subject but not eligible)
- [x] 8.3 Add unit test: verify subject-employee tasks appear in overview when actor is not eligible (new visibility requirement)
- [x] 8.4 Add unit test: verify `canComplete = true` when actor is both subject and eligible (eligible always wins)
- [x] 8.5 Update REST mapper test to assert `canComplete` is mapped correctly for both `true` and `false` cases
- [x] 8.6 Update or add endpoint integration test (`@QuarkusTest`) verifying `canComplete` appears in the status overview response JSON
