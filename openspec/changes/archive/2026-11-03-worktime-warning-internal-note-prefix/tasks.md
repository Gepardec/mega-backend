# Tasks

## 1. Booking model: project reference and note

- [x] 1.1 Add `note` (`@JsonProperty("note")`) to `ZepAttendance` and its builder; verify with a deserialisation assertion that `zep/rest/testresponses/attendances/001-duser/attendance1.json` yields its `note` value
- [x] 1.2 Add `Integer projectZepId` and `String note` to the `WorkTimeBooking` interface and to `ProjectBooking` / `JourneyBooking`; fix all construction sites in main and test code (incl. `WarningTestBookingBuilder`) and verify `mvn test -Dtest='com.gepardec.mega.hexagon.worktime.**'` compiles and passes
- [x] 1.3 Map `projectId` → `projectZepId` and `note` → `note` in `WorkTimeBookingZepMapper` for both booking kinds; extend `WorkTimeBookingZepMapperTest` with the spec scenarios "Project and note are taken from the attendance record" and "A record without a note still produces a booking" (project + journey) and verify it passes

## 2. Project billability lookup

- [x] 2.1 Add `Map<Integer, Boolean> findBillabilityByZepIds(Set<Integer> zepIds)` to `WorkTimeProjectSnapshotPort` and implement it in `WorkTimeProjectSnapshotAdapter` via `ProjectRepository.findByZepId` per id, omitting unknown ids and applying no active-in-month filter; verify with a unit test covering billable, non-billable, unknown, and an inactive project
- [x] 2.2 Verify `ArchitectureTest` and the hexagon layer tests still pass (no new dependency from `worktime` domain/application on the `project` BC beyond the outbound adapter)

## 3. Warning rule

- [x] 3.1 Add `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and `INTERNAL_PREFIX_MISSING` to `WorkTimeWarningType`; verify `WorkTimeWarningRestMapperTest`/`WorkTimeRestMapperTest` still pass and the new types map to their identifier strings
- [x] 3.2 Implement `InternalNotePrefixCalculator` (`calculate(WorkTimeBookings, Map<Integer, Boolean>)`) with the tolerant prefix pattern from design.md, date + no hours, distinct per type and date; verify with `InternalNotePrefixCalculatorTest` covering every scenario of the `worktime-warning-internal-note-prefix` spec (prefix variants incl. en/em dash, `Interne Schulung`/`Internet-Recherche`, empty note, journey booking, unknown project, booking-level billable flag ignored, both violations on one day, several bookings per day)
- [x] 3.3 Register the calculator in `WorkTimeWarningAssembler` and extend `assemble(...)` with the billability map; update `WorkTimeWarningAssemblerTest` and `WorkTimeWarningCalculatorsTest` and verify both pass, incl. that the empty-booking path still yields only `EMPTY_ENTRY_LIST`

## 4. Use case wiring

- [x] 4.1 In `GetEmployeeWarningsService`, collect distinct `projectZepId`s, resolve them via `findBillabilityByZepIds`, log unknown project ids at `WARN` (one line per id, no personal data), and pass the map to the assembler; verify with `GetEmployeeWarningsServiceTest` cases for a prefixed billable booking, an unprefixed internal booking, and an unknown project (no warning, port called with the distinct ids)
- [x] 4.2 Extend the REST test in `WorkTimeEmployeeAndProjectLeadResourceTest` so `GET /worktime/warnings/{payrollMonth}` returns an `INTERNAL_PREFIX_MISSING` entry with `date` set and `hours: null`; verify it passes

## 5. Integration check

- [x] 5.1 Run `mvn test` and verify the full suite is green
- [x] 5.2 Run `openspec validate worktime-warning-internal-note-prefix --strict` and verify it passes

## 6. Frontend (mega-frontend-v2) — blocks archive

See `frontend-handoff.md` in this change for details and the manual test plan.

- [x] 6.1 Rename the existing i18n keys `INTERNAL_NOTE_BILLABLE` → `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and `INTERNAL_NOTE_MISSING` → `INTERNAL_PREFIX_MISSING` in `public/i18n/de.json` and `en.json` and reword them to project level; verify no old key remains (`grep INTERNAL_NOTE public/i18n` is empty)
- [x] 6.2 Add component tests rendering both new warning types as translated text with a date chip and no hours; verify `npm test` in mega-frontend-v2 is green
- [x] 6.3 Run manual tests 1–8 from `frontend-handoff.md` against this backend branch; verify every expectation holds in DE and EN

## Workflow follow-up

- Archive the change after review and after group 6 is done.
