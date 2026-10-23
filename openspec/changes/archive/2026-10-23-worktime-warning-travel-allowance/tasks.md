# Tasks

## 1. Start and destination on journey bookings

- [x] 1.1 Add the record components `start` and `destination` with builder fields `@JsonProperty("start")` / `@JsonProperty("destination")` to `ZepAttendance`; extend `ZepAttendanceTest` to deserialize both from JSON (and `null` when absent) and verify with `mvn test -Dtest=ZepAttendanceTest`
- [x] 1.2 Add nullable `String start` and `String destination` to `JourneyBooking`; `WorkTimeBookingZepMapper` maps them unchanged per design.md decision 2. Extend `WorkTimeBookingZepMapperTest` with the scenarios "Start and destination are carried over unchanged" and "Missing start and destination yield a journey without them" and verify with `mvn test -Dtest=WorkTimeBookingZepMapperTest`
- [x] 1.3 Give the journey builder in `WarningTestBookingBuilder` `start`/`destination` setters with `null` defaults; verify with `mvn test -Dtest='com.gepardec.mega.hexagon.worktime.**'` that all existing worktime tests still pass

## 2. Places and route allowance

- [x] 2.1 Implement the domain enum `KnownPlace` with `recognise(String)` per design.md decision 3 (normalise umlauts, split into words, exactly one match); write `KnownPlaceTest` (parameterized) covering every scenario of the requirement "Known places are recognised despite spelling variants" plus `null`/blank input, and verify with `mvn test -Dtest=KnownPlaceTest`
- [x] 2.2 Implement `TravelAllowancePolicy.allowanceFor(JourneyBooking)` per design.md decision 4 (active → 0, short route → 15 min, otherwise 30 min); write `TravelAllowancePolicyTest` covering the scenarios of the requirement "Routes between St. Pölten and Wien or Linz are short routes" (both directions, St. Pölten–Salzburg, unrecognised place, missing start, same place on both ends) and the active vehicle, and verify with `mvn test -Dtest=TravelAllowancePolicyTest`

## 3. Warning rule

- [x] 3.1 Add `TRAVEL_ALLOWANCE_EXCEEDED` and `TRAVEL_ALLOWANCE_WRONG_PROJECT` to `WorkTimeWarningType`; verify that `mvn compile` succeeds
- [x] 3.2 Implement `TravelAllowanceCalculator.calculate(WorkTimeBookings, Set<Integer> billableProjectZepIds)` per design.md decisions 5 and 6 (process `"873"`, internal vs. customer trips, wrong project takes precedence, excess hours rounded to two decimals, cutoff 2026-10-01)
- [x] 3.3 Write `TravelAllowanceCalculatorTest` covering every scenario of the requirements on allowance per journey, travel-allowance bookings, internal vs. customer trips, exceeded, wrong project and cutoff in `specs/worktime-warning-travel-allowance/spec.md`; verify with `mvn test -Dtest=TravelAllowanceCalculatorTest`
- [x] 3.4 Wire the calculator into `WorkTimeWarningAssembler` as a separate field next to `NonBillableOnBillableProjectCalculator`, passing the existing `billableProjectZepIds`; extend the package-private constructor, update its call sites in `WorkTimeWarningAssemblerTest`, and add a case in which a day in October 2026 with 30 minutes on process 873 and no journey yields `TRAVEL_ALLOWANCE_EXCEEDED` with `hours` 0.5; verify with `mvn test -Dtest=WorkTimeWarningAssemblerTest`

## 4. Integration

- [x] 4.1 Extend the REST test for `GET /worktime/warnings/{payrollMonth}` (`WorkTimeEmployeeAndProjectLeadResourceTest`) with an October 2026 case: a passive journey from "St. Pölten" to "Wien" on a non-billable project plus 30 minutes on process 873 on the same day, expecting a warning with `type` `TRAVEL_ALLOWANCE_EXCEEDED`, the booking date and `hours` 0.25; verify that the test passes
- [x] 4.2 Run the full build with `mvn clean package` and verify that all tests including `ArchitectureTest` pass
- [x] 4.3 Run `openspec validate worktime-warning-travel-allowance --strict` and verify that the change validates without errors

## Workflow follow-up

- Have the frontend translation keys for `TRAVEL_ALLOWANCE_EXCEEDED` and `TRAVEL_ALLOWANCE_WRONG_PROJECT` created in the frontend repository and agree on the release timing.
