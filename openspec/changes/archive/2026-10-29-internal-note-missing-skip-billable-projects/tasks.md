# Tasks

## 1. Skip billable projects in the internal-note convention check

- [x] 1.1 In `InternalNoteConventionCalculator`, produce `INTERNAL_NOTE_MISSING` only when the booking's `projectZepId` is null or not contained in `evaluation.billableProjectZepIds()` (in addition to the existing month gate, non-billable and unmarked conditions); leave `containsBillableWork` and `INTERNAL_NOTE_BILLABLE` unchanged. Verify with `mvn test -Dtest=InternalNoteConventionCalculatorTest`.
- [x] 1.2 In `InternalNoteConventionCalculatorTest`, rewrite `calculate_whenNonBillableBookingIsOnBillableProject_thenCountsAsBillableWork` so the booking on the billable project opens the gate but gets no warning, while an unmarked non-billable booking on another day without a billable project is reported; add tests for: non-billable unmarked booking on a billable project (no `INTERNAL_NOTE_MISSING`), unmarked booking on a project not in the billable set (reported), unmarked booking without project reference while the month has billable work (reported). Verify with `mvn test -Dtest=InternalNoteConventionCalculatorTest`.
- [x] 1.3 In `WorkTimeWarningAssemblerTest`, change `assemble_whenNonBillableBookingWithoutMarkerIsOnBillableProjectFromOctober2026_thenBothConventionWarnings` to expect only `NON_BILLABLE_ON_BILLABLE_PROJECT` and no `INTERNAL_NOTE_MISSING` for that date, and rename it accordingly. Verify with `mvn test -Dtest=WorkTimeWarningAssemblerTest`.

## 2. Code hygiene

- [x] 2.1 Run `mvn package -Prewrite-check`; if it reports drift, run `mvn rewrite:run` and re-run the check until the build passes.
