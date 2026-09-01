## Why

An employee assigned to several projects gets one `EMPLOYEE_TIME_CHECK` task per project each month, and today confirms each one individually — one completion request per project, even though the confirmation is a single act ("my times for this month are correct"). Employees want to confirm a whole month at once, or all remaining time checks of one project at once.

The lead-facing bulk endpoint (`POST /monthend/{month}/tasks/complete/project-lead`) cannot serve this: only the subject employee is an eligible actor on an `EMPLOYEE_TIME_CHECK` task, so that endpoint rejects the type outright. Self-service needs its own endpoint with its own actor model.

> **Note:** This change is written retroactively — the behavior described here is already implemented and tested on `feature/813-815-integration`. It exists so the shipped endpoint is captured in the specs instead of remaining undocumented.

## What Changes

- Add an employee-scoped bulk completion endpoint `POST /monthend/{month}/tasks/complete/employee` carrying the month in the path and an optional `{ projectId }` in the body.
- The endpoint completes every `EMPLOYEE_TIME_CHECK` task in that month whose subject is the authenticated caller and that is currently open, in one transaction.
- `projectId` is **optional**: when given, the scope is that one project; when omitted, the scope is every project the caller has an open time check for in that month.
- The actor is always the subject employee — the caller can never complete another employee's time check, and no eligibility change is introduced. Tasks the caller is not eligible for are skipped, never failed.
- Partial success is the normal outcome: already-done tasks are silently skipped, and only tasks actually transitioned to done are returned.
- The response reuses the existing bulk completion shape `{ "completed": [ <month-end task> ] }`, so the frontend updates its store exactly as it does for lead-side bulk completion.
- Authorization is the employee role plus the self-scoped query — there is no project-level authorization step, because a caller can only ever address their own tasks.

## Capabilities

### New Capabilities

_None. This change extends existing month-end capabilities rather than introducing a new one._

### Modified Capabilities

- `monthend-task-completion`: add a requirement for an employee completing their own open time-check tasks for a month, optionally narrowed to one project, with partial success.
- `monthend-rest-api`: add a requirement defining the `POST /monthend/{month}/tasks/complete/employee` endpoint contract (request body with optional project scope, response shape, role restriction) and include it in the shared, role-secured month-end action endpoints.

## Impact

- **API contract**: new path `POST /monthend/{month}/tasks/complete/employee` and one new request schema in the canonical month-end OpenAPI document; the existing bulk completion response schema is reused. A new generated API interface method the REST adapter implements.
- **REST adapter**: new resource method, secured for the employee role, that parses the month, treats a missing `projectId` as "all projects", and maps completed tasks to the existing task response shape.
- **Application layer**: a new inbound use case and service orchestrating self-scoped bulk completion, reusing the existing `MonthEndTask` completion behavior.
- **Persistence**: one new outbound repository query returning the caller's open time-check tasks for a month, with an optional project filter.
- **No changes** to the `MonthEndTask` aggregate, task generation, eligibility rules, or existing endpoints — in particular, the lead-facing `POST /monthend/{month}/tasks/complete/project-lead` keeps rejecting `EMPLOYEE_TIME_CHECK`.
