## Why

The month-end read API is split by role: `/status-overview/employee` vs `/status-overview/project-lead`, and `/payroll-month/employee` vs `/payroll-month/project-lead`. Each pair returns the same model and differs only in **which rows** are selected, yet each one has its own endpoint, its own use case, and its own repository query. The duplication spreads to every client, which keeps two near-identical stores. Encoding the view as a role also hides what the view really is: the role the caller is *acting in* right now, which they choose per page.

Because the view is not modelled, privileges ignore it. A project lead who also works on a project they lead sees their own `PROJECT_LEAD_REVIEW` and `LEISTUNGSNACHWEIS` as completable on their employee dashboard. On the lead page, they see their own `EMPLOYEE_TIME_CHECK` as completable. Users find this confusing. The server is right to allow the action, but the page offers it in the wrong place.

## What Changes

- **New concept: actor capacity.** A caller reads month-end data in one of two capacities, `EMPLOYEE` or `CONTROLLING`, named after the UI page each one serves. `EMPLOYEE` requires the employee role, and `CONTROLLING` requires the project-lead role. Capacity is a read-side concept only: it selects the rows and decides which actions a read offers. It never changes what a command accepts.
- **BREAKING:** `GET /monthend/{month}/status-overview/employee` and `GET /monthend/{month}/status-overview/project-lead` are replaced by `GET /monthend/{month}/status-overview?capacity=EMPLOYEE|CONTROLLING`.
- **BREAKING:** `GET /monthend/payroll-month/employee` and `GET /monthend/payroll-month/project-lead` are replaced by `GET /monthend/payroll-month?capacity=EMPLOYEE|CONTROLLING`. The two resolution rules are unchanged and are selected by capacity.
- `capacity` is required. A request without it is rejected with `400`, and a request in the `CONTROLLING` capacity from a caller without the project-lead role is rejected with `403`.
- **BREAKING (behavior):** `canComplete` on overview entries now also requires that the task type is a duty of the requested capacity. `EMPLOYEE_TIME_CHECK` is an employee duty. `LEISTUNGSNACHWEIS`, `PROJECT_LEAD_REVIEW` and `ABRECHNUNG` are controlling duties. As a result:
  - a lead reviewing themselves sees their own review as read-only in the `EMPLOYEE` capacity and as completable in the `CONTROLLING` capacity
  - their own time check is completable only in the `EMPLOYEE` capacity

  Task completion itself is unchanged: an eligible actor can still complete any task they are eligible for, so self-review stays allowed.
- Clarification capability flags (`canResolve`, `canEditText`, `canDelete`) stay actor-based and do not depend on capacity.
- No deprecation period. The old endpoints are removed in this change.

## Capabilities

### New Capabilities

- `shared-actor-capacity`: the capacities a caller can read data in, the role each capacity requires, how capacity-scoped read endpoints accept and validate it, and the rule that capacity shapes reads but never command authorization. This change uses it in month-end; `unify-worktime-report-by-capacity` reuses it.

### Modified Capabilities

- `monthend-rest-api`: the role-suffixed status overview and payroll month endpoints are replaced by capacity-parameterized endpoints. The authenticated-actor and role-access requirements now refer to the capacity-scoped endpoints. The overview requirement no longer carries a clarification-creation scenario, which is already covered by the clarification creation requirement.
- `monthend-status-overview`: the employee and lead overviews become the `EMPLOYEE` and `CONTROLLING` capacity scopes, and `canComplete` additionally requires that the task type is a duty of the requested capacity.
- `monthend-task-aggregate`: each task type declares the capacity whose duty it is.
- `monthend-clarifications`: the overview clarification query is described by capacity scope instead of two role-named queries.

## Impact

- **API contract:** four operations are removed and two are added (`getMonthEndStatusOverview`, `getPayrollMonth`), plus a shared `Capacity` enum schema. The response shapes do not change. The frontend must switch to the new paths and send `capacity`. It also gets new `canComplete` values for a lead's own tasks. Frontend work is out of scope for this change.
- **Shared kernel:** a new `Capacity` type next to `Role`.
- **Domain:** month-end task types know which capacity they are a duty of. The task aggregate gets a capacity-aware completion check used only by reads. The existing actor-only check still governs commands.
- **Application:** four role-specific query use cases become two capacity-parameterized ones.
- **Persistence:** the role-named task and clarification overview queries become one scope-parameterized query each. The SQL does not change.
- **REST adapter:** one overview method and one payroll-month method, each gated by the role the capacity requires.
- **Unchanged:** commands (single and bulk completion, clarification create, edit, resolve and delete, and premature preparation), task generation, notification queries, and worktime endpoints (see `unify-worktime-report-by-capacity`).
- **Sequencing:** apply and archive before `unify-worktime-report-by-capacity` (which depends on `Capacity`) and before `intent-named-bulk-completion` (whose `monthend-rest-api` access delta builds on this change's version of that requirement).
