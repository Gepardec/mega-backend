# Design

## Context

Motivation: see proposal.md, section "Why". Requirements: see `specs/worktime-warning-internal-note/spec.md`.

Current state in the worktime BC:

- `InternalNoteConventionCalculator` implements `BookingsWarningCalculator` and sees only `WorkTimeBookings`. It produces both `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING`.
- `WorkTimeEvaluation` already carries `billableProjectZepIds`. `GetEmployeeWarningsService` fills it from the projects the employee booked on in the month, using the local project table (`WorkTimeProjectSnapshotPort.findBillableZepIds`).
- The bookings in an evaluation are exactly the employee's bookings for the payroll month.
- `NonBillableOnBillableProjectCalculator` already reads `billableProjectZepIds` from the evaluation and decides "refers to a billable project" via `booking.projectZepId() != null && billableProjectZepIds.contains(...)`.

## Goals / Non-Goals

**Goals:**
- Decide "the month contains billable work" inside the rule, from data the evaluation already holds.

**Non-Goals:**
- No new ports, ZEP calls, persisted fields or sync steps.
- No exemption for `INTERNAL_NOTE_BILLABLE`.
- No per-employee setting or manual override.

## Decisions

### 1. Base the exemption on the month's bookings, not on ZEP project assignments

The month contains billable work if any booking is billable or refers to a billable project.

- *Alternative:* exempt employees who are assigned to no billable project in ZEP. This matches the wording "cannot book billable work", but ZEP assignments are not maintained reliably. Stale or open-ended assignments would deny the exemption to the very employees it is meant for. It would also need one ZEP call per billable project.
- *Alternative:* sync an `internal` flag onto the `User` during the scheduled sync, derived from the assignments that the project lead sync already fetches. This avoids per-request ZEP calls, but it stores the unreliable assignment data. It also adds a "current" flag that would retroactively change warnings of past months.
- *Alternative:* a manually maintained exemption flag. Explicit, but it becomes stale in the same way the assignments did.

### 2. "Refers to a billable project" counts, not only `billable = true`

A non-billable booking on a billable project is billable work that was booked wrongly (`NON_BILLABLE_ON_BILLABLE_PROJECT`). If it did not count, a month whose only customer work was booked as non-billable would also lose its `INTERNAL_NOTE_MISSING` warnings. Counting it keeps the existing scenario in `worktime-warning-billable-project`, where both warnings fire for the same booking.

### 3. The calculator moves to `WorkTimeWarningCalculator`

`InternalNoteConventionCalculator` implements `WorkTimeWarningCalculator` directly and reads `bookings()` and `billableProjectZepIds()` from the evaluation. It computes the "contains billable work" flag once per evaluation, before the per-booking loop. Marker detection, the cutoff constant and the `INTERNAL_NOTE_BILLABLE` branch stay as they are. The position in the assembler's calculator list does not change, so the warning order stays the same.

- *Alternative:* add a `containsBillableWork()` method on `WorkTimeEvaluation` or `WorkTimeBookings`. `WorkTimeBookings` cannot answer it alone because project billability lives in the evaluation. A method on the evaluation would be reasonable, but only one rule needs it. It can be extracted once a second rule does. `NonBillableOnBillableProjectCalculator` repeats the "refers to a billable project" check; that duplication is a one-liner and is accepted.

### 4. Tests build evaluations with Instancio

Following the pattern in `NonBillableOnBillableProjectCalculatorTest`, the calculator test builds `WorkTimeEvaluation` with Instancio and sets `bookings` and `billableProjectZepIds` explicitly. Existing tests in which each month has only one non-billable booking and expects `INTERNAL_NOTE_MISSING` get a billable booking on another day added. This keeps them testing what they tested before.

## Risks / Trade-offs

- [An employee with billable work later in the month sees all earlier unmarked internal bookings turn into warnings at once] → Accepted. The warnings are correct for a mixed month and appear before month-end close.
- [A project's billability change in ZEP retroactively changes whether an earlier month is checked] → Accepted and consistent with `worktime-warning-billable-project`, which also uses the current billability.
- [Bookings on projects unknown to the system do not count as billable work] → Same behaviour as `NON_BILLABLE_ON_BILLABLE_PROJECT`. Such a booking still counts if it is flagged billable.
- [The announcement says "cannot book billable work", the rule implements "did not book billable work"] → The announcement is clarified outside this repository (see proposal.md, Impact).

## Migration Plan

- No data migration; warnings are recalculated on every request.
- Rollback: revert the backend release.
