## 1. API contract (OpenAPI)

- [x] 1.1 Add a `BulkCompleteTasksRequest` schema to `openapi/schemas/monthend.yaml` with required `month` (`MonthValue`), `projectId` (uuid), and `type` (`MonthEndTaskType`).
- [x] 1.2 Add a `BulkCompleteTasksResponse` schema to `openapi/schemas/monthend.yaml` with a required `completed` array of `MonthEndTask`.
- [x] 1.3 Add the `POST /monthend/tasks/complete` path to `openapi/paths/monthend.yaml` (operationId `completeMonthEndTasks`, request body `BulkCompleteTasksRequest`, `200` → `BulkCompleteTasksResponse`, plus `400`/`403`/`500`) and register the path in `openapi/openapi.yaml`.
- [x] 1.4 Build to regenerate the `MonthEndApi` interface and DTOs; confirm the new operation method and request/response models are generated.

## 2. Persistence — scoped query

- [x] 2.1 Add `findByProjectMonthAndType(YearMonth month, ProjectId projectId, MonthEndTaskType type)` returning `List<MonthEndTask>` to the `MonthEndTaskRepository` outbound port.
- [x] 2.2 Implement it in `MonthEndTaskRepositoryAdapter` as a Panache query on `monthValue`, `projectId`, and `type`, mapping entities to domain via `MonthEndTaskMapper`.

## 3. Application — scoped bulk completion use case

- [ ] 3.1 Add inbound port `CompleteMonthEndTasksForProjectUseCase` with a method taking `(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId)` and returning the `List<MonthEndTask>` newly completed.
- [ ] 3.2 Implement `CompleteMonthEndTasksForProjectService` (`@ApplicationScoped @Transactional`): resolve the project context via `MonthEndProjectContextService.resolve(month, projectId)`; if `actorId` is not in `eligibleProjectLeadIds`, throw `MonthEndActorNotAuthorizedException`.
- [ ] 3.3 In the same service, query the scope via `findByProjectMonthAndType`, filter to tasks that are `isOpen()` and `canBeCompletedBy(actorId)`, call `task.complete(actorId)` on each, `saveAll(...)` the transitioned tasks, and return them.
- [ ] 3.4 Log an INFO summary of the outcome (project, type, month, completed count) on completion.

## 4. REST adapter

- [ ] 4.1 Implement the generated bulk-complete method in `MonthEndResource`, secured with `@MegaRolesAllowed(Role.PROJECT_LEAD)`.
- [ ] 4.2 In the adapter, reject any `type` other than `LEISTUNGSNACHWEIS` or `PROJECT_LEAD_REVIEW` with `MonthEndRequestValidationException` (→ `400`) before invoking the use case; parse `month` via the existing transport helper.
- [ ] 4.3 Invoke `CompleteMonthEndTasksForProjectUseCase` with the authenticated actor from `AuthenticatedActorContext`, map each completed task with `MonthEndRestMapper.toDto(MonthEndTask)`, and return `200` with `{ "completed": [ ... ] }`.

## 5. Tests

- [ ] 5.1 Domain/application unit tests for `CompleteMonthEndTasksForProjectService`: all open eligible tasks completed; already-done tasks skipped and omitted; empty result when re-run; non-eligible-lead actor rejected (`403` exception); unknown/inactive project rejected (context-not-found exception).
- [ ] 5.2 Adapter/REST tests in `MonthEndResourceTest`: happy-path `200` with `completed` shape; `EMPLOYEE_TIME_CHECK` and `ABRECHNUNG` → `400`; non-lead caller → `403`; unknown project → `400`; role security (`PROJECT_LEAD` required).
- [ ] 5.3 Repository test for `findByProjectMonthAndType` covering type/project/month scoping.
