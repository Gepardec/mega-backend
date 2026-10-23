# Proposal

## Why

On every day of a business trip, employees must record in ZEP which meals the employer covered (breakfast, lunch, dinner), because covered meals reduce the per diem. When an employee forgets the entry for a day, nobody notices until the manual review. MEGA should remind them with a worktime warning before the month is closed.

## What Changes

- The journeys of a payroll month form trips: an outbound journey opens a trip, onward journeys continue it, a return journey closes it. Every day from the outbound journey through the return journey is a trip day, including the days in between that carry no journey.
- Trips that are not properly closed still yield trip days:
  - A trip still open at the end of the month runs until the last day of the month.
  - A return journey without a preceding outbound journey in the month starts its trip on the first day of the month.
  - A trip interrupted by a new outbound journey only counts its own journey days. The pairing rule already reports it with `BACK_MISSING`.
  - Every day that carries a journey is always a trip day.
- The employee's meal entries are read from ZEP (`GET /employees/{username}/meals`). A day has a meal entry when ZEP returns an entry for that date. Whether breakfast, lunch or dinner is set does not matter, so an entry with all three flags false is fine.
- New warning type `MEAL_ENTRY_MISSING`: a trip day without a meal entry. The warning carries the date and no hours.
- The rule only checks days from 2026-10-01 onward, the same cutoff date as the other booking-convention rules.
- Like `NO_TIME_ENTRY`, the rule skips the current date and later days.

## Capabilities

### New Capabilities
- `worktime-warning-meal-entry`: Determines the trip days of a payroll month from its journeys, including trips that are not properly closed, and flags trip days without a meal entry (cutoff date, current date and later days excluded).

### Modified Capabilities
- `worktime-warnings`: The warnings use case also takes the employee's meal entries into account, and the warning-type vocabulary gains `MEAL_ENTRY_MISSING`.

## Impact

- **Backend (worktime BC)**: new outbound port and ZEP adapter for meal entries, the warnings service passes the meal-entry dates to the assembler, a new calculator, the trip-day logic, and the warning-type enum.
- **ZEP integration**: new REST call `GET /employees/{username}/meals` with paging. The endpoint has no date filter, so all of the employee's meal entries are read and filtered to the month in MEGA. The call is skipped for months without journeys.
- **REST API**: no structural change. `GET /worktime/warnings/{payrollMonth}` can return the new `type` value `MEAL_ENTRY_MISSING`.
- **Frontend**: needs a translation key for `MEAL_ENTRY_MISSING`. Adding it is not part of this change.
