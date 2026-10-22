# Tasks

## 1. Billability and note in the booking model

- [x] 1.1 Extend `ZepAttendance` with `note` (`@JsonProperty("note")`) and a builder method; verify that a deserialization test with `src/test/resources/zep/rest/testresponses/attendances/002-tuser/ZepAttendanceList.json` yields the note "Jenkins aufräumen weil Speicherplatz ausgegangen ist"
- [x] 1.2 Extend `WorkTimeBooking` with `billable()` and `note()` and add both fields to `ProjectBooking` and `JourneyBooking`; verify that `mvn compile` succeeds
- [x] 1.3 `WorkTimeBookingZepMapper` maps `billable` (`null` → `false`) and `note` for both booking kinds; extend `WorkTimeBookingZepMapperTest` with the scenarios "Billable flag and note are carried over", "A missing billable flag means not billable" and "A missing note yields a booking without note" for project and journey bookings and make it pass
- [x] 1.4 Adjust test data creation: `WarningTestBookingBuilder` (project and journey) gets `billable`/`note` with fixed defaults, direct `Instancio.of(ProjectBooking/JourneyBooking)` calls in tests set both fields explicitly; verify with `mvn test -Dtest='com.gepardec.mega.hexagon.worktime.**'`

## 2. Warning rule for the internal convention

- [x] 2.1 Extend `WorkTimeWarningType` with `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING`; verify that `mvn compile` succeeds
- [x] 2.2 Implement `InternalNoteConventionCalculator` according to design.md (decisions 1, 2, 4): marker pattern, cutoff date 2026-10-01 on the booking date, one warning per type and day, date without hours
- [x] 2.3 Write `InternalNoteConventionCalculatorTest` covering every scenario from `specs/worktime-warning-internal-note/spec.md` (marker spelling variants parameterized, project and journey bookings, empty note, cutoff 2026-09-30 vs. 2026-10-01, deduplication per day); verify with `mvn test -Dtest=InternalNoteConventionCalculatorTest`
- [x] 2.4 Register the calculator in the default list of `WorkTimeWarningAssembler`; extend `WorkTimeWarningAssemblerTest` with a case in which a billable, internally marked booking from October 2026 produces an `INTERNAL_NOTE_BILLABLE` in the result, and make it pass

## 3. Integration

- [x] 3.1 Add a REST test for `GET /worktime/warnings/{payrollMonth}` (or extend an existing one) that, for October 2026 with a billable, internally marked booking, expects a warning with `type` `INTERNAL_NOTE_BILLABLE`, the booking date and `hours` `null`; verify that the test passes
- [x] 3.2 Run the full build with `mvn clean package` and verify that all tests including `ArchitectureTest` pass
- [x] 3.3 Run `openspec validate add-internal-note-warnings --strict` and verify that the change validates without errors

## Workflow follow-up

- Have the frontend translation keys for `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING` created in the frontend repository and agree on the release timing.
