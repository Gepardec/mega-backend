## Context

The `MonthEndTaskGenerationScheduler` (inbound adapter) currently fires on the last calendar day of each month via cron `0 0 0 L * ? *`. It unconditionally delegates to `GenerateMonthEndTasksUseCase`. The last calendar day can fall on a weekend or an Austrian public holiday, meaning tasks are generated on a day when employees are not working.

`OfficeCalendarUtil` (hexagon shared domain util) already computes working days per month using the Jollyday Austria holiday calendar, but has no helper to determine whether a given date is the last working day of a month.

## Goals / Non-Goals

**Goals:**
- Generate month-end tasks at midnight on the last working day of the month, not the last calendar day.
- Keep `GenerateMonthEndTasksUseCase` free of scheduling concerns — it must remain a clean "generate for this month" command callable from REST, admin tooling, or tests at any time.

**Non-Goals:**
- Catching up if the server was down on the last working day (out of scope per explicit decision).
- Changing the generation logic or any outbound ports.

## Decisions

### Decision: Guard in the scheduler adapter, not the application service

**Chosen**: Add the `isLastWorkingDay` check inside `MonthEndTaskGenerationScheduler.generateMonthEndTasks()` before calling the use case.

**Alternative considered**: Add the guard inside `GenerateMonthEndTasksService.generate()`.

**Rationale**: The use case contract is "generate tasks for this month." Embedding a time-of-day policy in the use case would make it impossible to trigger generation manually via REST or admin tooling on any day other than the last working day. The scheduler is the only caller that cares about *when* — that concern belongs in the inbound adapter.

### Decision: Cron fires on days 25–31, not daily

**Chosen**: `0 0 0 25-31 * ? *` — fires at midnight on days 25 through 31.

**Alternative considered**: `0 0 0 * * ? *` — truly daily.

**Rationale**: The last working day of any month is always within the last 7 calendar days. Restricting to days 25–31 avoids 18–24 unnecessary scheduler invocations per month and reduces log noise. Quartz silently skips days that do not exist in shorter months (e.g., Feb 28/29), so this is safe for all months.

### Decision: Add `isLastWorkingDayOfMonth(LocalDate)` to `OfficeCalendarUtil`

**Rationale**: The check is a natural extension of the existing working-day utilities. It is a pure, stateless computation with no external dependencies beyond the existing Jollyday manager. Placing it here keeps the logic reusable and testable in isolation.

## Risks / Trade-offs

- **Risk**: Server downtime on the last working day means tasks are never generated for that month.  
  → **Mitigation**: Out of scope by explicit decision. Manual REST trigger exists as a fallback.

- **Risk**: Jollyday Austria calendar is missing a regional holiday, causing the last working day to be misidentified.  
  → **Mitigation**: Jollyday is already used in production for other scheduling decisions; this is an existing shared risk, not introduced here.

## Migration Plan

No data migration required. The change is purely behavioral — tasks are still generated once per month, just on the correct day. Existing tasks for months already generated are unaffected (generation is idempotent).

Deployment: standard rolling update. No feature flag needed.
