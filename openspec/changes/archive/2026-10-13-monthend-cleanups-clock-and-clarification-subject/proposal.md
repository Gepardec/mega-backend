# Proposal

## Why

Two small issues in the monthend context:

- The payroll-month services read the system clock directly. Their month logic can't be tested at fixed dates, and they break the project rule that time comes from an injected clock.
- The REST adapter decides the subject of a clarification based on the global project-lead role flag. If a non-lead sends another employee's id, it is silently replaced. A user who holds the role but doesn't lead the target project gets a project-level clarification, and the domain then rejects it.

Fixing both now keeps the upcoming `perspective-scoped-read-endpoints` change focused on the perspective model. That change reworks the same payroll-month services.

## What Changes

- The employee and project-lead payroll-month services take the current month from the injected clock. Behavior is unchanged.
- The rule that picks the clarification subject moves from the REST adapter to the clarification creation use case. It now depends on whether the actor is an eligible lead **of the target project in that month**, not on the global project-lead role:
  - No subject given, actor is an eligible lead: project-level clarification (unchanged).
  - No subject given, actor is not an eligible lead: the actor becomes the subject. This now also applies to users who hold the project-lead role but don't lead this project. Before, they got a validation error.
  - Another employee given as subject, actor is not an eligible lead: **BREAKING** the request is rejected with `403 Forbidden` instead of the subject being silently replaced with the actor.
- The REST adapter no longer checks roles when creating a clarification.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `monthend-rest-api`: the clarification-creation requirement now determines the subject by project-lead eligibility for the target project and month instead of the project-lead role, and rejects a foreign subject from a non-lead instead of ignoring it.

## Impact

- **Code**: `GetEmployeePayrollMonthService`, `GetProjectLeadPayrollMonthService`, `CreateMonthEndClarificationService`, `MonthEndResource.createMonthEndClarification`, and their tests.
- **API**: `POST /monthend/clarifications` returns `403` for a non-lead request that names another employee as subject. The request and response shapes are unchanged.
- **Frontend**: if the frontend sends `subjectEmployeeId` from an employee view, it must send the user's own id (or none). On the employee dashboard, a project lead who creates a clarification for themselves in a project they lead must send their own id as `subjectEmployeeId`. Without it, the request is a project-level clarification (same as today).
