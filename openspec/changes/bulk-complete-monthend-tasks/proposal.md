## Why

On the controlling (project-lead) page, completing a task type for a whole project today means expanding the project and marking each employee's task "Erledigt" one at a time — one completion request per employee. Project leads want to complete an entire task column for a project in a single action. Because the backend is the authority on which tasks a caller may complete, the action should carry the **intent** (project + type + month), not a client-assembled list of task identifiers.

## What Changes

- Add a scoped bulk completion endpoint `POST /monthend/tasks/complete` accepting `{ month, projectId, type }`.
- The endpoint completes every task of that type for that project, in that month, that is currently open and that the authenticated actor is eligible to complete — in one transaction.
- `type` is restricted to the lead-completable, employee-scoped types `LEISTUNGSNACHWEIS` and `PROJECT_LEAD_REVIEW`. `EMPLOYEE_TIME_CHECK` (only its subject employee is eligible) and `ABRECHNUNG` (a single project-level task, out of scope) are rejected with `400`.
- Partial success is the normal outcome: tasks that are already done or not currently completable are silently skipped, never failed. Only tasks actually transitioned to done are returned.
- The response reuses the existing single-task completion shape: `{ "completed": [ <month-end task> ] }`, one entry per completed task, so the frontend updates its store exactly as it does for single-task completion.
- Authorization mirrors the existing single-task completion flow: the actor must be an eligible project lead of the project (otherwise `403`). An unknown or inactive project for that month is rejected with `400`.
- No domain behavior changes — bulk completion is an orchestration over the existing per-task completion rules.

## Capabilities

### New Capabilities

_None. This change extends existing month-end capabilities rather than introducing a new one._

### Modified Capabilities

- `monthend-task-completion`: add a requirement for completing a scoped set of same-type tasks for a project in one operation, with per-task eligibility filtering and partial success.
- `monthend-rest-api`: add a requirement defining the `POST /monthend/tasks/complete` endpoint contract (request body, accepted types, response shape, and `400`/`403` behavior) and include it in the shared, role-secured month-end action endpoints.

## Impact

- **API contract**: new path `POST /monthend/tasks/complete` and two new schemas (bulk completion request and response) in the canonical month-end OpenAPI document; a new generated API interface method the REST adapter implements.
- **REST adapter**: new resource method that validates the requested type, resolves the project's month-end context, authorizes the actor as an eligible project lead, and maps completed tasks to the existing single-task response shape.
- **Application layer**: a new inbound use case and service orchestrating scoped bulk completion, reusing the existing `MonthEndTask` completion behavior and the existing project-context resolution.
- **Persistence**: one new outbound repository query returning the tasks for a `(month, project, type)` scope.
- **No changes** to the `MonthEndTask` aggregate, task generation, or existing endpoints.
