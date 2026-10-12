## Why

The month-end task generation scheduler currently fires on the last **calendar** day of the month (`cron: 0 0 0 L * ? *`). The last calendar day is not always a working day — it can fall on a weekend or an Austrian public holiday. The business rule is that employees must complete month-end tasks by EOB on the **last working day**; generating tasks on a non-working day defeats the purpose.

## What Changes

- The scheduler cron expression is changed from `0 0 0 L * ? *` (last calendar day) to `0 0 0 25-31 * ? *` (daily during the last week of the month).
- A guard is added to `MonthEndTaskGenerationScheduler` that checks whether today is the last working day of the month (using `OfficeCalendarUtil`); if not, the scheduler exits early without calling the use case.
- A new static method `isLastWorkingDayOfMonth(LocalDate)` is added to `OfficeCalendarUtil`.
- The guard lives in the inbound adapter (scheduler), not in the application service, so that `GenerateMonthEndTasksUseCase` remains callable from REST or admin tools on any day.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

- `monthend-task-generation`: The scheduled trigger requirement changes from "last calendar day of the month" to "last working day of the month" (accounting for weekends and Austrian public holidays).

## Impact

- `MonthEndTaskGenerationScheduler` — cron change and guard logic added
- `OfficeCalendarUtil` — new `isLastWorkingDayOfMonth(LocalDate)` helper
- `MonthEndTaskGenerationSchedulerTest` — cron assertion updated, guard behaviour tests added
- No changes to `GenerateMonthEndTasksService`, `GenerateMonthEndTasksUseCase`, or any outbound adapters
