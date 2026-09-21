## 1. API contract (OpenAPI)

- [x] 1.1 Add a `CompleteOwnTimeChecksRequest` schema to `openapi/schemas/monthend.yaml` with a single nullable optional `projectId` (uuid); the month travels as a path parameter. Verify the schema resolves during the build.
- [x] 1.2 Add the `POST /monthend/{month}/tasks/complete/employee` path to `openapi/paths/monthend.yaml` (operationId `completeEmployeeMonthEndTasks`, request body `CompleteOwnTimeChecksRequest`, `200` → the existing `CompletedTasksResponse`, plus `400`/`403`/`500`) and register the path in `openapi.yaml`; verify the generated `MonthEndApi` gains the operation method.

## 2. Persistence — self-scoped query

- [x] 2.1 Add `findOpenEmployeeTimeCheckTasks(UserId employeeId, YearMonth month, ProjectId projectId)` returning `List<MonthEndTask>` to the `MonthEndTaskRepository` outbound port, with a null `projectId` meaning "all projects".
- [x] 2.2 Implement it in `MonthEndTaskRepositoryAdapter` as a Panache query on `monthValue`, `subjectEmployeeId`, `type = EMPLOYEE_TIME_CHECK`, and `status = OPEN`, adding the `projectId` predicate only when a project is given; verify with `MonthEndTaskRepositoryAdapterTest.findOpenEmployeeTimeCheckTasks_shouldReturnOnlyOpenEmployeeTimeCheckTasksForEmployeeMonthAndProject` and `..._shouldReturnOpenEmployeeTimeCheckTasksAcrossAllProjects_whenProjectIdIsNull`.

## 3. Application — self-service bulk completion use case

- [x] 3.1 Add inbound port `CompleteOwnTimeCheckTasksUseCase` with a method taking `(UserId actorId, YearMonth month, ProjectId projectId)` and returning the `List<MonthEndTask>` newly completed.
- [x] 3.2 Implement `CompleteOwnTimeCheckTasksService` (`@ApplicationScoped @Transactional`): query the scope via `findOpenEmployeeTimeCheckTasks`, filter to tasks that are `isOpen()` and `canBeCompletedBy(actorId)`, call `task.complete(actorId)` on each, `saveAll(...)` the transitioned tasks, and return them.
- [x] 3.3 Log an INFO summary of the outcome (actor, month, completed count) on completion; verify the message renders (no format-conversion error in the test log).

## 4. REST adapter

- [x] 4.1 Implement the generated bulk time-check method in `MonthEndResource`, secured with `@MegaRolesAllowed(Role.EMPLOYEE)`; verify with `MonthEndResourceTest.completeEmployeeMonthEndTasks_shouldRejectCallerWithoutEmployeeRole` (403).
- [x] 4.2 In the adapter, parse `month` via the existing transport helper and pass `null` as the project when `projectId` is absent; take the actor from `AuthenticatedActorContext` and never from the request body.
- [x] 4.3 Map each completed task with `MonthEndRestMapper.toDto(MonthEndTask)` and return `200` with `{ "completed": [ ... ] }`; verify with `MonthEndResourceTest.completeEmployeeMonthEndTasks_shouldReturnCompletedTasksForEmployee`.

## 5. Tests

- [x] 5.1 Application unit tests for `CompleteOwnTimeCheckTasksService`: all open tasks completed for a given project; all projects covered when the project is null; nothing saved when no open tasks exist (`CompleteOwnTimeCheckTasksServiceTest`).
- [x] 5.2 REST tests in `MonthEndResourceTest`: happy-path `200` with the `completed` shape for a named project; project omitted delegates with a null project; non-employee caller → `403`.
