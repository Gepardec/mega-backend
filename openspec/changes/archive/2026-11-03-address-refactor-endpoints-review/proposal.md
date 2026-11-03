# Proposal

## Why

The code review of `feature/refactor-endpoints` found seven issues in the unified work time report, month-end clarification creation, and the controlling payroll month. Most are duplicated rules, duplicated loading, or dead code. One is a behavior regression: work time report entries lost the stable order the frontend was built against. This change fixes all seven before the branch is merged.

## What Changes

- **Work time report order (finding 1):** Entries are sorted again, by employee display name and then by project name, in both capacities. This replaces the requirement that said entry order is undefined. The employee capacity only has one employee, so it ends up ordered by project name, the same as before the refactor.
- **Unresolved project logging (finding 2):** Bookings on projects that cannot be resolved are reported from one place per report, not from inside a capacity-specific resolver. In the employee capacity a missing project snapshot is unexpected and is logged as a warning. In the controlling capacity, bookings on projects the lead doesn't lead are expected and are logged at debug level. The hours still count towards `employeeMonthTotalHours`, as the spec already requires.
- **Duplicate context loading (finding 3):** Creating a clarification with a subject loads the project and user snapshots once, not twice.
- **Single creator rule (finding 4):** The rule "creator must be the subject employee or an eligible project lead" lives only in the clarification's creation factory and rejects with the authorization error (`403 Forbidden` over REST). The application service no longer repeats it. Loading a stored clarification no longer checks the creator, because that is a creation-time authorization rule, not a structural invariant. REST behavior is unchanged: `403` stays `403`. Internal callers (ZEP mail import, self-service preparation) get the authorization error where they previously got a validation error. The ZEP mail import catches all exceptions, so its outcome doesn't change.
- **Project ID enumeration (finding 5):** Accepted, no behavior change. `main` also resolved the project before checking the caller, and a lead can only be identified after that lookup. Project IDs are not secret to authenticated staff. An unknown or inactive project keeps returning the current status, `400 Bad Request` (the review assumed `404`).
- **Dead employee filter (finding 6):** The work time report no longer filters attendances for "unknown employees". Every fetched key comes from the known-employee map, so the filter could never remove anything.
- **Cheap payroll month check (finding 7):** The controlling payroll month checks whether any task exists for the lead's projects with an existence query, instead of loading and mapping every task.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `worktime-report`: the "entries have no defined order" requirement is replaced by a defined order (employee display name, then project name).
- `monthend-clarifications`: the system-actor creation requirement refers to the creator rule by behavior, not by the old validation method name, now that the rule moves into the creation factory. No caller-observable change.

Reviewed and not affected: `worktime-rest-api` (it doesn't state an order; only the OpenAPI response description changes), `monthend-rest-api` (it already requires `403` for an ineligible creator), `monthend-employee-project-context`, `monthend-task-aggregate`, `monthend-status-overview`.

## Impact

- **Code:** `WorkTimeReport`, `GetWorkTimeReportService`, `WorkTimeScope` (worktime). `MonthEndClarification`, `CreateMonthEndClarificationService`, `GetPayrollMonthService`, `MonthEndTaskRepository` and its persistence adapter (monthend).
- **API:** `GET /worktime/{payrollMonth}` returns entries in a defined order again. The OpenAPI response description is updated to match. No other contract changes.
- **Tests:** Unit tests for the report assembly, the work time report service, the clarification aggregate and service, the payroll month service, and the task repository adapter.
