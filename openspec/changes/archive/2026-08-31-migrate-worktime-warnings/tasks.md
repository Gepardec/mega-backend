## 1. Domain vocabulary and models

- [x] 1.1 Add worktime-owned vocabulary enums `Task`, `Vehicle`, `WorkingLocation`, `JourneyDirection` in `worktime/domain/model` (copied from the legacy `monthlyreport` enums; keep the ZEP-code lookups they expose)
- [x] 1.2 Add sealed `WorkTimeBooking` interface with `from`, `to`, `task`, `workingLocation`, `workLocationProjectRelevant`, and default `date()`/`durationInHours()`
- [x] 1.3 Add records `ProjectBooking` (adds `process`) and `JourneyBooking` (adds `direction`, `vehicle`) implementing `WorkTimeBooking`
- [x] 1.4 Add `WorkTimeWarningType` enum unifying the time + journey constants, names preserved exactly, no message template
- [x] 1.5 Add `WorkTimeWarning` record `(LocalDate date, WorkTimeWarningType type, Double hours)`

## 2. Relocate the calculators (history-preserving)

- [x] 2.1 `git mv` the 8 time calculators, 3 journey calculators, and `AbstractTimeWarningCalculationStrategy` into `worktime/domain/services/warning` as a standalone commit (no edits)
- [x] 2.2 `git mv` the corresponding calculator unit tests into the worktime test tree in the same commit
- [x] 2.3 Adapt the 11 pure/calendar calculators: repackage, switch inputs to `WorkTimeBooking` (pattern-switch on the sealed type in place of `instanceof`), outputs to `WorkTimeWarning`, and point the holiday rule at the hexagon `OfficeCalendarUtil`
- [x] 2.4 Define `WorkTimeWarningCalculator` (`List<WorkTimeWarning> calculate(List<WorkTimeBooking>)`) and have the 11 calculators implement it
- [x] 2.5 Update the migrated calculator unit tests to the new models (use Instancio for booking construction); assert per-calculator parity with the legacy expectations

## 3. No-time-entry rule and assembly

- [x] 3.1 Rewrite `NoEntryWarningCalculator` as a pure set difference over `expectedWorkingDays − bookedDates − excusedDates − futureDays(today)`, taking `today` as a parameter
- [x] 3.2 Preserve the empty-bookings short-circuit to a single `EMPTY_ENTRY_LIST` warning
- [x] 3.3 Add `WorkTimeWarningAssembler` domain service that concatenates all calculator outputs into one flat `List<WorkTimeWarning>` (no merge-by-date, no i18n)
- [x] 3.4 Unit-test the no-entry set difference (past/future/absence-excused/pre-employment/zero-hours-weekday/HOME_OFFICE-not-excused) and the assembler

## 4. Outbound ports and adapters

- [x] 4.1 Add `WorkTimeBookingZepPort` (`fetchBookingsForEmployee(zepUsername, month) → List<WorkTimeBooking>`)
- [x] 4.2 Implement its adapter over the existing `AttendanceService` fetch, mapping `ZepAttendance` → `ProjectBooking`/`JourneyBooking` (port the legacy `zep/rest/mapper/ProjectEntryMapper` rules: activity→task, journey selection, direction default-outbound, work-location default-main, vehicle mapping, mapping-error on unmappable values)
- [x] 4.3 Add `WorkTimeExpectedWorkingDaysPort` (`expectedWorkingDays(UserId, month) → Set<LocalDate>`)
- [x] 4.4 Implement its adapter using the user BC's `UserRepository` outbound port for the active employment period plus the established on-demand ZEP regular-working-time lookup, selecting the schedule through `RegularWorkingTimes.active(YearMonth)`, intersecting with `OfficeCalendarUtil`, and returning `Set<LocalDate>` (no user/provider type reaches the worktime domain); handle no-active-employment-period
- [x] 4.5 Adapter tests for the booking mapping (project vs journey, defaults, mapping errors) and the expected-working-days resolution

## 5. Application service and inbound port

- [x] 5.1 Add `GetEmployeeWarningsUseCase` (`getWarnings(UserId, YearMonth) → List<WorkTimeWarning>`)
- [x] 5.2 Implement `GetEmployeeWarningsService`: resolve the employee via `WorkTimeUserSnapshotPort` (raise `WorkTimeUserNotFoundException` if absent), fetch bookings + absences (exclude `HOME_OFFICE` for excused days) + expected working days, derive `today` from an injected `Clock`, run the assembler
- [x] 5.3 Service unit tests covering the happy path, unknown employee, and empty-month short-circuit

## 6. REST contract and adapter

- [x] 6.1 Add `WorkTimeWarning` response schema to `openapi/schemas/worktime.yaml` (`type`, optional `date`, optional `hours`)
- [x] 6.2 Add `GET /worktime/warnings/{payrollMonth}` to `openapi/paths/worktime.yaml` under the existing `WorkTimeEmployee` tag (so it generates into `WorkTimeEmployeeApi`; bearer auth, 200/400/403/500), then regenerate the API interface + model
- [x] 6.3 Add the `getEmployeeWarnings` method to the existing `WorkTimeEmployeeResource` (actor from `AuthenticatedActorContext`, month via `WorkTimeRestTransportHelper`; the resource already carries `@MegaRolesAllowed(EMPLOYEE)`) plus a `WorkTimeWarningRestMapper` (`WorkTimeWarning` → flat DTO)
- [x] 6.4 REST integration test: authenticated employee gets the flat warnings array; caller without EMPLOYEE role is forbidden; malformed month is a bad request

## 7. Remove the legacy warnings path

- [x] 7.1 Delete `WarningCalculatorsManager`, `TimeWarningService` + `TimeWarningServiceImpl`
- [x] 7.2 Delete the legacy warning models: `TimeWarning`, `JourneyWarning`, `ProjectEntryWarning`, `TimeWarningType`, `JourneyWarningType`, `WarningType`, `MappedTimeWarningTypes`, `WorkTimeBookingWarning`
- [x] 7.3 Delete the legacy warnings REST surface: `WorkTimeBookingWarningDto`, `WorkTimeBookingWarningMapper`, and the `getAllWarningsForEmployeeAndMonth` method on `WorkerResource`/`WorkerResourceImpl`
- [x] 7.4 Remove now-dead legacy warning tests; confirm `ProjectEntry`/`getProjectTimes` remain (still used by monthly report)

## 8. Verification

- [x] 8.1 `mvn clean package` green (unit + integration + ArchitectureTest; confirm no hexagon→legacy dependency was introduced)
- [x] 8.2 Drive the new endpoint end-to-end for a sample employee/month and confirm the warning set matches the legacy output for the same inputs
