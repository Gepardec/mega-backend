## Context

The month-end status overview is currently served by a single `GetMonthEndStatusOverviewUseCase` from a shared REST endpoint (`MonthEndSharedResource`). Both employees and project leads call the same endpoint with the same actor ID, but their information needs are different: employees want to see their own personal obligations, while project leads want a comprehensive view of all tasks across the projects they lead.

The current `findVisibleTasksForActor` query tries to serve both audiences with a combined predicate (eligible actor OR subject employee OR lead-of-project). This causes incorrect results: a lead who is also assigned as an employee on a project they don't lead sees that project in their lead overview.

The existing worklist already follows a split pattern — `GetEmployeeMonthEndWorklistUseCase` and `GetProjectLeadMonthEndWorklistUseCase` are separate. This change brings the overview into the same pattern.

## Goals / Non-Goals

**Goals:**
- Correct the visibility bug: leads must not see projects they are only an employee on.
- Split the use case, service, repository method, and REST endpoint by role.
- Align with the existing worklist split pattern.

**Non-Goals:**
- Changing the `MonthEndStatusOverviewItem` response model or the `canComplete` flag semantics.
- Changing task generation or any other month-end use case.
- Soft-migrating the old shared endpoint — it is simply removed.

## Decisions

### D1: Two separate use cases, not a role parameter

**Decision:** Introduce `GetEmployeeMonthEndStatusOverviewUseCase` and `GetProjectLeadMonthEndStatusOverviewUseCase` as separate ports and services.

**Rationale:** The two use cases answer different domain questions ("what am I personally doing?" vs. "how are my projects doing?") and differ in their query, their visibility contract, and their REST resource. A role parameter would conflate these into a single method with branching logic — the same anti-pattern that caused the bug. Separate use cases also align with the existing worklist split and keep each service independently testable.

**Alternative considered:** Single use case with a `Role` parameter — rejected because it couples unrelated visibility semantics into one method and obscures intent.

---

### D2: Employee visibility = subjectEmployeeId only

**Decision:** `findEmployeeVisibleTasks` filters by `task.subjectEmployeeId = actorId`.

**Rationale:** The employee overview is about "what is happening to me." The `subjectEmployeeId` condition captures exactly:
- `EMPLOYEE_TIME_CHECK` / `LEISTUNGSNACHWEIS`: employee is subject → visible, and since employee is also in `eligibleActorIds`, `canComplete=true`.
- `PROJECT_LEAD_REVIEW`: employee is subject but not eligible → visible, `canComplete=false` (read-only).
- `ABRECHNUNG`: subject is null → not visible (correct).

**Alternative considered:** `eligibleActorIds` OR `subjectEmployeeId` (the original combined query) — rejected because it exposes tasks the employee has no personal connection to as subject, and was already replaced by the split approach.

---

### D3: Lead visibility = all tasks for led projects via correlated EXISTS, no type filter

**Decision:** `findLeadProjectTasks` uses a correlated EXISTS subquery — "return all tasks for this month where there exists any task on the same project and month in which the actor is in `eligibleActorIds`." No explicit task type filter on the inner query.

**Rationale:** `MonthEndTaskPlanningService` only places leads in `eligibleActorIds` of `ANY_ELIGIBLE_ACTOR` tasks (`PROJECT_LEAD_REVIEW`, `ABRECHNUNG`). This is a domain invariant enforced at task generation time. The type filter would be redundant coupling to task type names. Omitting it keeps the query simpler and trusts the invariant.

**Edge case accepted:** If a lead is also assigned as an employee on their own project, their personal ETC/LN tasks appear in the lead overview (because the EXISTS finds a PLR/ABRECHNUNG task for the same project). This is intentional — the lead should see the full picture of their projects, including their own participation.

**Alternative considered:** Add `lead.type in (PLR, ABRECHNUNG)` as a safety guard — rejected to avoid coupling the query to type names and as redundant with the generation-time invariant.

---

### D4: Breaking REST change — remove shared endpoint, add role-specific endpoints

**Decision:** Remove the overview from `MonthEndSharedResource`. Add employee overview to `MonthEndEmployeeResource` and lead overview to `MonthEndProjectLeadResource`.

**Rationale:** The two overviews have different semantics, different use cases, and different access patterns. Keeping them on a shared endpoint requires the server to infer the caller's intent — which is what caused the bug in the first place. Role-specific endpoints make the contract explicit and consistent with the existing worklist endpoints.

**Implication:** Frontend must be updated to call the correct role-specific endpoint.

---

### D5: canComplete semantics unchanged

**Decision:** `canComplete` remains `task.eligibleActorIds().contains(actorId)` in both overviews.

**Rationale:** This already correctly distinguishes actionable vs read-only for both views. A lead viewing an employee's ETC has `canComplete=false` (not in eligibleActors). A lead viewing a PLR has `canComplete=true`. No change needed.

## Risks / Trade-offs

- **Breaking API change**: The shared overview endpoint is removed. The frontend must be updated in the same release. → Coordinate with frontend; this is a required coupled change.
- **Generation invariant assumption**: The lead query trusts that leads only appear in `eligibleActorIds` of `ANY_ELIGIBLE_ACTOR` tasks. If task generation ever places a lead in an `INDIVIDUAL_ACTOR` task's `eligibleActorIds` by mistake, the lead would see additional unexpected tasks. → The domain model's `validateTypeSpecificInvariants` enforces this at construction time, making the risk low.
- **Intermediate fix must be reverted**: `findVisibleTasksForActor` currently holds the combined a+b+c query with type filter. This must be cleaned up as part of this change to avoid leaving dead code.
