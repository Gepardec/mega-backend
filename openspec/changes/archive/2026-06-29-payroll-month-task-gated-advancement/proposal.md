## Why

Payroll month resolution for both employees and project leads can leave users stuck on the previous month even after the current month's tasks have been generated. Project leads have no task-based advancement at all — they always see the previous month, regardless of workflow state.

## What Changes

- **Employee**: add a second advancement condition — if open tasks exist for the current month (i.e., tasks have been generated), advance to the current month even if the previous month still has open tasks remaining.
- **Project lead**: replace the unconditional "previous month" rule with a task-gated rule — return the current month when tasks have been generated for it (detected via open lead tasks), otherwise return the previous month.
- **BREAKING**: `GetProjectLeadPayrollMonthUseCase.getPayrollMonth()` gains a `UserId leadId` parameter to enable per-lead task lookup.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `payroll-month`: requirements for both employee and project-lead payroll month resolution are changing — project leads gain task-gated advancement; employees gain a second advancement condition based on current-month task existence.

## Impact

- `GetProjectLeadPayrollMonthUseCase` interface — signature change (breaking for any caller)
- `GetProjectLeadPayrollMonthService` — logic rewrite + repository injection
- `GetEmployeePayrollMonthService` — logic extension (additional OR condition)
- `MonthEndResource` — passes actor `UserId` to project lead use case
- Tests: `GetProjectLeadPayrollMonthServiceTest`, `GetEmployeePayrollMonthServiceTest`
