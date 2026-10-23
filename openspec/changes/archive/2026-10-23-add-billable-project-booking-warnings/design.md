# Design

## Context

Motivation: see proposal.md, section "Why". Requirements: see `specs/worktime-warning-billable-project/spec.md`.

Current state:

- `ZepAttendance` already reads `project_id`, but `WorkTimeBookingZepMapper` does not map it. `ProjectBooking` and `JourneyBooking` do not know their project. The mapper runs with `unmappedTargetPolicy = ERROR`.
- The project BC stores billability as one boolean: `Project.billable()` is true for the ZEP presets `BILLABLE` (1) and `BILLABLE_FIXED` (2), see `ZepProjectMapper`. The preset itself is not persisted.
- The worktime BC reads projects through its own outbound port `WorkTimeProjectSnapshotPort`, whose adapter uses the project BC's `ProjectRepository`. Its `ProjectRef` carries id, ZEP id and name, but no billability.
- `WorkTimeWarningCalculator.calculate(WorkTimeBookings)` receives only the bookings. `WorkTimeWarningAssembler` builds its calculators once, and `GetEmployeeWarningsService` (application-scoped) builds the assembler once. `NoEntryWarningCalculator` is already a separate calculator with its own signature that the assembler calls explicitly.

## Goals / Non-Goals

**Goals:**
- Give the warning rule the booked projects' billability without the domain reading external data.
- Keep the existing calculators and their interface untouched.

**Non-Goals:**
- No persisting of the four ZEP billability presets. The existing boolean is enough.
- No historical billability per month.
- No change to the project BC.
- No frontend translations in this repository.

## Decisions

### 1. A standalone calculator called explicitly by the assembler

`NonBillableOnBillableProjectCalculator` does not implement `WorkTimeWarningCalculator`. It has its own method `calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds)`. The assembler holds it as a separate field, like `NoEntryWarningCalculator`, and calls it after the regular calculators. `assemble(...)` gets an additional parameter `Set<Integer> billableProjectZepIds`, and the package-private test constructor gets the new calculator as an argument. Its warnings go through the same deduplication per type and date.

- *Alternative:* pass the billable IDs into the calculator's constructor. That doesn't work, because the calculators are built once per assembler, and the assembler lives as long as the application-scoped service, while the billable projects differ per request.
- *Alternative:* add a `projectBillable` flag to each booking before assembly. The calculator would then fit the existing interface, but a project fact that ZEP doesn't send would end up on every booking for the sake of one rule, and the bookings would have to be rebuilt in the service or enriched in the adapter.
- *Alternative:* widen the interface to `calculate(WarningContext)`. This is the cleanest option once several rules need outside data, but it touches all 12 calculators and their tests. Revisit it when a third rule needs input beyond the bookings.

### 2. The booking carries the ZEP project id as a nullable `Integer`

`WorkTimeBooking` gets `Integer projectZepId()`, and both records get the field. The mapper maps `attendance.projectId` directly. A missing value stays `null` and is not a mapping error, so a single odd record can't break the whole warnings request. The calculator skips bookings without a project id.

- *Alternative:* a required `int` with a mapping error when it's missing. Stricter, but a missing project id would then fail every worktime request for that employee and month, which is worse than not checking that one booking.
- *Alternative:* the shared `ProjectRef`/`ProjectId`. The booking only knows the ZEP id; resolving the internal id would mean a lookup during mapping.

### 3. The port returns the billable subset of the booked projects

`WorkTimeProjectSnapshotPort` gets `Set<Integer> findBillableZepIds(Set<Integer> zepIds)`. The service collects the distinct non-null project ids from the bookings and passes the result to the assembler. The adapter looks up each ID with `ProjectRepository.findByZepId` and keeps those whose `billable()` is true. IDs unknown to the repository are dropped, which gives the "unknown project is not checked" behaviour without extra code in the domain.

There is no `isActiveIn(month)` filter, unlike the other port methods. Billability is a property of the project, not of the month, and a booking that exists already shows that the project was in use.

- *Alternative:* extend `ProjectRef` with `billable` and reuse `findByZepId`. That would widen a shared kernel type used across BCs just for this rule.
- *Alternative:* a bulk query `findAllByZepIds` in `ProjectRepository`. That's a change to the project BC for a set that is typically fewer than ten IDs per employee and month.

The port is only called when there are bookings, so the existing early return for an empty month stays unchanged.

### 4. Billability preset via the existing `Project.billable()`

"Billable" means `BILLABLE` or `BILLABLE_FIXED`, which is exactly what `Project.billable()` already expresses. On a "fixed" project ZEP shouldn't allow non-billable bookings at all. If one appears anyway, it is worth a warning.

### 5. Cutoff date as the calculator's own constant

`LocalDate.of(2026, 10, 1)` becomes a constant of the new calculator, compared against `booking.date()`. It has the same value as the internal-note rule's cutoff, but stays separate on purpose: the two rules are independent business decisions and may diverge.

- *Alternative:* a shared constant for both convention rules. That couples two rules that only coincidentally start on the same date.

## Risks / Trade-offs

- [Instancio-created bookings in existing tests get random `projectZepId` values.] → The new calculator only reports IDs contained in `billableProjectZepIds`. Tests that pass an empty set never see the new warning. `WarningTestBookingBuilder` still gets a fixed default.
- [Changing the `assemble(...)` signature touches every call site (service plus assembler tests).] → The change is mechanical; existing tests pass `Set.of()`.
- [One repository lookup per distinct booked project per request.] → The set is small per employee and month. If it grows, switch to a bulk query in the project BC.
- [Billability is read as of now, so changing a preset also changes past months' warnings.] → Agreed this way (see spec). Warnings are not persisted, so nothing has to be migrated.
- [Many warnings in October 2026 if non-billable bookings on customer projects are common practice.] → Intended from a business perspective. Communication is outside this change.
- [The frontend only knows the new type after its own release.] → Roll out the translation key together with or before the backend.

## Migration Plan

- No data migration; warnings are recalculated on every request.
- Roll out the frontend translation key for `NON_BILLABLE_ON_BILLABLE_PROJECT` together with or before the backend.
- Rollback: revert the backend release; no persisted data is created.
