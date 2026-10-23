# Proposal

## Why

Four warning rules no longer fit the `WorkTimeWarningCalculator` interface: no-time-entry, non-billable-on-billable-project, travel allowance and meal entry. Each one has its own signature, its own field on the assembler, and its own argument on `assemble(...)`. That method now takes seven positional arguments, three of them `Set<LocalDate>`, so a swap compiles and produces wrong warnings without any error. Every new rule that needs data beyond the bookings changes the assembler signature, its test constructor, the service call and every assembler test. The billable-project change (#800) deferred a common input object until a third such rule appeared. There are now four.

## What Changes

- Introduce `WorkTimeEvaluation`, a domain value object that holds everything a warning rule may look at: the bookings, the payroll month, the evaluation date (today), the expected working days, the excused dates, the billable project ZEP ids and the meal entry dates.
- `WorkTimeWarningCalculator.calculate` takes a `WorkTimeEvaluation` instead of `WorkTimeBookings`.
- Add `BookingsWarningCalculator`, a sub-interface for rules that only need the bookings. It keeps `calculate(WorkTimeBookings)` and implements `calculate(WorkTimeEvaluation)` as a default method that delegates to it. The twelve bookings-only calculators switch to this interface and keep their logic and tests unchanged.
- The four outlier calculators implement `WorkTimeWarningCalculator` directly and read only the fields they need from the evaluation.
- `WorkTimeWarningAssembler` holds one list of calculators and exposes `assemble(WorkTimeEvaluation)`. The four dedicated fields and the long argument list go away.
- `GetEmployeeWarningsService` builds the `WorkTimeEvaluation` and passes it to the assembler.
- Each rule's start date (`RULE_START`) stays in its own calculator. No shared rule start date is introduced.

No externally observable behaviour changes. The same warnings are produced, in the same order, with the same deduplication per type and date.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

None. The `worktime-warnings` requirements and the per-rule `worktime-warning-*` requirements describe which warnings are produced from which data, not the calculator signatures, and they all remain satisfied. The change sets `skip_specs: true` because it is a pure internal refactor.

## Impact

- **Code (worktime domain)**: new `domain/model/WorkTimeEvaluation`; `domain/services/warning/WorkTimeWarningCalculator`, new `BookingsWarningCalculator`, `WorkTimeWarningAssembler`, and all sixteen calculators (twelve change only their `implements` clause; `NoEntryWarningCalculator`, `NonBillableOnBillableProjectCalculator`, `TravelAllowanceCalculator` and `MealEntryCalculator` change their signature).
- **Code (worktime application)**: `GetEmployeeWarningsService` (builds the evaluation, including the empty-bookings branch).
- **Tests**: `WorkTimeWarningAssemblerTest`, `GetEmployeeWarningsServiceTest`, and the tests of the four outlier calculators. `WorkTimeWarningCalculatorsTest` only changes the declared helper type to `BookingsWarningCalculator`. The tests of the twelve bookings-only calculators stay as they are.
- **APIs, external systems, dependencies**: none.
