## Why

The legacy `syncUpdateEmployeesWithoutTimeBookingsAndAbsentWholeMonth()` workflow auto-closes step entries for employees absent an entire payroll month, but it operates against the legacy `StepEntry` model with a flat reason string. As the `monthend` bounded context becomes the authoritative model for month-end workflows, this automation must be migrated to complete `MonthEndTask` records and create structured `MonthEndClarification` records instead.

## What Changes

- **New**: `Role.SYSTEM` added to the shared role enum to represent the MEGA application itself as a trusted actor
- **New**: System actor seeded in `hexagon_users` via Liquibase with a well-known UUID; exposed as `SystemActor.USER_ID` domain constant
- **New**: `GetEmployeeAbsencesUseCase` in the `worktime` BC — canonical source for ZEP absence data
- **New**: `Absence` and `AbsenceType` domain types in the `worktime` BC
- **New**: `MonthEndEmployeeAbsencePort` outbound port in `monthend` BC; implemented by an ACL adapter that calls the `worktime` use case and filters non-qualifying absence types
- **New**: `CompleteTasksForAbsentEmployeeUseCase` in `monthend` BC — per-employee atomic operation that completes all open subject tasks and creates one clarification per assigned project when the employee was absent the whole month
- **New**: `AbsentEmployeeMonthEndScheduler` — fans out the use case over all active users on the last working day of the month
- **Modified**: `MonthEndTask` — new `completeBySystem()` method; `validateCompletionState` allows `SystemActor.USER_ID` as `completedBy`
- **Modified**: `MonthEndClarification` — new `createBySystem()` factory method; `canDelete` blocks deletion of system-created clarifications

## Capabilities

### New Capabilities

- `system-actor`: A trusted system identity (`Role.SYSTEM`) that can act as an authorized principal in monthend domain operations without being a human user. Used for automated task completion and clarification creation.
- `worktime-absences`: Query an employee's absences for a given month from ZEP, exposed as a formal use case in the `worktime` BC.
- `monthend-absent-employee-auto-completion`: Automated workflow that detects employees absent for an entire month and completes their open monthend tasks, creating one clarification per project as an audit record for the project lead.

### Modified Capabilities

- `monthend-task-completion`: System-initiated completion path added alongside existing human actor completion.
- `monthend-clarifications`: System-created clarification path added via `createBySystem()` factory; `canDelete` constraint updated to block deletion of system-created clarifications.
- `user-aggregate`: `Role.SYSTEM` added; `User` invariant relaxed conditionally for system users (nullable `zepUsername`/`email`).

## Impact

- `com.gepardec.mega.hexagon.shared.domain.model.Role` — new `SYSTEM` value
- `com.gepardec.mega.hexagon.shared.domain.model.SystemActor` — new constant class
- `com.gepardec.mega.hexagon.user.domain.model.User` — conditional null-check on `zepUsername`/`email`
- `com.gepardec.mega.hexagon.worktime` — new `Absence`, `AbsenceType`, `WorkTimeAbsenceZepPort`, `WorkTimeAbsenceZepAdapter`, `GetEmployeeAbsencesUseCase`, `GetEmployeeAbsencesService`
- `com.gepardec.mega.hexagon.monthend` — new port, ACL adapter, use case, service, scheduler; domain model changes to `MonthEndTask` and `MonthEndClarification`
- Liquibase: new changelog entry seeding the system actor row in `hexagon_users` and `hexagon_user_roles`
- No REST API changes; no breaking changes to existing endpoints
