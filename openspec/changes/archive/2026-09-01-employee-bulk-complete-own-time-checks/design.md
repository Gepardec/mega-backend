## Context

See proposal.md — Why. This design records decisions already embodied in the shipped implementation on `feature/813-815-integration`; it is written retroactively so the reasoning behind the endpoint is captured alongside its specs.

`EMPLOYEE_TIME_CHECK` tasks are generated per assigned employee with the employee as the sole eligible actor and completion policy `INDIVIDUAL_ACTOR` (`MonthEndTaskPlanningService`). `MonthEndTask.complete(actor)` enforces exactly two rules — the actor must be in `eligibleActorIds`, and an already-`DONE` task is a no-op returning itself — so an employee is the only actor who can ever complete their own time check.

The lead-facing bulk endpoint added by `bulk-complete-monthend-tasks` (`POST /monthend/{month}/tasks/complete/project-lead`) deliberately rejects `EMPLOYEE_TIME_CHECK` with `400`: it authorizes the caller as an eligible project lead via `MonthEndProjectContext.eligibleProjectLeadIds`, and a lead is never an eligible actor on a time check, so the request could only ever complete nothing. That endpoint's design also excludes, as a non-goal, letting a lead complete employees' time checks — an eligibility change this design does not make either.

## Goals / Non-Goals

**Goals:**
- One self-scoped, transactional endpoint that completes all of an employee's open time-check tasks for a month.
- Optional project narrowing, so the same endpoint serves both "confirm this project" and "confirm my whole month".
- Reuse the existing per-task completion behavior, completer tracking, and bulk response shape unchanged.

**Non-Goals:**
- No change to `EMPLOYEE_TIME_CHECK` eligibility — a lead or office user still cannot complete an employee's time check (this remains excluded, as in `bulk-complete-monthend-tasks`).
- No task-ID-list variant.
- No merge with the lead-facing bulk endpoint (see the first decision below).
- No dependency/readiness gate before completing a time check; none exists today.

## Decisions

### A separate endpoint, because the request contract differs — not because the operation does

Completing a task for yourself is **not** a different operation from completing one as a lead. The completion machinery is actor-agnostic: eligibility is fully expressed by `MonthEndTask.canBeCompletedBy(actor)` (membership in `eligibleActorIds`), the aggregate re-enforces it inside `complete(actor)`, and the lead-facing service's query → filter-on-open-and-eligible → complete → save sequence would produce the correct result for an employee actor unchanged. The only thing standing in the way is that service's authorization pre-check against `MonthEndProjectContext.eligibleProjectLeadIds`. Because `EMPLOYEE_TIME_CHECK` is `INDIVIDUAL_ACTOR` with exactly one eligible actor, that filter applied to an employee actor yields precisely their own task — no eligibility rule would need to bend.

What genuinely differs is the **request contract**, on three points:

1. *The authorization pre-check means something different per actor.* It exists only to separate "not your project" (`403`) from "your project, nothing to do" (`200` with an empty list). For a lead the right question is "are you a lead of this project"; for an employee there is no equivalent project-level relationship — the honest question is "do you have a task here", which the per-task eligibility filter already answers. Sharing the endpoint therefore means branching the authorization rule on the caller's role.
2. *`projectId` is required there and optional here.* The employee's actual need is "confirm my whole month", spanning projects. Making `projectId` optional on the shared endpoint would simultaneously grant leads a month-and-type-only request = "complete this column across every project I lead" — a larger capability nobody asked for, with its own authorization question.
3. *Per project, the employee case is not bulk at all.* Generation creates exactly one `EMPLOYEE_TIME_CHECK` per employee, project, and month, so a project-scoped request would complete exactly one task — which `POST /monthend/tasks/{taskId}/complete` already does, addressed by id instead of by scope. The only genuinely new capability is the cross-project sweep, and that is precisely what the shared endpoint's required `projectId` cannot express.

Points 2 and 3 are what justify a second endpoint; point 1 is its cost of admission.

**Alternative considered:** unify — make `projectId` optional, drop the type restriction, and dispatch the authorization step on the caller's role. Rejected, but not because it would be incorrect: it trades two contracts that each read unambiguously for one whose required fields, accepted types, and authorization rule all shift with the caller, and it would reverse the existing endpoint's `400`-on-`EMPLOYEE_TIME_CHECK` rule that the frontend relies on. If a future requirement needs lead-side cross-project completion as well, this unification becomes the better shape and should be revisited.

### Authorization is structural: the scope is the actor
The request carries no subject employee identifier, and the outbound query filters on `subjectEmployeeId = actor`. A caller therefore cannot address another employee's tasks regardless of their roles, so no explicit authorization check is needed beyond the employee role gate — there is no "not your task" case to reject. The per-task `canBeCompletedBy(actor)` filter is still applied before completing, so the aggregate's rule stays the single source of truth. **Alternative considered:** accept an employee id and check it equals the caller. Rejected — an identifier that must always equal the caller is redundant surface that invites misuse.

### Optional `projectId` instead of two endpoints
A missing `projectId` means "every project I have an open time check for this month"; a present one narrows to that project. The outbound query takes the project as a nullable filter. **Alternative considered:** separate paths for the per-project and whole-month cases. Rejected — same actor, same task type, same response; the only difference is one filter.

### Scope is restricted to open tasks in the query, not only in memory
The outbound query returns only open time-check tasks for the actor, and the service filters again on `isOpen() && canBeCompletedBy(actor)` before completing. The query filter keeps the result set small for an employee on many projects; the in-memory filter keeps the domain rule authoritative and unit-testable without a database. This differs from the lead-facing service, which fetches the whole `(month, project, type)` scope and filters entirely in memory — acceptable, because there the open/done split is exactly what the caller wants to see reflected in `completed[]`, and the scope is bounded by project headcount either way.

### Per-request transaction; skip is not an error
The service is `@ApplicationScoped @Transactional`, matching the other completion services. A not-completable task is a filtered-out element, never an exception, so it cannot roll the request back; an unexpected error while saving rolls the whole request back.

### Response reuses the bulk completion shape
The response body is `{ "completed": [ ... ] }` using the same `CompletedTasksResponse` schema as lead-side bulk completion, mapped by the existing task mapper. Already-done tasks are simply omitted, so re-issuing yields `{ "completed": [] }`. **Alternative considered:** a dedicated response schema. Rejected — the payload is identical, and sharing it lets the frontend reuse one store-update path for both bulk endpoints.

### Role scope: employee only
The endpoint requires the employee role. Project leads also hold the employee role, so a lead can complete their own time checks through it — acting as an employee, not as a lead.

## Risks / Trade-offs

- **Two bulk completion endpoints with similar names** (`/complete/project-lead` and `/complete/employee`) → Mitigation: the paths state the actor model, and the accepted-type rules make a wrong choice fail loudly (`400`) rather than silently completing nothing.
- **A whole-month request touches tasks across many projects in one transaction** → Mitigation: bounded by the number of projects an employee is assigned to (single digits); one scoped query plus N saves is well within limits.
- **No server-side count of "already done" tasks** → Mitigation: accepted, consistent with lead-side bulk completion; the client derives it from its own store.

## Migration Plan

Additive — a new endpoint, one new query method, one new use case/service, and one new request schema. No data migration, no changes to existing endpoints or persisted data. Rollback is removal of the new endpoint; nothing else depends on it.

## Open Questions

None.
