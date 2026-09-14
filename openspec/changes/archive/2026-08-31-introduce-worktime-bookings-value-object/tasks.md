## 1. Collection Value Object

- [x] 1.1 Add `WorkTimeBookingsTest` using Instancio-created project and journey bookings to cover stable chronological ordering, defensive copying, immutable exposure, null rejection, and empty construction
- [x] 1.2 Implement immutable `WorkTimeBookings` in `worktime.domain.model` with canonical ordering by `from` then `to`, structural equality, emptiness, and minimal chronological traversal/access
- [x] 1.3 Add tests for chronological `byDate()` groups, typed project/journey views, working-time-contributing filtering, distinct booked dates, and exact aggregate `Duration`
- [x] 1.4 Implement the domain-specific collection operations, returning immutable and encounter-ordered views without warning-rule or generic predicate APIs

## 2. Warning Flow Contract

- [x] 2.1 Change `WorkTimeWarningCalculator.calculate` to accept `WorkTimeBookings`
- [x] 2.2 Wrap the outbound booking list once in `GetEmployeeWarningsService` and pass the value object into `WorkTimeWarningAssembler`
- [x] 2.3 Refactor `WorkTimeWarningAssembler` to use `isEmpty()`, `bookedDates()`, and the same `WorkTimeBookings` instance for every booking-based calculator while leaving `NoEntryWarningCalculator` date-set inputs unchanged
- [x] 2.4 Adapt application-service and assembler tests to assert empty-month behaviour, booked-date forwarding, and unchanged flat warning assembly through the value object

## 3. Calculator Refactoring

- [x] 3.1 Refactor `CoreWorkingHoursCalculator`, `ExceededMaximumWorkingHoursPerDayCalculator`, and `InsufficientBreakCalculator` to use chronological daily groups and collection duration operations
- [x] 3.2 Refactor `InsufficientRestCalculator`, `InvalidJourneyCalculator`, and `InvalidWorkingLocationInJourneyCalculator` to use canonical working-time, journey, and chronological views without local sorting
- [x] 3.3 Refactor `DoctorAppointmentCalculator`, `HolidayCalculator`, `WeekendCalculator`, `LocationRelevantSetJourneyCalculator`, and `TimeOverlapCalculator` to consume canonical collection traversal and typed/date views while preserving warning content and multiplicity
- [x] 3.4 Delete `AbstractTimeWarningCalculationStrategy` and verify no warning calculator extends a shared collection-utility base class

## 4. Parity and Verification

- [x] 4.1 Adapt the complete per-calculator and journey-validator test matrix to pass `WorkTimeBookings` without removing or weakening existing scenarios
- [x] 4.2 Add regression coverage proving unordered provider input yields deterministic chronological calculator traversal with unchanged warning types, dates, quantities, and occurrence counts
- [x] 4.3 Run the warning-domain suite and confirm all existing and new warning tests pass
- [x] 4.4 Run architecture tests and confirm the value object remains isolated in the worktime domain with no infrastructure or application dependencies
- [x] 4.5 Run `mvn clean package` outside the sandbox and `openspec validate introduce-worktime-bookings-value-object --strict`
