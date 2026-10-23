# Tasks

## 1. Evaluation value object

- [x] 1.1 Add the record `WorkTimeEvaluation` in `worktime/domain/model` with the fields `bookings`, `payrollMonth`, `evaluationDate`, `expectedWorkingDays`, `excusedDates`, `billableProjectZepIds` and `mealEntryDates`. Its compact constructor rejects `null` and copies the sets into unmodifiable sets (design decision 1). Verify with a `WorkTimeEvaluationTest` covering the null rejection and that the sets are unmodifiable copies, and run `mvn test -Dtest=WorkTimeEvaluationTest`.

## 2. Calculator interfaces and calculators

The interface change in 2.1 breaks compilation until the assembler (3.1) and the service (4.1) are updated. Groups 2 to 4 are therefore verified together at the end of group 4.

- [x] 2.1 Change `WorkTimeWarningCalculator.calculate` to take a `WorkTimeEvaluation`, and add `BookingsWarningCalculator` with `calculate(WorkTimeBookings)` plus the delegating default method (design decision 2). Verify by checking that both interfaces compile as described in the design.
- [x] 2.2 Switch the twelve bookings-only calculators (`CoreWorkingHours`, `TimeOverlap`, `Holiday`, `Weekend`, `DoctorAppointment`, `ExceededMaximumWorkingHoursPerDay`, `InsufficientRest`, `InsufficientBreak`, `InvalidJourney`, `InvalidWorkingLocation`, `LocationRelevantSetJourney`, `InternalNoteConvention`) to `implements BookingsWarningCalculator` without changing their logic. Verify that their diff contains only the `implements` clause and imports.
- [x] 2.3 Change the declared helper type in `WorkTimeWarningCalculatorsTest` (the parameterised order test, `bookingBasedCalculators()` and `assertType`) from `WorkTimeWarningCalculator` to `BookingsWarningCalculator`. Verify that the test's assertions are unchanged.
- [x] 2.4 Make `NoEntryWarningCalculator` implement `WorkTimeWarningCalculator` with `calculate(WorkTimeEvaluation)`, taking the booked dates from `evaluation.bookings().bookedDates()` and today from `evaluationDate`. Update `NoEntryWarningCalculatorTest` to build the evaluation with Instancio, setting every field the rule reads (design decision 5). Verify that the test cases keep their current expectations.
- [x] 2.5 Make `NonBillableOnBillableProjectCalculator` and `TravelAllowanceCalculator` implement `WorkTimeWarningCalculator` and read `bookings` and `billableProjectZepIds` from the evaluation, keeping each rule's own `RULE_START`. Update their tests the same way. Verify that the test cases keep their current expectations.
- [x] 2.6 Make `MealEntryCalculator` implement `WorkTimeWarningCalculator` and read `bookings`, `payrollMonth`, `mealEntryDates` and `evaluationDate` from the evaluation, keeping its own `RULE_START`. Update `MealEntryCalculatorTest` the same way. Verify that the test cases keep their current expectations.

## 3. Assembler

- [x] 3.1 Reduce `WorkTimeWarningAssembler` to a single `List<WorkTimeWarningCalculator>`: the twelve current calculators in their current order, followed by `NoEntryWarningCalculator`, `NonBillableOnBillableProjectCalculator`, `TravelAllowanceCalculator` and `MealEntryCalculator`. Replace the signature with `assemble(WorkTimeEvaluation)`, keep the `EMPTY_ENTRY_LIST` early return and the per-type-and-date deduplication, and reduce the package-private constructor to `WorkTimeWarningAssembler(List<WorkTimeWarningCalculator>)` (design decision 3). Verify by checking that the assembler has no calculator-specific fields left.
- [x] 3.2 Update `WorkTimeWarningAssemblerTest` to build `WorkTimeEvaluation` inputs and to stub and verify mocked calculators with `calculate(same(evaluation))`. The test that mocks `NoEntryWarningCalculator` now stubs the evaluation call too. Verify that every test keeps its current expected warnings and order.

## 4. Application service

- [x] 4.1 Make `GetEmployeeWarningsService.getWarnings` build a `WorkTimeEvaluation` and call `assemble(evaluation)`. With no bookings it uses empty sets and makes no extra port calls. It still skips the meal entry call when there are no journeys (design decision 4). Update `GetEmployeeWarningsServiceTest` where it depends on the assembler call shape. Verify that the existing port-call assertions (no calls when there are no bookings, billable ids passed through, meal entry call only with journeys) still pass.
- [x] 4.2 Run `mvn test -Dtest='*Calculator*Test,WorkTimeWarningAssemblerTest,WorkTimeEvaluationTest,GetEmployeeWarningsServiceTest'` and verify that everything compiles and all tests pass.

## 5. Integration check

- [x] 5.1 Run `mvn test` and verify the full suite passes, including `ArchitectureTest` (which checks that `WorkTimeEvaluation` in `domain/model` brings in no forbidden dependencies).
