## Context

See proposal.md for the reasons behind this change, and the delta specs for the required behavior.

Current state in `com.gepardec.mega.hexagon.monthend`:

- `CompleteProjectTasksByTypeService` checks authorization with `MonthEndProjectContextService.resolve(month, projectId).eligibleProjectLeadIds()`. That resolves through `ProjectSnapshotAdapter.findActiveIn(month)`, which reads the **live** `ProjectRepository`. An unknown or inactive project throws `MonthEndProjectContextNotFoundException`, which `MonthEndDomainExceptionMapper` maps to `400`.
- The project-lead status overview decides which projects a lead sees with `MonthEndTaskRepository.findLeadProjectTasks`. That query is an `exists` subquery on `eligibleActorIds` of the project's tasks whose type has `MonthEndCompletionPolicy.ANY_ELIGIBLE_ACTOR`. So the overview works from eligibility recorded at generation time, and the bulk authorization does not.
- The rule that only `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW` are allowed lives only in `MonthEndResource.completeProjectLeadMonthEndTasks` and throws the adapter-level `MonthEndRequestValidationException`.
- `MonthEndDomainExceptionMapper` maps `MonthEndActorNotAuthorizedException` to `403`, the not-found exceptions to `404`, and every other `MonthEndException` (including `MonthEndValidationException`) to `400`.
- The employee flow passes a nullable `ProjectId` from the resource through `CompleteOwnTimeCheckTasksUseCase` into `MonthEndTaskRepository.findOpenEmployeeTimeCheckTasks`. The adapter branches on `null` and duplicates the query.
- The employee request body is `required: true`, so the generated interface declares it `@NotNull`. Its only field, the optional `projectId`, narrows the scope to one project, where generation creates exactly one `EMPLOYEE_TIME_CHECK` per employee and month. A project-scoped request therefore completes a single task, which `POST /monthend/tasks/{taskId}/complete` already covers.

## Goals / Non-Goals

**Goals:**
- Bulk authorization, the overview's `canComplete`, and single-task completion all use one source of truth: the eligible actors recorded on each task.
- Every rule the specs state for the operation is enforced in the application or domain layer, so any inbound adapter gets the same behavior.
- Names follow the context's role-suffixed pattern from the contract down to the use case, so the frontend's generated types read the way the backend does.

**Non-Goals:**
- No change to `MonthEndTask`, task generation, single-task completion, or the status overview.
- No change to the eligibility of `EMPLOYEE_TIME_CHECK` or `ABRECHNUNG`.
- No change to `MonthEndProjectContextService` or its other callers (clarification creation keeps using live project context. It creates new records, so the snapshot rule for existing tasks does not apply).
- No merge of the two bulk endpoints (the decision recorded in `employee-bulk-complete-own-time-checks` stands).

## Decisions

### Lead authorization is a repository existence check over lead-eligible tasks
Add an outbound query to `MonthEndTaskRepository` that answers "is this actor an eligible actor on any `ANY_ELIGIBLE_ACTOR` task of this project in this month". It reuses the type set and the `eligibleActorIds` join that `findLeadProjectTasks` already uses, so both answers come from one definition. The service calls it first and throws `MonthEndActorNotAuthorizedException` (→ `403`) when it is false. Otherwise it loads the open tasks of the requested type for the project and month, filters with `isOpen() && canBeCompletedBy(actor)`, completes, and saves. Because authorization no longer depends on the loaded tasks, the scope query only needs to return **open** tasks. The in-memory `isOpen()` filter stays, so the aggregate remains the authority on the rule.

`MonthEndProjectContextService` is removed from this service. An unknown project, an inactive project, and a project with no tasks that month all give "no lead-eligible task for you", which the spec defines as `403`.

**Alternatives considered:**
- *Derive authorization from the loaded `(month, project, type)` scope* (eligible on any task in the scope, whether open or done). Rejected: if the scope is empty, for example `LEISTUNGSNACHWEIS` on a non-billable project, it can't tell "you lead this project, nothing to do" (`200 []`) from "not your project" (`403`). Checking against all lead-eligible task types of the project avoids that.
- *Load all of the lead's project tasks via `findLeadProjectTasks` and check in memory.* Rejected: it loads every task of every project the lead leads just to answer a yes/no question about one project.
- *Keep `MonthEndProjectContextService` and fall back to task eligibility.* Rejected: that keeps two sources of truth.

### The type restriction is a property of `MonthEndTaskType`
Add `MonthEndTaskType.isProjectLeadBulkCompletable()`, true for `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`. The lead service checks it before any repository access and throws `MonthEndValidationException` (→ `400` through the existing mapper). The REST adapter no longer validates the type. `MonthEndRequestValidationException` remains for transport-level problems only.

**Alternative considered:** derive the set as `completionPolicy() == ANY_ELIGIBLE_ACTOR` plus "has a subject employee". Rejected: "has a subject employee" is a per-task invariant checked in `MonthEndTask`, not a property of the type, so the rule would be implicit and easy to break. Listing the two types explicitly on the enum keeps the rule in the domain.

### Employee scope reuses `findOpenSubjectTasks`; no dedicated query
The employee service loads the actor's open subject tasks with the existing `findOpenSubjectTasks(actorId, month)`. It then filters in memory on `type == EMPLOYEE_TIME_CHECK` and on `isOpen() && canBeCompletedBy(actor)`. `findOpenEmployeeTimeCheckTasks` is removed from the port and adapter. An employee has about three subject tasks per project and only a few projects, so filtering in memory costs nothing, and it drops a query that only served this use case.

The **explicit type filter is required**, because eligibility alone is not enough: a lead assigned as an employee on their own project is the subject of that project's `PROJECT_LEAD_REVIEW` (and `LEISTUNGSNACHWEIS`) *and* an eligible actor on it. Filtering only on `canBeCompletedBy` would let the employee endpoint complete the lead's own review.

The use case has a single `complete(month, actorId)` method. The scope is always the caller's whole month.

**Alternatives considered:**
- *A single `findOpenTasks(MonthEndTaskScope)` port method with a scope value object shared by both flows.* Rejected for now: it would be the only criteria-object finder in a port made of intent-named finders, and generic scope objects tend to grow into query builders. Revisit if more "complete everything in scope X" operations appear.
- *A dedicated `findOpenTimeCheckTasks(employeeId, month)`.* Rejected: it adds port surface for a lookup the existing method already covers.

### Naming and parameter order
| Layer | From | To |
|---|---|---|
| Inbound port | `CompleteProjectTasksByTypeUseCase` | `CompleteProjectLeadMonthEndTasksUseCase` |
| Service | `CompleteProjectTasksByTypeService` | `CompleteProjectLeadMonthEndTasksService` |
| Inbound port | `CompleteOwnTimeCheckTasksUseCase` | `CompleteEmployeeMonthEndTasksUseCase` |
| Service | `CompleteOwnTimeCheckTasksService` | `CompleteEmployeeMonthEndTasksService` |
| Outbound port | `findByMonthProjectAndType(month, projectId, type)` (all statuses) | `findOpenProjectTasksOfType(month, projectId, type)` (open only) |
| Outbound port | `findOpenEmployeeTimeCheckTasks(employeeId, month, projectId?)` | removed; reuse `findOpenSubjectTasks(subjectId, month)` |
| Outbound port | none | `existsLeadTask(month, projectId, leadId)` |
| OpenAPI schema | `CompleteProjectTasksRequest` | `CompleteProjectLeadMonthEndTasksRequest` |
| OpenAPI schema | `CompleteOwnTimeChecksRequest` | removed; the employee endpoint takes no body |
| OpenAPI schema | `CompletedTasksResponse` | `MonthEndTaskCompletion` |

The use cases and services mirror the operation IDs, which are unchanged, the same way `GetEmployee…`/`GetProjectLead…MonthEndStatusOverviewUseCase` do. Use-case parameters put the actor **last**, matching the closest sibling, `CompleteMonthEndTaskUseCase.complete(taskId, actorId)`:
- `complete(YearMonth month, ProjectId projectId, MonthEndTaskType type, UserId actorId)`
- `complete(YearMonth month, UserId actorId)`

The new and renamed repository methods go with the other query methods, before `save`/`saveAll`.

### The employee endpoint takes no request body
Remove `requestBody` from `POST /monthend/{month}/tasks/complete/employee` and delete the `CompleteOwnTimeChecksRequest` schema. The generated method then takes only the month, and the endpoint no longer consumes a media type, so a bare POST works without a `Content-Type` header. The scope is always the caller's open time checks for the whole month.

The project-scoped variant is dropped rather than kept as a convenience. Generation creates exactly one `EMPLOYEE_TIME_CHECK` per employee, project, and month, so completing "my time check for project X" is a single-task completion. The client already has the task ID from the status overview and can use `POST /monthend/tasks/{taskId}/complete`. Keeping `projectId` would give two endpoints for the same operation, plus an overload and a request schema that exist only to support it.

**Alternative considered:** keep an optional body with an optional `projectId`. Rejected for the reasons above. It also needed clients to send `Content-Type: application/json` with an empty body, because a bare POST was rejected with `415`.

### Transport mapping goes through `MonthEndRestMapper`
Add `MonthEndTaskType toDomain(MonthEndTaskTypeDto)` to the MapStruct mapper (an enum-to-enum mapping by name) and replace `MonthEndTaskType.valueOf(request.getType().toString())`. Restore explicit imports in `MonthEndResource`.

## Risks / Trade-offs

- **`400` → `403` for an unknown or not-led project is visible to clients.** → The frontend only reaches this with hand-built requests, because the UI offers bulk completion only on columns where the overview shows completable tasks. It is recorded as **BREAKING** in the proposal and the REST spec's migration note.
- **Renaming the schemas renames the frontend's generated types.** → The JSON on the wire is unchanged, so only a regenerate plus import fixes is needed. Ship both in the same release as the frontend feature that uses the endpoints.
- **Clarification creation keeps authorizing leads from the live project context.** → That is the right source for creating new records, but the two rules can now disagree about "who leads project X in month M". This is accepted and recorded under Non-Goals. Revisit if clarification actions for existing records get a lead check.
- **A client that still sends `{ "projectId": ... }` to the employee endpoint completes the whole month instead of one project.** → The endpoint no longer reads a body, so the field is silently ignored rather than rejected. This is recorded as **BREAKING** in the proposal and the REST spec's migration note. The frontend has not built on this endpoint yet, and it uses single-task completion for one project.
- **The employee flow depends on an in-memory type filter for correctness.** → A service test covers a lead who is also an employee on their own project and has an open `PROJECT_LEAD_REVIEW` as subject. Employee bulk completion must leave that task open.
- **Another project-lead query.** → It's an indexed existence check on `(monthValue, projectId)` plus the eligible-actor join, the same shape as the existing overview query.

## Migration Plan

This is a code and contract change only, with no data migration. Deploy the backend and the frontend's regenerated client together. To roll back, revert the change; nothing is persisted differently.
