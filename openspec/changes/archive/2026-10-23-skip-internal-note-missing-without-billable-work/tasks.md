# Tasks

## 1. Calculator

- [x] 1.1 Change `InternalNoteConventionCalculator` to implement `WorkTimeWarningCalculator` with `calculate(WorkTimeEvaluation)`. Before the per-booking loop, decide once whether the month contains billable work: any booking is `billable`, or has a non-null `projectZepId` contained in `evaluation.billableProjectZepIds()` (design decisions 2 and 3). Produce `INTERNAL_NOTE_MISSING` only when that holds. Leave `INTERNAL_NOTE_BILLABLE`, the marker pattern and `CONVENTION_START` unchanged. Verify that the class compiles and its position in the `WorkTimeWarningAssembler` calculator list is unchanged.
- [x] 1.2 Update `InternalNoteConventionCalculatorTest` to build `WorkTimeEvaluation` with Instancio, setting `bookings` and `billableProjectZepIds` explicitly (design decision 4). In the existing `INTERNAL_NOTE_MISSING` cases, add a billable booking on another day so they keep their current expectations. Add tests for the new spec scenarios: a month with only non-billable bookings on non-billable projects produces no `INTERNAL_NOTE_MISSING`; a single billable booking on another day makes earlier unmarked days warn; a non-billable booking on a billable project counts as billable work; `INTERNAL_NOTE_BILLABLE` is still produced as before. Verify with `mvn test -Dtest=InternalNoteConventionCalculatorTest`.

## 2. Integration check

- [x] 2.1 Run `mvn test -Dtest='WorkTimeWarningAssemblerTest,WorkTimeWarningCalculatorsTest,GetEmployeeWarningsServiceTest,WorkTimeEmployeeAndProjectLeadResourceTest'` and verify they pass unchanged. In particular, the assembler test that expects both `INTERNAL_NOTE_MISSING` and `NON_BILLABLE_ON_BILLABLE_PROJECT` must still pass, because its booking refers to a billable project.
- [x] 2.2 Run `mvn test` and verify the full suite passes, including `ArchitectureTest`.
