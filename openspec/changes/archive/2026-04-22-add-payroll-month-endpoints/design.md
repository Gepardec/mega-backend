## Context

The legacy backend resolves the "active payroll month" via a `PayrollMonthProvider` in the REST layer — a CDI-qualified bean injected into resource implementations. Two variants exist: one for employees (stateful check against step entries) and one for management/project-leads (always previous month). Neither has been migrated to the hexagon.

The frontend calls a payroll-month endpoint on initial page load to anchor subsequent data fetches (monthend status overview, worktime). Without this in the hexagon, the frontend must continue to rely on the legacy backend for this bootstrapping step.

## Goals / Non-Goals

**Goals:**
- Add `GET /monthend/payroll-month/employee` and `GET /monthend/payroll-month/project-lead` to the hexagon
- Place all logic in the `monthend` bounded context (application layer)
- Reuse the existing `MonthEndTaskRepository.findOpenEmployeeTasks` port without modification

**Non-Goals:**
- Migrating or touching the legacy `PayrollMonthProvider` — it stays as-is until the legacy is decommissioned
- Adding payroll month resolution to the `worktime` context
- Introducing any new outbound port

## Decisions

### Decision: payroll-month endpoints belong in the `monthend` context

**Rationale**: The employee rule depends directly on monthend task state (`findOpenEmployeeTasks`). Placing it in `monthend` requires no cross-BC dependency. Placing it in `worktime` would require `worktime` to reach into `monthend` state via a new outbound port, violating the BC boundary.

**Alternative considered**: `shared` context — rejected because the concept is not truly cross-cutting; only the frontend treats it as a bootstrapping step, not something multiple BCs need.

### Decision: Two separate use cases — `GetEmployeePayrollMonthUseCase` and `GetProjectLeadPayrollMonthUseCase`

**Rationale**: The rules are different in kind, not just parameterisation. The employee rule queries repository state; the project-lead rule is a pure date computation. Separate use cases keep each testable in isolation and leave a clear seam to evolve the project-lead rule independently in future.

**Alternative considered**: Single use case with a role parameter — rejected because it merges two distinct policies into one place, complicating future changes to either rule.

### Decision: Drop the legacy "14th of month" gate

**Rationale**: The gate was a conservative buffer — "don't advance to the current month until we're halfway through it." The new rule is simpler and more correct: the month advances the moment the actor has no open tasks, regardless of calendar date. There is no business requirement for the gate in the hexagon.

### Decision: Empty task list (no tasks generated yet) resolves to current month

**Rationale**: `findOpenEmployeeTasks` returns an empty list both when all tasks are done and when no tasks exist yet. Treating both as "move forward" is consistent with the rule's intent: nothing is blocking the actor. This edge case only arises in the first month of use.

### Decision: `MonthEndTaskRepository.findOpenEmployeeTasks` is sufficient — no new port

**Rationale**: The existing query returns tasks that are open for a given employee and month. An empty result means all employee-owned tasks for that month are done. No new query or port is needed.

### Decision: Endpoints are added to `MonthEndResource` as two new methods

**Rationale**: Consistent with the existing pattern in `MonthEndResource`, which already hosts both employee and project-lead endpoints with per-method role guards. Dedicated sub-resources would add class overhead for two simple read methods.

### Decision: Response is a plain `YearMonth` string (e.g. `"2026-03"`)

**Rationale**: The only information the frontend needs is the resolved month. A wrapper object adds no value. Consistent with the worktime endpoints that accept `YearMonth` as a string path/query param.

## Risks / Trade-offs

- **Empty-task-list ambiguity** → The "no tasks yet" and "all tasks done" states are indistinguishable at the repository level and both resolve to current month. This is an accepted simplification; it only affects the first calendar month of use and the behaviour is reasonable in both cases.

- **Project-lead rule is static** → Always returning previous month may need revision if business rules change (e.g. a project-lead gets the same smart-check as employees). The separate use case provides the right seam for this without touching the employee path.

- **Legacy and hexagon endpoints coexist** → Both `GET /worker/payrollMonth` (legacy) and `GET /monthend/payroll-month/employee` will exist simultaneously until the legacy is decommissioned. This is intentional and not a risk — the frontend migrates when ready.
