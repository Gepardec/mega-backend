# Design

## Context

Today each month-end read exists twice (see proposal.md, Why):

```
REST                                   use case                                   repository
/status-overview/employee     -> GetEmployeeMonthEndStatusOverviewUseCase    -> findEmployeeVisibleTasks, findAllEmployeeClarifications
/status-overview/project-lead -> GetProjectLeadMonthEndStatusOverviewUseCase -> findLeadProjectTasks,     findAllProjectLeadClarifications
/payroll-month/employee       -> GetEmployeePayrollMonthUseCase              -> findOpenSubjectTasks
/payroll-month/project-lead   -> GetProjectLeadPayrollMonthUseCase           -> findLeadProjectTasks
```

Constraints that shape the approach:

- Role gating happens in the REST adapter today, through `@MegaRolesAllowed`, which checks `AuthenticatedActorContext.roles()` and throws the shared `ForbiddenException` (mapped to `403`). Use cases receive a `UserId` only and never see roles (`hexagon-boundary-conventions`).
- `canComplete` is computed in `MonthEndRestMapper` via `task.canBeCompletedBy(actorId)`, which is also the check single and bulk completion use. That check must not change, because commands must keep allowing self-review.
- The generator runs with `useBeanValidation=true`, so a required query parameter is rejected with `400` before the resource method runs.
- `Role` already lives in `shared.domain.model`, and the shared kernel is designated for shared authorization vocabulary.

## Goals / Non-Goals

**Goals:**
- One endpoint, one use case and one repository query per month-end read model, parameterized by capacity.
- Capacity-aware `canComplete` without touching command authorization.
- A `Capacity` type that worktime can reuse unchanged.

**Non-Goals:**
- Changing any command, including clarification subject inference (`explicit-clarification-subject`) and bulk path naming (`intent-named-bulk-completion`).
- Making clarification flags capacity-aware.
- Changing the payroll month rules or the task and clarification selection SQL.
- Frontend changes.

## Decisions

### D1 — `Capacity` is its own shared-kernel enum, not a subset of `Role`

`Capacity { EMPLOYEE, CONTROLLING }` lives in `shared.domain.model` with `Role requiredRole()`.

`CONTROLLING` is named after the page it serves in the UI, not after the role it requires (`requiredRole()` returns `Role.PROJECT_LEAD`). A capacity is the perspective the caller acts in, and its name should match what the user sees. It should not repeat the permission, so if the role or its name changes later, the API value does not have to change.

*Alternative:* pass `Role` directly. Rejected: `Role` also contains `OFFICE_MANAGEMENT` and may grow. A capacity is a closed set of reading perspectives, and the contract should only offer values that are valid. Keeping the two separate also keeps the meaning clear: a role is a permission, and a capacity is the role you are acting in.

### D2 — Gate in the REST adapter, not in the use case

Each capacity-scoped method keeps the baseline `@MegaRolesAllowed(Role.EMPLOYEE)` (any authenticated user of these pages). It then checks `authenticatedActorContext.hasRole(capacity.requiredRole())` and throws the existing `ForbiddenException` when the check fails. A small shared helper next to `AuthenticatedActorContext` (e.g. `requireCapacity(Capacity)`) keeps this to one line, so worktime can reuse it.

*Alternatives:*
- Gate in the use case. Rejected: use cases would need the actor's roles, which breaks the convention of passing explicit `UserId`s.
- No gate, letting a non-lead get an empty lead scope. Rejected: a silent empty response hides client bugs. `403` is explicit and matches today's behavior for the old lead endpoints.

### D3 — Contract shape

- `Capacity` becomes a string enum schema in `schemas/shared.yaml` (uppercase values, like `MonthEndTaskType`), referenced from a required `capacity` query parameter.
- `GET /monthend/{month}/status-overview` (operationId `getMonthEndStatusOverview`) and `GET /monthend/payroll-month` (operationId `getPayrollMonth`). The response schemas are unchanged.
- The four old paths and operationIds are deleted (hard cut).
- A missing `capacity` is rejected with `400` by bean validation. An unknown value fails JAX-RS parameter conversion, and the framework picks the status (for query parameters this is typically `404`). The spec deliberately only requires "a client error", and we accept the framework default rather than adding a custom converter or mapper. A REST test pins the observed status so a framework upgrade that changes it is noticed.

### D4 — One query use case per read model

- `GetMonthEndStatusOverviewUseCase.getOverview(UserId actorId, YearMonth month, Capacity capacity)`.
- `GetPayrollMonthUseCase.getPayrollMonth(UserId actorId, Capacity capacity)`. A `switch` over `capacity` selects one of the two existing rules, kept as private methods with their current logic and queries. The employee rule keeps using `findOpenSubjectTasks`, and the lead rule uses the scoped task query from D5.

*Alternative:* keep four use cases and only merge the endpoints. Rejected: the duplication sits in the use cases and repository queries, so that would only move the problem.

### D5 — Capacity scope as a domain value, one repository query per aggregate

`MonthEndScope` in `monthend.domain.model` is a sealed interface with two records, `SubjectEmployee(UserId)` and `LedProjects(UserId)`, plus a factory `MonthEndScope.of(Capacity, UserId)`.

- `MonthEndTaskRepository.findTasks(YearMonth, MonthEndScope)` replaces `findEmployeeVisibleTasks` and `findLeadProjectTasks`.
- `MonthEndClarificationRepository.findClarifications(YearMonth, MonthEndScope)` replaces `findAllEmployeeClarifications` and `findAllProjectLeadClarifications`.
- The adapters pattern-match on the scope and run the existing queries unchanged. The `findOpen*Clarifications` methods used elsewhere are untouched.

*Alternative:* keep the two role-named methods and branch in the use case. That is viable and slightly less code. The scope type was chosen because it names the concept once, and the payroll lead rule and the overview then share the same selection instead of each picking a method.

### D6 — Duty on the task type, capacity-aware check on the aggregate

- `MonthEndTaskType.dutyOf()` returns `Capacity`: `EMPLOYEE_TIME_CHECK` maps to `EMPLOYEE`, all other types to `CONTROLLING`.
- `MonthEndTask.isCompletableIn(Capacity capacity, UserId actorId)` returns `canBeCompletedBy(actorId) && type.dutyOf() == capacity`.
- `canBeCompletedBy` stays unchanged and remains the only check used by commands.
- `MonthEndRestMapper` receives `Capacity` as an additional `@Context` and calls `isCompletableIn` in the task `@AfterMapping`. The clarification `@AfterMapping` keeps using the actor-only methods.

*Alternative:* compute the duty check in the REST mapper. Rejected: which capacity a task type belongs to is a business fact and should live on the type.

### D7 — The mapper keeps the enrichment

The REST adapter still resolves `ProjectRef` and `UserRef` and computes the flags (`monthend-status-overview`, `monthend-rest-api` enrichment requirement). The only change is the added capacity context.

## Risks / Trade-offs

- [The frontend breaks on deploy (hard cut)] → Agreed explicitly. Ship together with the frontend migration. The proposal lists the exact path and behavior changes for that work.
- [A lead's own tasks change `canComplete` in both views] → This is the intended fix. Covered by status-overview scenarios and REST tests for both capacities.
- [The status for an unknown `capacity` is decided by the framework] → Accepted. Pinned by a REST test (D3).
- [Role and task eligibility can disagree] A lead whose `PROJECT_LEAD` role was revoked can no longer open the controlling capacity, even though generation-time eligibility still lists them on the tasks. This is the same as today's lead endpoints, and they can still complete tasks via single-task completion.
- [`intent-named-bulk-completion` also modifies the `monthend-rest-api` access requirement] → Apply and archive this change first. That change's delta is written against the requirement text produced here.

## Migration Plan

Deploy together with the frontend release that switches to the new paths. Rollback means reverting the backend and the frontend together. There is no data migration and the persistence schema does not change.
