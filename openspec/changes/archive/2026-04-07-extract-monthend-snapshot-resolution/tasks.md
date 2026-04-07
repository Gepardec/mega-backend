## 1. Extract shared snapshot resolution

- [x] 1.1 Introduce a shared application-layer resolver for batch loading project and subject-employee snapshots for a list of `MonthEndTask` records
- [x] 1.2 Introduce an immutable lookup/helper object that encapsulates `projectFor(...)` and `subjectEmployeeFor(...)` access with the current fail-fast behavior
- [x] 1.3 Add dedicated unit tests for the resolver covering project-only tasks, nullable subject employees, and missing snapshot failures

## 2. Refactor month-end query services

- [x] 2.1 Update `GetMonthEndStatusOverviewService` to delegate snapshot loading and lookup to the shared resolver
- [x] 2.2 Update `GetEmployeeMonthEndWorklistService` to delegate snapshot loading and lookup to the shared resolver
- [x] 2.3 Update `GetProjectLeadMonthEndWorklistService` to delegate snapshot loading and lookup to the shared resolver

## 3. Verify unchanged behavior

- [x] 3.1 Update `MonthEndStatusOverviewServiceTest` and `MonthEndWorklistServicesTest` to cover the refactored collaboration without changing expected results
- [x] 3.2 Run `mvn test` and confirm the month-end query behavior remains unchanged
