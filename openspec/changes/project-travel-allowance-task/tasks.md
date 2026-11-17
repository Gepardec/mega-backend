# Tasks

## 1. ZEP project tasks

- [x] 1.1 On the ZEP test instance, call `GET /api/v1/projects/{id}/tasks?search=reisepauschale` and compare the returned `id` with the `project_task_id` of attendances. Verified 2026-11-17: Intern returns "Reisepauschale" with `id` 873, the process id the attendances carry. The result is recorded in design.md under D5.
- [ ] 1.2 Add the `ZepProjectTask` DTO (`id`, `name`, `project_id` only, all other fields ignored, D5) and `getProjectTasks(projectId, search, page)` on `ZepProjectRestClient` (`{id}/tasks`). Verify: a JSON test response under `src/test/resources/zep/rest/testresponses/projects/` uses the full observed task shape from D5, including the fields not mapped, and deserialises into the three mapped fields.
- [ ] 1.3 Add `ProjectService.getProjectTasks(int projectId, String search)`, which pages through all results and returns an empty list on ZEP 404. The adapter passes `search=reisepauschale`. Verify: a new `ProjectServiceTest` covers multiple pages, 404 (empty list) and other errors (propagated).

## 2. Domain model and calculator

- [ ] 2.1 Add the domain record `ProjectTask(projectZepId, zepTaskId, name)` and the value object `TravelAllowanceTasks` with `from(List<ProjectTask>)`, `none()`, `declaresOwnTask(projectZepId)` and `ownTaskIds(projectZepId)` (D2). Verify: `TravelAllowanceTasksTest` covers an exact name, " reisepauschale ", "Reisepauschale Kunde" (no match), several matching tasks per project, task id 873 dropped, and an unknown project.
- [ ] 2.2 Add `travelAllowanceTasks` (non-null) to `WorkTimeEvaluation` and update every construction site and test fixture. Verify: `mvn test -Dtest='WorkTimeWarningEvaluatorTest,*CalculatorTest'` compiles and passes.
- [ ] 2.3 Rewrite `TravelAllowanceCalculator` per D4: places, booked/allowed per place, `wrongProject`, summed excess. Remove the billability input. Verify: `TravelAllowanceCalculatorTest` covers every scenario of the requirements "Travel allowance exceeding the journeys' allowance is reported per day", "Travel allowance on "Intern" for a project with its own task is reported per day", "A journey's allowance belongs on its project's own task or on "Intern"" and "Travel-allowance bookings are bookings on the process "Reisepauschale"". The old customer-trip tests are removed or rewritten.
- [ ] 2.4 Update `WorkTimeWarningEvaluatorTest` cases that relied on customer-trip semantics, and add one case where both `TRAVEL_ALLOWANCE_WRONG_PROJECT` and `TRAVEL_ALLOWANCE_EXCEEDED` (project-task excess) appear on one day. Verify: `mvn test -Dtest=WorkTimeWarningEvaluatorTest` passes.

## 3. Application wiring

- [ ] 3.1 Add the outbound port `WorkTimeProjectTaskZepPort.fetchTasks(Set<Integer> projectZepIds)` and `WorkTimeProjectTaskZepAdapter` over `ProjectService`, mapping DTOs to `ProjectTask` with MapStruct and logging the fetch at debug level (D1, D5). Verify: `WorkTimeProjectTaskZepAdapterTest` covers several projects, a project with no tasks, and an empty id set (no ZEP call).
- [ ] 3.2 In `GetEmployeeWarningsService`, load the tasks only when `TRAVEL_ALLOWANCE` is active, for the distinct project ids of all bookings, and pass `TravelAllowanceTasks.from(...)` (or `none()`) into the evaluation. Load `billableProjectZepIds` for `INTERNAL_NOTE` only (D3). Verify: `GetEmployeeWarningsServiceTest` covers the task lookup being called with the booked project ids, not called when the rule is inactive, billable ids not loaded when only `TRAVEL_ALLOWANCE` is active, and the spec scenario "Warnings take the booked projects' travel-allowance tasks into account".
- [ ] 3.3 Run `HexagonalArchitectureTest`. Verify: it passes. `ProjectTask` and `TravelAllowanceTasks` are records in `domain`, the port is an interface in `application/port/outbound`, and the adapter is in `adapter/outbound`.

## 4. Integration checks

- [ ] 4.1 Run `mvn test`. Verify: the whole suite passes.
- [ ] 4.2 Run `mvn package -Prewrite-check`. If it reports drift, run `mvn rewrite:run` and re-run the check. Verify: the build passes.

## Workflow follow-up

- Archive `remove-billable-project-warning` before archiving this change. Both modify the `worktime-warnings` requirement "Warnings are queryable per employee for a payroll month".
- Frontend: change the hint text for `TRAVEL_ALLOWANCE_WRONG_PROJECT` to "This project tracks travel time. Please book the allowance on its 'Reisepauschale' task." and release it with or before this backend change.
