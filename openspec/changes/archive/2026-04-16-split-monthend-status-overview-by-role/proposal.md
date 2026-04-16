## Why

The single `GetMonthEndStatusOverviewUseCase` serves both employees and project leads from a shared endpoint, but their visibility semantics are fundamentally different. This causes a bug: project leads see tasks from projects where they are only a subject employee (not a lead), polluting their overview with projects they have no lead responsibility for.

## What Changes

- **BREAKING** Remove the status overview from the shared endpoint; add role-specific overview endpoints to the employee and project-lead resources.
- Split `GetMonthEndStatusOverviewUseCase` into `GetEmployeeMonthEndStatusOverviewUseCase` and `GetProjectLeadMonthEndStatusOverviewUseCase`.
- Employee overview shows tasks where the employee is the `subjectEmployeeId` (own ETC/LN tasks and PLR tasks about them).
- Lead overview shows all tasks for projects the lead leads — including employee ETC/LN tasks (read-only) as well as PLR and ABRECHNUNG tasks they can act on.
- Rename `findVisibleTasksForActor` → `findEmployeeVisibleTasks` with simplified query (subject employee filter only).
- Add `findLeadProjectTasks` to the repository port with a correlated EXISTS subquery.
- Revert the intermediate combined (a+b+c) query that was added as a workaround.

## Capabilities

### New Capabilities

*(none)*

### Modified Capabilities

- `monthend-status-overview`: Extend the existing spec to define both employee and lead visibility semantics separately, following the same dual-role pattern as `monthend-task-worklist`.
- `monthend-rest-api`: Move the status overview from the shared endpoint requirement to the employee and project-lead endpoint requirements; update the actor-identity requirement to reflect the split.

## Impact

- `MonthEndTaskRepository` port (outbound): rename + new method
- `MonthEndTaskRepositoryAdapter`: two updated/new queries
- `GetMonthEndStatusOverviewService`: renamed to employee variant
- New `GetProjectLeadMonthEndStatusOverviewService`
- `MonthEndSharedResource`: overview endpoint removed
- `MonthEndEmployeeResource`: employee overview endpoint added
- `MonthEndProjectLeadResource`: lead overview endpoint added
- `openspec/specs/monthend-status-overview/spec.md`: extended to cover both roles
- `openspec/specs/monthend-rest-api/spec.md`: shared overview → role-specific
- Tests: existing adapter test updated, new lead overview adapter test added
