## Context

Month-end tasks live in the `com.gepardec.mega.hexagon.monthend` bounded context. Tasks are generated up front for a month (`MonthEndTaskPlanningService` / `GenerateMonthEndTasksService`): per assigned employee an `EMPLOYEE_TIME_CHECK` (eligible actor = the employee, `INDIVIDUAL_ACTOR`), plus `PROJECT_LEAD_REVIEW` and — for billable projects — `LEISTUNGSNACHWEIS` (eligible actors = the project's active leads, `ANY_ELIGIBLE_ACTOR`), and one project-level `ABRECHNUNG`.

Completion today goes through `POST /monthend/tasks/{taskId}/complete` → `CompleteMonthEndTaskUseCase` → `MonthEndTask.complete(actor)`. The aggregate's `complete(actor)` enforces exactly two rules: the actor must be in `eligibleActorIds` (else `MonthEndActorNotAuthorizedException` → `403`), and an already-`DONE` task is a no-op returning itself. There is **no dependency/readiness gate** in the domain — a lead-eligible task is completable by any active lead from the moment it is generated. Consequently the `canComplete` flag exposed on overview entries is defined as pure eligibility: `task.canBeCompletedBy(actor) == eligibleActorIds.contains(actor)`.

The controlling page wants to complete an entire task column for a project in one action instead of issuing one request per employee. The request carries intent — `{ month, projectId, type }` — and the backend decides which tasks are actually completed.

## Goals / Non-Goals

**Goals:**
- One scoped, transactional endpoint that completes all open, actor-eligible tasks of one type for a project in a month.
- Reuse the existing per-task completion behavior and completer tracking unchanged.
- Partial success as the normal case: not-open / not-eligible tasks are skipped silently, never failed.
- Return the same task shape as single-task completion so the frontend updates its store identically.

**Non-Goals:**
- No task-ID-list ("arbitrary selection") variant — that can be added later without breaking this endpoint.
- No new dependency/readiness rule (e.g. "lead review requires the employee's time-check first"). None exists today; introducing one is out of scope.
- No ability for a lead/office user to complete employees' `EMPLOYEE_TIME_CHECK` tasks — that would be a domain eligibility change, explicitly excluded.
- No change to the `MonthEndTask` aggregate, task generation, or existing endpoints.

## Decisions

### Endpoint carries intent `{ month, projectId, type }`, not task IDs
The backend is authoritative on completability, so the client sends scope, not a resolved list. **Alternative considered:** an ID-list body. Rejected for the primary UI (the chip acts on a whole column); an ID-list variant remains possible later as a separate endpoint.

### Accepted `type` restricted to `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`
These are the only employee-scoped types a project lead is an eligible actor for. `EMPLOYEE_TIME_CHECK` is eligible only to its subject employee, so bulk-completing it as a lead would complete nothing; `ABRECHNUNG` is a single project-level task already handled by the per-task endpoint. Both are rejected with `400` at the REST adapter (validation, `MonthEndRequestValidationException`), before any domain call. **Alternative considered:** accept all three employee-scoped types and let `EMPLOYEE_TIME_CHECK` return an empty result. Rejected — a request that can never do anything should fail loudly, not return a misleading empty success.

### Authorization lives in the application service, via `MonthEndProjectContext.eligibleProjectLeadIds`
The new application service resolves the project context with the existing `MonthEndProjectContextService.resolve(month, projectId)`, then requires the acting `UserId` to be in `eligibleProjectLeadIds` (→ else `403`). This set is the *same* `project.leadIds() ∩ active-users` set that became each lead-eligible task's `eligibleActorIds` at generation time. So a caller who passes this check is guaranteed eligible on every in-scope task — which is precisely why per-task `NOT_ELIGIBLE` skips cannot occur in practice and no `skipped[]` list is needed. Keeping this check in the service (not the adapter) keeps the REST adapter thin: the adapter owns only transport concerns (type-enum validation, month parsing, response mapping), and completability/authorization stays in the application/domain layer. **Alternative considered:** infer authorization from whether the scoped query returns any actor-eligible task. Rejected — it cannot distinguish "not your project" (`403`) from "your project has no such tasks / all done" (`200` empty).

### Unknown / inactive project → `400` (reuse existing exception mapping)
`MonthEndProjectContextService.resolve` throws `MonthEndProjectContextNotFoundException`, which the existing `MonthEndDomainExceptionMapper` maps to `400`. "Unknown project" and "no month-end context for the month" are a single failure mode here, so both surface as `400`. **Alternative considered:** map project-context-not-found to `404`. Deferred — it would change the mapper's behavior for an existing exception; `400` is consistent with the current contract and the proposal's "unknown projectId → 400" line.

### New outbound query; completion stays in the aggregate
Add `MonthEndTaskRepository.findByProjectMonthAndType(month, projectId, type)` returning all in-scope tasks. The new application service filters `isOpen() && canBeCompletedBy(actor)`, calls `task.complete(actor)` on each, and `saveAll(...)` the transitioned tasks. **Alternative considered:** filter open/eligible in the SQL query. Rejected — keeping the eligibility and open-state predicates on the aggregate keeps the rule in one place and unit-testable without the DB.

### Per-request transaction; skip ≠ error
The new service is `@ApplicationScoped @Transactional`, matching `CompleteMonthEndTaskService`. A not-completable task is a filtered-out element (no exception), so it never rolls back; an unexpected error while saving does roll the whole request back. This gives the atomicity the proposal asked for while treating partial success as a normal business outcome.

### Response reuses the single-task shape
`completed[]` entries are the `MonthEndTask` schema (`taskId, month, type, projectId, subjectEmployeeId, status, completedBy`) mapped by the existing `MonthEndRestMapper.toDto(MonthEndTask)`. No project/user ref resolution is needed. The response body is `{ "completed": [ ... ] }`; already-done tasks are simply omitted, so re-issuing yields `{ "completed": [] }`. **Alternative considered:** the richer `MonthEndStatusOverviewEntry` shape (adds `project`, `subjectEmployee`, `canComplete`). Rejected as unnecessary — completion changes only `status`/`completedBy`; eligibility (`canComplete`) is unchanged, and the frontend patches store entities from the minimal shape.

### Role scope: project-lead only
The endpoint is secured with `@MegaRolesAllowed(Role.PROJECT_LEAD)` — tighter than the single endpoint's `{EMPLOYEE, PROJECT_LEAD}` because only leads can act on the accepted types. Eligibility still gates the actual work.

## Risks / Trade-offs

- **Snapshot drift between generation and request** → `eligibleProjectLeadIds` is recomputed from the month snapshot at request time; if a project's lead set changed after generation, the authz set and a task's `eligibleActorIds` could differ. Mitigation: both derive from the same per-month snapshot data, so drift within a month is not expected; any residual mismatch degrades safely to a per-task not-eligible skip, not an error.
- **`400` instead of `404` for unknown project** → diverges from the proposal's `404` wording. Mitigation: documented as a deliberate reuse of the existing mapper; a `404` mapping can be added later as an isolated change if the frontend needs to distinguish it.
- **Silent omission of already-done tasks** → the frontend gets no server-side count of "already done / not ready". Mitigation: accepted per product decision — the client derives any such count from its own store (column total − completed).
- **No size cap** → a project column is bounded by project headcount (tens), so one scoped query plus N saves in a single transaction is well within limits; no special rate/size limit is enforced.

## Migration Plan

Additive change — a new endpoint, one new query method, one new use case/service, and new OpenAPI schemas. No data migration, no changes to existing endpoints or persisted data. Rollback is removal of the new endpoint; nothing else depends on it.

## Open Questions

- None blocking. If the controlling page later needs to distinguish "unknown project" from "no context for month", revisit the `400` vs `404` mapping.
