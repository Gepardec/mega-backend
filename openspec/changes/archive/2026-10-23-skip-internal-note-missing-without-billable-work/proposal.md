# Proposal

## Why

The "Intern - " marker exists to tell internal and billable bookings apart during manual review. Employees who did no billable work in a month have nothing to tell apart, yet `INTERNAL_NOTE_MISSING` currently asks them to mark every single booking. This affects above all employees who only work internally. An announcement made it clear that this creates friction. Exempting by ZEP project assignment was considered and rejected because assignments are not maintained reliably. The month's own bookings are the only trustworthy signal.

## What Changes

- `INTERNAL_NOTE_MISSING` is only produced for a payroll month in which the employee has billable work. A month has billable work if at least one of its bookings is billable or refers to a billable project.
- In a month without billable work, non-billable bookings without a marker no longer produce `INTERNAL_NOTE_MISSING`.
- `INTERNAL_NOTE_BILLABLE` is unchanged. The marker detection and the 2026-10-01 cutoff date are unchanged.
- No new data is fetched or stored. The decision uses the bookings and the billable-project lookup that the warnings request already performs.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `worktime-warning-internal-note`: The requirement "Non-billable bookings without an internal marker are reported per day" gains the condition that the month contains billable work.

Reviewed and unaffected: `worktime-warnings` and `worktime-rest-api` only name the warning types. In `worktime-warning-billable-project`, the scenario where both `INTERNAL_NOTE_MISSING` and `NON_BILLABLE_ON_BILLABLE_PROJECT` are produced still holds, because a booking on a billable project counts as billable work. `worktime-bookings` is not affected.

## Impact

- **Code (worktime domain)**: `InternalNoteConventionCalculator` switches from the bookings-only interface to the evaluation-based interface, so it can read the billable project ids.
- **Tests**: `InternalNoteConventionCalculatorTest`. The existing assembler test that expects both convention warnings already provides a billable project and keeps passing.
- **APIs, external systems, dependencies**: none. `GET /worktime/warnings/{payrollMonth}` returns fewer `INTERNAL_NOTE_MISSING` warnings for months without billable work.
- **Outside this repository**: the announcement should be clarified to say that the prefix is not required in a month without billable bookings.
