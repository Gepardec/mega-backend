# Tasks

## 1. Project reference in the booking model

- [x] 1.1 Extend `WorkTimeBooking` with `Integer projectZepId()` and add the field to `ProjectBooking` and `JourneyBooking`; verify that `mvn compile` succeeds
- [x] 1.2 `WorkTimeBookingZepMapper` maps `attendance.projectId` to `projectZepId` for both booking kinds (`null` stays `null`); extend `WorkTimeBookingZepMapperTest` with the scenarios "The project id is carried over as project reference" and "A missing project id yields a booking without project reference" for project and journey bookings and make it pass
- [x] 1.3 Give `WarningTestBookingBuilder` (project and journey) a `projectZepId` with a fixed default and a setter; verify with `mvn test -Dtest='com.gepardec.mega.hexagon.worktime.**'`

## 2. Billability of the booked projects

- [x] 2.1 Add `Set<Integer> findBillableZepIds(Set<Integer> zepIds)` to `WorkTimeProjectSnapshotPort` and implement it in `WorkTimeProjectSnapshotAdapter` per design.md decision 3 (lookup via `findByZepId`, keep `billable()`, drop unknown IDs, no `isActiveIn` filter)
- [x] 2.2 Write `WorkTimeProjectSnapshotAdapterTest` (plain Mockito) covering: billable project returned, non-billable project dropped, unknown ZEP id dropped, empty input yields empty set without repository calls; verify with `mvn test -Dtest=WorkTimeProjectSnapshotAdapterTest`

## 3. Warning rule

- [x] 3.1 Add `NON_BILLABLE_ON_BILLABLE_PROJECT` to `WorkTimeWarningType`; verify that `mvn compile` succeeds
- [x] 3.2 Implement `NonBillableOnBillableProjectCalculator` per design.md decisions 1, 2 and 5: `calculate(WorkTimeBookings, Set<Integer> billableProjectZepIds)`, non-billable bookings whose `projectZepId` is in the set, project and journey bookings, note ignored, cutoff 2026-10-01 on the booking date, one warning per day without hours
- [x] 3.3 Write `NonBillableOnBillableProjectCalculatorTest` covering every scenario from `specs/worktime-warning-billable-project/spec.md` that the calculator owns (project and journey bookings, note "Intern - JFX" still reported, billable booking not reported, project not in the set not reported, `null` project id not reported, cutoff 2026-09-30 vs. 2026-10-01, deduplication per day); verify with `mvn test -Dtest=NonBillableOnBillableProjectCalculatorTest`
- [x] 3.4 Wire the calculator into `WorkTimeWarningAssembler` as a separate field next to `NoEntryWarningCalculator`, extend `assemble(...)` with `Set<Integer> billableProjectZepIds` and the package-private constructor with the calculator; update existing call sites in `WorkTimeWarningAssemblerTest` to pass `Set.of()` and add a case in which a non-billable booking without marker from October 2026 on a billable project yields both `INTERNAL_NOTE_MISSING` and `NON_BILLABLE_ON_BILLABLE_PROJECT`; verify with `mvn test -Dtest=WorkTimeWarningAssemblerTest`

## 4. Application service

- [x] 4.1 `GetEmployeeWarningsService` injects `WorkTimeProjectSnapshotPort`, collects the distinct non-null `projectZepId`s of the bookings, calls `findBillableZepIds` and passes the result to the assembler; the empty-month early return does not call the port. Extend `GetEmployeeWarningsServiceTest` with: billable IDs are passed through to the assembler, port not called for an empty month, a booking on an unknown project does not fail the request; verify with `mvn test -Dtest=GetEmployeeWarningsServiceTest`

## 5. Integration

- [x] 5.1 Extend the REST test for `GET /worktime/warnings/{payrollMonth}` (`WorkTimeEmployeeAndProjectLeadResourceTest`) with a case for October 2026 with a non-billable booking on a billable project, expecting a warning with `type` `NON_BILLABLE_ON_BILLABLE_PROJECT`, the booking date and `hours` `null`; verify that the test passes
- [x] 5.2 Run the full build with `mvn clean package` and verify that all tests including `ArchitectureTest` pass
- [x] 5.3 Run `openspec validate add-billable-project-booking-warnings --strict` and verify that the change validates without errors

## Workflow follow-up

- Have the frontend translation key for `NON_BILLABLE_ON_BILLABLE_PROJECT` created in the frontend repository and agree on the release timing.
