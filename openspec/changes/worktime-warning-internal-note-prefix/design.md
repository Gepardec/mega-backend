# Design

## Context

See proposal.md for motivation and specs/ for the required behaviour.

Current state of the `worktime` BC:

- `WorkTimeBookingZepMapper` maps `ZepAttendance` to the sealed `WorkTimeBooking` (`ProjectBooking` | `JourneyBooking`). Neither the attendance's `projectId` nor a note is mapped; `ZepAttendance` has no `note` field at all, although ZEP returns it (see `src/test/resources/zep/rest/testresponses/attendances/*`).
- `GetEmployeeWarningsService` fetches bookings, absences and expected working days and hands them to `WorkTimeWarningAssembler`, which runs the `WorkTimeWarningCalculator`s (signature `calculate(WorkTimeBookings)`) plus `NoEntryWarningCalculator` (which takes extra inputs) and deduplicates per type and date.
- Project billability lives in the `project` BC (`Project.billable`, synced from ZEP `billingType`). `worktime` already reads projects through its outbound `WorkTimeProjectSnapshotPort` → `WorkTimeProjectSnapshotAdapter` → `ProjectRepository`, returning the shared-kernel `ProjectRef` (id, zepId, name — no billability).
- The REST warning `type` is a plain string in the OpenAPI contract; no enum must be extended there.

## Goals / Non-Goals

**Goals:**
- Keep the new rule a pure domain function, like all other warning rules (no repository or clock access inside the calculator).
- Confine the change to the `worktime` BC; the `project` BC and the shared kernel stay unchanged.

**Non-Goals:**
- No change to the legacy `com.gepardec.mega` warning code.
- No new REST endpoint and no project-lead warning view.
- No caching of project billability beyond what the repository already does.

## Decisions

### 1. Bookings carry `projectZepId` and `note` on the sealed interface
Add `Integer projectZepId` and `String note` to `WorkTimeBooking` and both record implementations; map them in `WorkTimeBookingZepMapper` from `ZepAttendance.projectId` and the new `ZepAttendance.note` (`@JsonProperty("note")`).
- *Alternative*: put them only on `ProjectBooking` — rejected, journeys must be checked too.
- *Alternative*: store a resolved `billable` flag on the booking — rejected, the booking is built by the ZEP adapter, which must not reach into the `project` BC; classification belongs in the application layer.

### 2. Billability is resolved in the application service and passed into the assembler
`GetEmployeeWarningsService` collects the distinct `projectZepId`s of the month's bookings, asks the snapshot port for their billability, and passes the result to `WorkTimeWarningAssembler.assemble(...)` as an additional argument — the same way `expectedWorkingDays`, `excusedDates` and `today` are passed today. The new calculator, like `NoEntryWarningCalculator`, is held separately by the assembler with its own signature `calculate(WorkTimeBookings, Map<Integer, Boolean> billableByProjectZepId)` rather than widening the shared `WorkTimeWarningCalculator` interface for all eleven calculators.
- *Alternative*: widen `WorkTimeWarningCalculator.calculate` with a context object — rejected for now, it churns every calculator and test for one consumer. Revisit when a second rule needs project data.

### 3. New port method instead of extending `ProjectRef`
Add `Map<Integer, Boolean> findBillabilityByZepIds(Set<Integer> zepIds)` to `WorkTimeProjectSnapshotPort`. The adapter calls `ProjectRepository.findByZepId` per distinct id and omits unknown ids from the map. No `isActiveIn(month)` filter: a project's billability classifies a booking regardless of whether the project is still active.
- *Alternative*: add `billable` to shared `ProjectRef` — rejected, it is a shared-kernel type used by other BCs (`shared-user-project-refs`) and would widen their contract for a worktime-only need.
- *Alternative*: add `findAllByZepIds` to `ProjectRepository` — avoids N lookups but modifies the `project` BC's port. An employee books on a handful of projects per month, so per-id lookups are acceptable; switch if profiling shows otherwise.

### 4. Prefix matching via one precompiled pattern in the calculator
`Pattern.compile("^\\s*intern\\s*[-\\u2013\\u2014]", CASE_INSENSITIVE | UNICODE_CASE)`, applied with `find()` to a non-null note. Lives as a private constant in `InternalNotePrefixCalculator`; the rule is the calculator's own knowledge, not a reusable domain concept.

### 5. Calculator behaviour
`InternalNotePrefixCalculator` iterates over `bookings.values()`; for each booking it looks up `billableByProjectZepId.get(projectZepId)` — a `null` result (unknown or missing project id) skips the booking and is logged once per project id at `WARN` by the service, not the calculator, to keep the domain free of logging. Billable + prefix → `INTERNAL_PREFIX_ON_BILLABLE_PROJECT`; non-billable + no prefix → `INTERNAL_PREFIX_MISSING`. Results are made distinct per type and date inside the calculator; the assembler's dedup remains as the safety net.

### 6. Warning types
Append `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and `INTERNAL_PREFIX_MISSING` to `WorkTimeWarningType`. They are non-quantitative (`hours == null`), consistent with the existing hours rule in `worktime-warnings`.

## Risks / Trade-offs

- [Project sync lags behind ZEP, so new projects are unknown] → those bookings produce no convention warning (spec'd behaviour) and are logged at `WARN`, so the gap is visible.
- [`Project.billable` derived from ZEP `billingType` may not match the business notion of "internal" for every project] → classification is centralised in the port, so a later refinement (e.g. an explicit internal flag) only touches the adapter.
- [High warning volume right after rollout, because existing internal bookings were never checked] → expected and the point of the story; communicate to employees before enabling.
- [Frontend shows raw type identifiers until translation keys exist] → coordinate the frontend change to ship together with this backend change.
- [N repository lookups per warning request] → bounded by distinct projects per employee-month; see Decision 3.

## Migration Plan

No data migration. Deploy backend and frontend translations together; rollback is a plain redeploy of the previous version since nothing is persisted.
