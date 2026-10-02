# Tasks

## 1. Already implemented on feature/827 (verify only)

- [x] 1.1 Confirm the midnight mapping matches `worktime-bookings` ("A record ending at 24:00 ends at the start of the following day") and verify `WorkTimeBookingZepMapperTest` passes
- [x] 1.2 Confirm the exact 22:00 boundary matches `worktime-warning-core-working-time` and verify the 22:00 / 22:30 / 24:00 cases in `CoreWorkingHoursCalculatorTest` pass

## 2. Behaviour changes

- [x] 2.1 Rewrite the doctor-appointment window check to compare full start/end timestamps against [08:30, 12:00] and [12:30, 17:00] on the booking date; add tests for 12:00–12:30, 11:00–12:30 and 18:00–24:00 (flagged) and 08:30–12:00, 12:30–17:00 (permitted), and verify `DoctorAppointmentCalculatorTest` passes
- [x] 2.2 Make the core-working-time check ignore zero-duration bookings when determining a day's first and last booking, replacing the whole-day exemption; add tests for "zero-duration journey at 05:00 + end at 23:00 → warning" and "only zero-duration booking outside core time → no warning", adapt the existing zero-duration tests, and verify `CoreWorkingHoursCalculatorTest` passes
- [x] 2.3 Remove the "no journeys in the month" short-circuit from the working-location check so non-main locations outside an active trip are flagged in every month; add tests for "no journey + booking at A → warning" and "no journey + only main location → no warning", and verify `InvalidWorkingLocationCalculatorTest` passes
- [x] 2.4 De-duplicate assembled warnings per type and date (keep first occurrence and order) in the warning assembly; add an assembler test with two same-type warnings on one date and verify `WorkTimeWarningAssemblerTest` passes
- [x] 2.5 Replace per-booking multiplicity expectations in existing tests (e.g. `calculate_whenSeveralAppointmentsOnDifferentDates_thenOneWarningPerOffendingAppointment`, working-location and journey-pairing tests) where they assert same-type duplicates on one date at assembly level, and verify the affected test classes pass
- [x] 2.6 Make the journey pairing never open a trip with an onward journey (after a return journey the state behaves like the initial one), so every onward or return journey without an open trip yields `TO_MISSING`; add tests for TO, BACK, FURTHER, BACK (two `TO_MISSING`) and TO, BACK, FURTHER, TO, BACK (no `BACK_MISSING`), and verify `InvalidJourneyCalculatorTest` and `JourneyDirectionScannerTest` pass
- [x] 2.7 Round the missing break time once after summing the breaks instead of per break; adapt the two-10-minute-breaks test to 0.17 hours and verify `InsufficientBreakCalculatorTest` passes

## 3. Spec conformance of tests

- [x] 3.1 Walk through every scenario of the twelve `worktime-warning-*` specs and ensure each has a corresponding test in the matching calculator test class (add missing ones, e.g. 24:00 cases for max daily working time and rest time, the month-boundary limitation for journey pairing, the home-office case for no-time-entry); verify all warning test classes pass
- [x] 3.2 Remove or rename tests and test names that assert "legacy parity" rather than a specified rule, and verify `WorkTimeWarningCalculatorsTest` passes

## 4. Verification

- [x] 4.1 Run `mvn test` and verify the full suite passes
- [x] 4.2 Run `openspec validate rewrite-worktime-warning-specs --strict` and verify the change is valid
