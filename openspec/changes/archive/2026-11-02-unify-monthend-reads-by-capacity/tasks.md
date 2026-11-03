# Tasks

## 1. Shared kernel — capacity

- [x] 1.1 Add `Capacity { EMPLOYEE, CONTROLLING }` to `shared.domain.model` with `Role requiredRole()` (EMPLOYEE → `Role.EMPLOYEE`, CONTROLLING → `Role.PROJECT_LEAD`). Verify with a parameterized `CapacityTest` over both values.
- [x] 1.2 Add a capacity gate next to `AuthenticatedActorContext` in `shared.application.security` (e.g. `requireCapacity(Capacity)`) that throws the existing `ForbiddenException` when the actor lacks `capacity.requiredRole()`. Verify with unit tests in `AuthenticatedActorContextTest` (or a dedicated test): a lead passes both capacities, a plain employee passes `EMPLOYEE` and is rejected for `CONTROLLING`.

## 2. API contract (OpenAPI)

- [x] 2.1 Add a `Capacity` string enum schema (`EMPLOYEE`, `CONTROLLING`) to `openapi/schemas/shared.yaml`. Verify with `mvn clean compile` that a `CapacityDto` enum is generated.
- [x] 2.2 In `openapi/paths/monthend.yaml` and `openapi/openapi.yaml`, replace the four role-suffixed status-overview and payroll-month paths with `GET /monthend/{month}/status-overview` (operationId `getMonthEndStatusOverview`) and `GET /monthend/payroll-month` (operationId `getPayrollMonth`). Each takes a required `capacity` query parameter referencing `Capacity`, keeps its current response schema, and documents `400`/`403`/`500`. Verify with `mvn clean compile` that `MonthEndApi` declares the two new methods with a `@NotNull` capacity parameter and none of `getEmployeeMonthEndStatusOverview`, `getProjectLeadMonthEndStatusOverview`, `getEmployeePayrollMonth`, `getProjectLeadPayrollMonth`.

## 3. Domain — duty and scope

- [x] 3.1 Add `Capacity dutyOf()` to `MonthEndTaskType`: `EMPLOYEE_TIME_CHECK` → `EMPLOYEE`, `LEISTUNGSNACHWEIS`/`PROJECT_LEAD_REVIEW`/`ABRECHNUNG` → `CONTROLLING`. Verify with a parameterized case in `MonthEndTaskTypeTest` over all four types.
- [x] 3.2 Add `boolean isCompletableIn(Capacity capacity, UserId actorId)` to `MonthEndTask` (`canBeCompletedBy(actorId) && type().dutyOf() == capacity`), leaving `canBeCompletedBy` and `complete` unchanged. Verify with `MonthEndTaskTest`: a subject who is also eligible on a `PROJECT_LEAD_REVIEW` is completable only in `CONTROLLING`; the eligible employee on their `EMPLOYEE_TIME_CHECK` is completable only in `EMPLOYEE`; a non-eligible actor is completable in neither; `complete(actorId)` still succeeds for the eligible subject regardless of capacity.
- [x] 3.3 Add the sealed `MonthEndScope` (`SubjectEmployee(UserId)`, `LedProjects(UserId)`) with `MonthEndScope.of(Capacity, UserId)` in `monthend.domain.model`. Verify with a unit test that each capacity maps to the expected scope record.

## 4. Persistence — scoped queries

- [x] 4.1 On `MonthEndTaskRepository`, replace `findEmployeeVisibleTasks` and `findLeadProjectTasks` with `findTasks(YearMonth month, MonthEndScope scope)`. The adapter dispatches on the scope to the existing queries unchanged. Verify by moving the existing cases in `MonthEndTaskRepositoryAdapterTest` to `findTasks` with the matching scope and seeing them pass unchanged.
- [x] 4.2 On `MonthEndClarificationRepository`, replace `findAllEmployeeClarifications` and `findAllProjectLeadClarifications` with `findClarifications(YearMonth month, MonthEndScope scope)`, leaving the `findOpen*` methods untouched. Verify by moving the existing cases in `MonthEndClarificationRepositoryAdapterTest` to the new method with the matching scope and seeing them pass unchanged.

## 5. Application — unified query use cases

- [x] 5.1 Replace `GetEmployeeMonthEndStatusOverviewUseCase`/`Service` and `GetProjectLeadMonthEndStatusOverviewUseCase`/`Service` with `GetMonthEndStatusOverviewUseCase`/`Service` (`getOverview(UserId actorId, YearMonth month, Capacity capacity)`), loading tasks and clarifications via `MonthEndScope.of(capacity, actorId)`. Delete the old interfaces, services and their tests. Verify with `GetMonthEndStatusOverviewServiceTest`: each capacity queries both repositories with the matching scope and returns their results unchanged in the overview.
- [x] 5.2 Replace `GetEmployeePayrollMonthUseCase`/`Service` and `GetProjectLeadPayrollMonthUseCase`/`Service` with `GetPayrollMonthUseCase`/`Service` (`getPayrollMonth(UserId actorId, Capacity capacity)`), keeping both rules unchanged as private methods selected by capacity, with the injected `Clock`. The lead rule uses `findTasks(currentMonth, LedProjects)`. Delete the old interfaces, services and their tests. Verify with `GetPayrollMonthServiceTest`: the four employee-rule scenarios and two lead-rule scenarios of the `payroll-month` spec, with a fixed `Clock`.

## 6. REST adapter

- [x] 6.1 Add `Capacity toDomain(CapacityDto)` to `SharedRefRestMapper` in `shared.adapter.inbound.rest`, which month-end already `uses` and worktime can reuse without depending on month-end, and add `Capacity` as a `@Context` to the overview mappings in `MonthEndRestMapper`. Compute `canComplete` via `task.isCompletableIn(capacity, actorId)` and leave the clarification flags actor-only. Verify with a mapper-level or REST test showing a lead's own `PROJECT_LEAD_REVIEW` mapped with `canComplete=false` in `EMPLOYEE` and `true` in `CONTROLLING`.
- [x] 6.2 In `MonthEndResource`, replace the four methods with `getMonthEndStatusOverview(month, capacity)` and `getPayrollMonth(capacity)`, each `@MegaRolesAllowed(Role.EMPLOYEE)` followed by the capacity gate from 1.2, and remove the four old use-case dependencies. Verify with `MonthEndResourceTest`: `200` for an employee in `EMPLOYEE`; `403` for a non-lead in `CONTROLLING` on both endpoints; `200` for a lead in both capacities; `400` without `capacity`; `400` for a malformed month; the observed client-error status for an unknown capacity value is asserted explicitly; the old paths return `404`.
- [x] 6.3 Update `MonthEndIT` to the new endpoints and add an end-to-end case for a lead who is also assigned to their own project: in `EMPLOYEE` capacity their own `PROJECT_LEAD_REVIEW` has `canComplete=false`, and `POST /monthend/tasks/{taskId}/complete` on that task still returns `200` with the lead as `completedBy`. Verify by running `mvn test -Dtest=MonthEndIT`.

## 7. Verification

- [x] 7.1 Verify with `grep -rn` over `src` that no references remain to `GetEmployeeMonthEndStatusOverview`, `GetProjectLeadMonthEndStatusOverview`, `GetEmployeePayrollMonth`, `GetProjectLeadPayrollMonth`, `findEmployeeVisibleTasks`, `findLeadProjectTasks`, `findAllEmployeeClarifications`, `findAllProjectLeadClarifications`, `status-overview/employee`, `status-overview/project-lead`, `payroll-month/employee`, or `payroll-month/project-lead`.
- [x] 7.2 Run `mvn test` and confirm the full suite, including `ArchitectureTest`, passes.

## 8. Rename the lead capacity to `CONTROLLING`

- [x] 8.1 Rename the capacity value `PROJECT_LEAD` to `CONTROLLING` in `Capacity` (still requiring `Role.PROJECT_LEAD`), in the `Capacity` OpenAPI schema, in `MonthEndTaskType.dutyOf()` and in all tests that use it, including `?capacity=` query values. Leave `Role.PROJECT_LEAD` and `PROJECT_LEAD_REVIEW` unchanged. Verify with `grep -rn` over `src` and `openapi` that no `Capacity.PROJECT_LEAD`, `CapacityDto.PROJECT_LEAD` or `capacity=PROJECT_LEAD` remains, and that `MonthEndResourceTest` asserts `403` for a non-lead with `capacity=CONTROLLING` and a client error for `capacity=PROJECT_LEAD`.
- [x] 8.2 Run `mvn test` and confirm the full suite passes.
