## 1. Repository Port and Adapter

- [x] 1.1 Rename `findVisibleTasksForActor` → `findEmployeeVisibleTasks` in `MonthEndTaskRepository` port and update its query to `where task.monthValue = ?1 and task.subjectEmployeeId = ?2` (no join needed)
- [x] 1.2 Add `findLeadProjectTasks(UserId leadId, YearMonth month)` to `MonthEndTaskRepository` port
- [x] 1.3 Implement `findLeadProjectTasks` in `MonthEndTaskRepositoryAdapter` using the correlated EXISTS subquery
- [x] 1.4 Update `MonthEndTaskRepositoryAdapterTest`: fix the existing `findVisibleTasksForActor` test to use the renamed method and simplified semantics; add a test for `findLeadProjectTasks`

## 2. Employee Overview Use Case

- [x] 2.1 Rename `GetMonthEndStatusOverviewUseCase` port → `GetEmployeeMonthEndStatusOverviewUseCase`
- [x] 2.2 Rename `GetMonthEndStatusOverviewService` → `GetEmployeeMonthEndStatusOverviewService` and update it to use `findEmployeeVisibleTasks`

## 3. Lead Overview Use Case

- [x] 3.1 Add `GetProjectLeadMonthEndStatusOverviewUseCase` port
- [x] 3.2 Implement `GetProjectLeadMonthEndStatusOverviewService` using `findLeadProjectTasks`
- [x] 3.3 Add `GetProjectLeadMonthEndStatusOverviewServiceTest` unit test

## 4. REST Layer

- [x] 4.1 Add employee overview endpoint to `MonthEndEmployeeResource`, injecting `GetEmployeeMonthEndStatusOverviewUseCase`
- [x] 4.2 Add lead overview endpoint to `MonthEndProjectLeadResource`, injecting `GetProjectLeadMonthEndStatusOverviewUseCase`
- [x] 4.3 Remove the overview endpoint and `GetMonthEndStatusOverviewUseCase` injection from `MonthEndSharedResource`
- [x] 4.4 Update the OpenAPI spec / generated API interface for both role-specific resources

## 5. Spec Sync

- [x] 5.1 Sync delta spec: update `openspec/specs/monthend-status-overview/spec.md` with both employee and lead visibility requirements
- [x] 5.2 Sync delta spec: update `openspec/specs/monthend-rest-api/spec.md` to reflect role-specific overview endpoints and updated shared endpoint requirement
