## 1. API contract (OpenAPI)

- [x] 1.1 In `openapi/schemas/monthend.yaml`, rename `CompleteProjectTasksRequest` → `CompleteProjectLeadMonthEndTasksRequest` and `CompletedTasksResponse` → `MonthEndTaskCompletion`, and delete the employee request schema (`CompleteOwnTimeChecksRequest`, since renamed to `CompleteEmployeeMonthEndTasksRequest`). Leave the field names unchanged. Verify with `grep` that none of the old schema names, and no `CompleteEmployeeMonthEndTasksRequest`, remain under `src/main/resources/openapi`.
- [x] 1.2 In `openapi/paths/monthend.yaml`, point both bulk paths at the renamed response schema and the project-lead path at the renamed request schema, and remove `requestBody` from `/monthend/{month}/tasks/complete/employee`. Keep paths and operation IDs unchanged. Verify with `mvn clean compile` that the generated `completeEmployeeMonthEndTasks` takes only the month and has no `@Consumes`, and that `CompleteProjectLeadMonthEndTasksRequestDto` and `MonthEndTaskCompletionDto` are generated but no employee request DTO.

## 2. Domain

- [x] 2.1 Add `isProjectLeadBulkCompletable()` to `MonthEndTaskType`, returning `true` only for `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`. Verify with a parameterized unit test over all four types in the domain model tests.

## 3. Persistence — outbound port and adapter

- [x] 3.1 On `MonthEndTaskRepository`, replace `findByMonthProjectAndType` with `findOpenProjectTasksOfType(YearMonth month, ProjectId projectId, MonthEndTaskType type)`, which returns open tasks only, and move it next to the other `find…` methods. Verify with `MonthEndTaskRepositoryAdapterTest`: the existing type/project/month scoping tests pass under the new name, and a new case shows DONE tasks are excluded.
- [x] 3.2 Remove `findOpenEmployeeTimeCheckTasks` from `MonthEndTaskRepository` and `MonthEndTaskRepositoryAdapter`, along with its two adapter tests. Verify that `mvn compile` succeeds once the employee service (5.1) no longer calls it.
- [x] 3.3 Add a port method that reports whether an actor is an eligible actor on any `ANY_ELIGIBLE_ACTOR` task of a project in a month (e.g. `existsLeadTask(YearMonth month, ProjectId projectId, UserId leadId)`). Implement it in the adapter as a count/exists query that reuses `leadTaskTypes()` and the same `eligibleActorIds` join as `findLeadProjectTasks`. Verify with adapter tests: true for a lead eligible only on a DONE task, true when the project has only `PROJECT_LEAD_REVIEW`/`ABRECHNUNG` (no `LEISTUNGSNACHWEIS`), false when the actor is eligible only as the subject of an `EMPLOYEE_TIME_CHECK`, false for another project or month, and false for an unknown project.

## 4. Application — project-lead bulk completion

- [x] 4.1 Rename `CompleteProjectTasksByTypeUseCase`/`Service` → `CompleteProjectLeadMonthEndTasksUseCase`/`Service` with the signature `complete(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId)`. Verify it compiles and that the renamed service test class runs.
- [x] 4.2 In the service, reject a type where `isProjectLeadBulkCompletable()` is false with `MonthEndValidationException`, before any repository access. Verify with a unit test that `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG` throw and that the repository is never called.
- [x] 4.3 Replace the `MonthEndProjectContextService` check with the new `existsLeadTask` query, throwing `MonthEndActorNotAuthorizedException` when it is false, and remove the `MonthEndProjectContextService` dependency. Keep the query → filter `isOpen() && canBeCompletedBy` → complete → `saveAll` → INFO log sequence, and don't reassign the local result variable. Verify with unit tests: a lead removed after generation (eligible on the tasks) completes them; a non-leading actor (including an unknown project) is rejected; a leading actor with no tasks of the type gets an empty list and nothing is saved; existing cases (all open completed, done skipped, re-run empty, ineligible task skipped) still pass.

## 5. Application — employee bulk completion

- [x] 5.1 Rename `CompleteOwnTimeCheckTasksUseCase`/`Service` → `CompleteEmployeeMonthEndTasksUseCase`/`Service` and replace the nullable-project method with a single `complete(YearMonth month, UserId actorId)`. It loads via `findOpenSubjectTasks(actorId, month)` and filters on `type == EMPLOYEE_TIME_CHECK` and `isOpen() && canBeCompletedBy(actorId)`. Verify with the renamed service tests: all projects are completed; already-done tasks are skipped; empty scope saves nothing.
- [x] 5.2 Add a service test for a lead who is also an employee on their own project: the loaded subject tasks include an open `PROJECT_LEAD_REVIEW` and `LEISTUNGSNACHWEIS` on which the actor is eligible. Verify that only the `EMPLOYEE_TIME_CHECK` is completed and saved, and the lead tasks are neither returned nor saved.

## 6. REST adapter

- [x] 6.1 Add `MonthEndTaskType toDomain(MonthEndTaskTypeDto)` to `MonthEndRestMapper` and use it in `completeProjectLeadMonthEndTasks` in place of `valueOf(toString())`. Remove the type check from the resource. Verify in `MonthEndResourceTest` that the `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG` tests stub the mocked use case to throw `MonthEndValidationException` and assert `400`. The rule itself is covered by the service test in 4.2.
- [x] 6.2 Update both bulk methods to use the renamed use cases, the renamed project-lead request DTO, and `MonthEndTaskCompletionDto`. The employee method takes only the month and calls `complete(month, actorId)`. Verify with REST tests: a bare POST with no body and no `Content-Type` → `200` with the completed tasks; the role gate still rejects callers without the employee role.
- [x] 6.3 Restore explicit imports in `MonthEndResource` in place of `generated.model.*`, put each constructor parameter on its own line, and fix the `if(` spacing. Verify with `mvn compile` and a clean diff review.
- [x] 6.4 Update the project-lead REST tests for the new status codes. Unknown project → `403` (was `400`). A lead with no tasks of the requested type → `200` with an empty `completed` array. Keep the role-gate tests for both endpoints. Verify by running `mvn test -Dtest=MonthEndResourceTest`.

## 7. Verification

- [x] 7.1 Verify with `grep -rn` over `src` that no references remain to `CompleteProjectTasksByType`, `CompleteOwnTimeCheck`, `CompletedTasksResponse`, `CompleteProjectTasksRequest`, `CompleteOwnTimeChecksRequest`, `CompleteEmployeeMonthEndTasksRequest`, `findOpenEmployeeTimeCheckTasks`, or `findByMonthProjectAndType`.
- [x] 7.2 Run `mvn test` and confirm the full suite, including `ArchitectureTest`, passes.
