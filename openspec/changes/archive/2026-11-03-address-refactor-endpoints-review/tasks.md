# Tasks

## 1. Work time report order (finding 1, design D1)

- [x] 1.1 In `WorkTimeReport.assemble`, sort entries by employee display name → project name → employee id → project id. Verify with new `WorkTimeReportTest` cases: several employees and projects come out in that order; equal names fall back to ids; an input map given in a different iteration order yields the same output order
- [x] 1.2 Update `GetWorkTimeReportServiceTest` to assert the exact entry order for both capacities, and verify `mvn test -Dtest=GetWorkTimeReportServiceTest` passes
- [x] 1.3 Replace the "order of entries is not defined" response description in `src/main/resources/openapi/paths/worktime.yaml` with the defined order (employee name, then project name), and verify `mvn compile` regenerates the API without errors

## 2. Work time report cleanup (findings 2 and 6, design D2 and D5)

- [x] 2.1 Make `WorkTimeScope.ProjectResolver` a plain lookup without logging, add the "unresolved projects expected" flag to `WorkTimeScope` (EMPLOYEE `false`, CONTROLLING `true`), and remove the warning lambda from `employeeScope`. Verify it compiles and the existing `GetWorkTimeReportServiceTest` cases still pass
- [x] 2.2 In `GetWorkTimeReportService.resolveProjects`, collect the unresolved zep ids and log them once per report, at `WARN` when not expected and at `DEBUG` when expected. Verify with service tests: an unresolved project is left out of the entries in both capacities and its hours still count in `employeeMonthTotalHours`
- [x] 2.3 Delete `isKnownEmployee` and its filter from `buildReport` and map the attendance keys directly. Verify `mvn test -Dtest='GetWorkTimeReportServiceTest,WorkTimeReportTest,WorkTimeResourceTest'` passes

## 3. Clarification creator rule in the factory (finding 4, design D3)

- [x] 3.1 In `MonthEndClarification`, move `validateCreator` from the compact constructor into the `create(..., sourceSystem, ...)` factory. Make it throw `MonthEndActorNotAuthorizedException` and drop its `SystemActor` exception. Verify with `MonthEndClarificationTest`: `create` with an uninvolved creator throws the authorization exception; `createBySystem` succeeds; building a stored clarification directly with an uninvolved creator no longer throws
- [x] 3.2 Update existing tests that expected `MonthEndValidationException` for an invalid creator (aggregate, ZEP mail persistence) to expect `MonthEndActorNotAuthorizedException`. Verify `mvn test -Dtest='MonthEndClarificationTest,PersistZepClarificationServiceTest,CreateClarificationFromZepMailServiceTest'` passes, skipping any class that doesn't exist

## 4. Single context load on clarification create (finding 3, design D4)

- [x] 4.1 Add `MonthEndEmployeeProjectContextService.resolve(MonthEndProjectContext, UserId subject)`: load the subject via `findByIds(Set.of(subject), month)`, treat a missing user or the system actor as `MonthEndEmployeeContextNotFoundException`, check the assignment, and reuse the context's project and leads. Rewrite the existing three-argument `resolve` to delegate through `MonthEndProjectContextService`. Verify `MonthEndEmployeeProjectContextServiceTest` covers the new overload (found, not active, system actor, not assigned) and still passes for the old signature
- [x] 4.2 Rewrite `CreateMonthEndClarificationService.create` in the D4 order: project context → factory → subject context when a subject is given → save and event. Delete `resolveSubject`. Verify with `CreateMonthEndClarificationServiceTest` that the project and user snapshots are loaded once per request with a subject, and that the error order still holds: unknown project → `MonthEndProjectContextNotFoundException`; non-lead omitting the subject or naming another employee → `MonthEndActorNotAuthorizedException`, even when that employee isn't assigned; lead naming an unassigned employee → `MonthEndEmployeeNotAssignedToProjectException`
- [x] 4.3 Verify `mvn test -Dtest=MonthEndResourceTest` passes unchanged: clarification create still returns 403 for an ineligible caller and 400 for an unknown project

## 5. Payroll month existence query (finding 7, design D6)

- [x] 5.1 Add `existsTasks(YearMonth, MonthEndScope)` to `MonthEndTaskRepository`. Implement it in `MonthEndTaskRepositoryAdapter` with `panache.count(...) > 0`, with the where-clauses for both scopes extracted into constants shared with `findTasks`. Verify with `MonthEndTaskRepositoryAdapterTest` cases for both scopes (tasks present → `true`, none → `false`, tasks only on non-led projects → `false`)
- [x] 5.2 Switch `GetPayrollMonthService.controllingPayrollMonth` to `existsTasks`, and verify `mvn test -Dtest=GetPayrollMonthServiceTest` passes with its mocks updated to the new method

## 6. Code hygiene

- [x] 6.1 Run `mvn package -Prewrite-check`. If it reports drift, run `mvn rewrite:run` and re-run the check until the build passes
