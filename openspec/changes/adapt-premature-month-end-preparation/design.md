## Context

`PrematureMonthEndPreparationUseCase` lets an employee bootstrap their own month-end obligations before the scheduled generation runs. The current implementation takes a caller-supplied `projectId` and prepares exactly one project context per call. The employee must call the endpoint once per project, and the use case returns the created tasks and clarification as a body.

The required behaviour is: one call prepares **all** projects the employee is assigned to, with a mandatory clarification reason fanned out to each project's leads. Contexts that are already prepared (tasks already exist) are silently skipped.

## Goals / Non-Goals

**Goals:**
- Remove `projectId` from the use case input; discover all assigned projects automatically
- Make `clarificationText` required — premature preparation is always a communication act
- Return `void`; REST endpoint responds 204 No Content
- Skip a project context if tasks already exist for that month/project/subject (idempotency)
- Create exactly one clarification per newly prepared project context
- Delete dead code: `MonthEndPreparationResult`, `MonthEndTaskRepository.findByBusinessKey`

**Non-Goals:**
- Changing the scheduled `GenerateMonthEndTasksUseCase` flow (it keeps `ensureTask` / business-key upsert)
- Adding a project-filtering preference to the request
- Changing `MonthEndClarification` domain model (projectId stays required)

## Decisions

### 1. Project-context guard: `existsForSubjectEmployee` replaces per-task `findByBusinessKey`

**Chosen:** Add `MonthEndTaskRepository.existsForSubjectEmployee(month, projectId, subjectEmployeeId)`. Before creating anything for a project, check this once. If tasks exist → skip the entire project context (tasks + clarification). If not → proceed with direct `save` for all tasks and the clarification.

**Why over per-task upsert:** The `ensureTask` find-or-create pattern was appropriate for the scheduled generator, which needs to be fully idempotent at task granularity. Premature preparation is an intentional first-time action — if any tasks exist, the context was already prepared. A single guard query is cleaner, fewer DB round-trips, and the semantics are more honest.

**Alternative rejected:** Keep per-task `findByBusinessKey` upsert and add a "was anything new?" flag. More complexity for no benefit in this use case.

### 2. Direct port injection instead of `MonthEndEmployeeProjectContextService`

**Chosen:** `PrematureMonthEndPreparationService` injects `MonthEndProjectSnapshotPort`, `MonthEndUserSnapshotPort`, and `MonthEndProjectAssignmentPort` directly. `MonthEndEmployeeProjectContextService` is no longer a dependency.

**Why:** The context service resolves one project context given a `projectId`. It has no multi-project API. Rather than adding a new method to a service that has a single-project identity, the application service owns the fan-out logic directly and loads `activeUsersById` once, reusing it across all project iterations.

**Alternative rejected:** Add `resolveAllForActor(month, actorId)` to `MonthEndEmployeeProjectContextService`. This would couple a shared domain service to an actor-centric use case shape and obscure why the method exists.

### 3. `clarificationText` is required

**Chosen:** `clarificationText` becomes a required, non-blank field in both the port signature and the OpenAPI schema.

**Why:** Premature preparation exists so the employee can communicate a reason (leave, travel, etc.) to their project leads. A preparation without a reason is not meaningfully different from the scheduled generation — it has no value as early action. Requiring the text makes the intent explicit.

**Alternative rejected:** Keep optional, create clarification only when provided. Allows silent early preparation, which undermines the feature's purpose.

### 4. One clarification per newly prepared project context (fan-out)

**Chosen:** For each project where tasks are newly created, create one `MonthEndClarification` with that project's eligible lead IDs as recipients. The single `clarificationText` is reused verbatim across all project clarifications.

**Why:** `MonthEndClarification` is project-scoped by domain invariant (`projectId` is required, non-null). Leads are per-project. A clarification must be routed to the right lead set.

**Alternative rejected:** A single month-level clarification with `projectId = null`. Would require relaxing a domain invariant and changing the clarification model, which is out of scope.

### 5. 204 No Content response

**Chosen:** `POST /monthend/preparations` returns 204 after successful preparation.

**Why:** The client has no demonstrated need to process the newly created tasks or clarifications immediately after submission — the status overview endpoints already provide that. Returning a body would require maintaining `MonthEndPreparationResult` and its REST mapping, which adds complexity for no client benefit.

**Alternative rejected:** Return a summary DTO (tasks created, clarifications created, projects skipped). Useful for debugging but not required by any known client use case.

## Risks / Trade-offs

- **N ZEP API calls for assignment lookup** (one `findAssignedUsernames` call per active project) → Low risk: most employees are on ≤ a handful of projects; ZEP integration is already called this way in the scheduled generator. Caffeine cache provides a safety net for hot paths.

- **Race condition on simultaneous prepare calls** → Two concurrent requests for the same actor could both pass the `existsForSubjectEmployee` guard before either saves. Tasks are protected by the DB unique constraint on business key (will reject the duplicate). Clarifications have no equivalent constraint — a race could create two clarifications for the same project context. This is an accepted edge case given the near-zero probability of an employee triggering two simultaneous prepare requests.

- **`activeUsersById` loaded once but iteration calls ZEP per project** → The user snapshot is a single query and is efficient. The ZEP assignment calls cannot be batched with the current port contract. This is consistent with how the scheduled generator works today.
