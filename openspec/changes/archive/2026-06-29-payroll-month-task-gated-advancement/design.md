## Context

Payroll month resolution drives which calendar month is shown as the active billing period in the monthend workflow. The employee rule already inspects task state, but only looks backward (previous month open tasks). Project leads have no task-based logic at all — they always see the previous month.

The problem: once task generation runs for the new month, users should automatically advance. Currently, employees with leftover open tasks are stuck on the previous month indefinitely; project leads never advance on their own.

The fix is a single unified gate: **"have tasks been generated for the current month for this actor?"** If yes, advance. This is checked via the existing `MonthEndTaskRepository` methods.

## Goals / Non-Goals

**Goals:**
- Both roles advance to the current month as soon as its tasks are generated
- Employees retain early advancement when the previous month is already clean
- No new repository query methods required

**Non-Goals:**
- Changing when or how task generation is triggered
- Handling multi-month scenarios (only current and previous month are ever considered)
- Changing the employee or project lead task query semantics beyond what is needed here

## Decisions

### Project lead use case gains a `UserId leadId` parameter

The new logic requires knowing which lead to check tasks for. The interface changes from `getPayrollMonth()` to `getPayrollMonth(UserId leadId)`, mirroring the existing employee use case. The REST adapter already holds `authenticatedActorContext.userId()` and passes it through.

_Alternative considered_: inject `AuthenticatedActorContext` directly into the service. Rejected — mixing infrastructure (security context) into the application service violates hexagonal layering.

### Use `findLeadProjectTasks` for project lead current-month detection

`findLeadProjectTasks(leadId, currentMonth)` returns all tasks (any status) where the user is the lead, for the given month. Non-empty means tasks have been generated. This is the correct semantic: "has this month been prepared for the lead?"

### Use `findOpenSubjectTasks` for employee current-month detection

`findOpenSubjectTasks(actorId, currentMonth)` returns open tasks where the employee is the subject. Using it as the "tasks generated" signal is a pragmatic choice — if the employee has open tasks in the current month, tasks have obviously been generated.

The edge case (all current-month tasks are already closed, method returns empty, advancement is missed) is accepted as practically impossible: an employee cannot close current-month tasks before the payroll month has advanced to the current month.

### Logical expression for each role

**Employee:**
```
currentMonth if:
  findOpenSubjectTasks(actorId, previousMonth).isEmpty()   // previous month clean
  OR !findOpenSubjectTasks(actorId, currentMonth).isEmpty() // current month generated
else:
  previousMonth
```

**Project lead:**
```
currentMonth if:
  !findLeadProjectTasks(leadId, currentMonth).isEmpty()    // current month generated
else:
  previousMonth
```

## Risks / Trade-offs

- **Edge case: employee with all current-month tasks already done** → `findOpenSubjectTasks(actorId, currentMonth)` returns empty; the "current month generated" signal is false. The employee would not advance via the new condition, but would still advance via the existing "previous month clean" condition once they finish the previous month. Accepted.
- **Breaking interface change** → `GetProjectLeadPayrollMonthUseCase.getPayrollMonth()` → `getPayrollMonth(UserId)`. The only caller is `MonthEndResource`, which is updated in the same change.
