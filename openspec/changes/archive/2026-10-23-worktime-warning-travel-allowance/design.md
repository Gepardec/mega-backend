# Design

## Context

Motivation: see proposal.md, section "Why". Requirements: see `specs/worktime-warning-travel-allowance/spec.md`.

Current state:

- `ZepAttendance` (REST DTO, `zep/rest/dto`) does not read `start` and `destination`, although ZEP's attendance response contains both (see the test fixtures under `src/test/resources/zep/rest/testresponses/attendances`). `JourneyBooking` carries direction and vehicle, but no places. `WorkTimeBookingZepMapper` runs with `unmappedTargetPolicy = ERROR`.
- `Vehicle.isActiveTraveler()` already separates active from passive journeys. `CAR_INACTIVE` and `OTHER_INACTIVE` are passive, and `WorkTimeBookings.contributingToWorkingTime()` already uses this distinction.
- `ProjectBooking.process` holds ZEP's `project_task_id` as a string. The process "Reisepauschale" has id 873 in both the test and the production ZEP instance. ZEP process ids are unique across projects.
- Since #800, `GetEmployeeWarningsService` collects the project ids of all bookings (project and journey bookings), resolves the billable subset via `WorkTimeProjectSnapshotPort.findBillableZepIds` and passes it to `WorkTimeWarningAssembler.assemble(...)`. Unknown projects are dropped from that set. `NonBillableOnBillableProjectCalculator` is the precedent for a calculator with its own signature that the assembler calls explicitly.
- `InsufficientBreakCalculator` is the precedent for warning hours: minutes divided by 60, rounded `HALF_EVEN` to two decimals.

## Goals / Non-Goals

**Goals:**
- Keep the business rules (which places exist, which routes are short, which process is the travel allowance) in the domain, with no configuration and no new ports.
- Reuse the billable-project set that the service already computes.

**Non-Goals:**
- No configuration for routes, allowances or the process id (agreed: they change about as often as the policy itself; a code change is enough).
- No lookup of process names in ZEP.
- No geographic distance calculation or place database.
- No pairing of journeys into trips. Each journey is judged on its own (see spec).
- No frontend translations in this repository.
- No aliases beyond the three places (e.g. "Vienna") and no short routes beyond St. Pölten–Wien and St. Pölten–Linz. They will be added when production shows edge cases; doing so only extends the constants in decisions 3 and 4.

## Decisions

### 1. A standalone calculator called explicitly by the assembler

`TravelAllowanceCalculator` gets `calculate(WorkTimeBookings bookings, Set<Integer> billableProjectZepIds)`, like `NonBillableOnBillableProjectCalculator`. The assembler holds it as another separate field, calls it after the regular calculators, and passes the `billableProjectZepIds` it already receives. The package-private test constructor gets the calculator as an argument. Its warnings go through the existing deduplication per type and date. `GetEmployeeWarningsService` and `WorkTimeProjectSnapshotPort` stay unchanged.

- *Alternative:* now introduce the `calculate(WarningContext)` interface that #800's design deferred "until a third rule needs input beyond the bookings". This is only the second rule that needs billability. Widening all calculators is still not worth it; reconsider when a rule needs a different kind of input.

### 2. Journeys carry the raw free text; recognition happens in the domain

`JourneyBooking` gets `String start` and `String destination`, both nullable. The mapper copies `attendance.start` and `attendance.destination` unchanged. `ZepAttendance` gets the two record components plus builder fields with `@JsonProperty("start")` and `@JsonProperty("destination")`. A missing value is `null`, never a mapping error, so a journey without places can't break the warnings request.

- *Alternative:* map the text to a `KnownPlace` in the adapter. That moves a business rule (which places we know and how lenient recognition is) into infrastructure, and it would throw away the raw text that later rules or diagnostics might need.

### 3. Place recognition by word tokens

The domain enum `KnownPlace` (`WIEN`, `LINZ`, `ST_POELTEN`) has `static Optional<KnownPlace> recognise(String text)`:

1. Lower-case the text, replace `ä/ö/ü/ß` with `ae/oe/ue/ss`, and split it into words at every non-letter character ("St.Pölten Hbf" becomes `st`, `poelten`, `hbf`).
2. `WIEN` matches the word `wien`, `LINZ` the word `linz`, and `ST_POELTEN` the word `poelten` (the name only occurs in St. Pölten, so the "St."/"Sankt" prefix doesn't need its own check) or the merged word `stpoelten`.
3. Exactly one matching place is a match. No match, or matches for more than one place, gives `Optional.empty()`.

Whole words keep "Wiener Neustadt" or "Linzer Straße" from matching. If a place isn't recognised, the journey gets the ordinary 30 minutes. A typo therefore can only make the check more lenient; it never causes a false warning.

- *Alternative:* substring matching. Simpler, but "Wiener Neustadt" would count as Wien and get the 15-minute short-route allowance, which would produce false warnings.
- *Alternative:* fuzzy matching (edit distance). Overkill for three places, and harder to explain to employees.

### 4. The allowance rule as a small domain policy

`TravelAllowancePolicy` (domain service, static constants):

- `SHORT_ROUTES`: unordered pairs `{ST_POELTEN, WIEN}` and `{ST_POELTEN, LINZ}`
- `allowanceFor(JourneyBooking)`: `Duration.ZERO` for active vehicles. Otherwise 15 minutes when both ends are recognised and form a short route, else 30 minutes.

Unordered pairs cover both directions with one entry per route. The same place on both ends isn't in the set, so it counts as an ordinary route (see spec).

### 5. Evaluation per day

The calculator only looks at dates on or after the cutoff that carry at least one travel-allowance booking. Days without such a booking can't produce a warning. For each such date:

1. `booked` = total duration of the `ProjectBooking`s whose `process` equals `"873"`.
2. Split the date's passive journeys into customer trips (`projectZepId` in `billableProjectZepIds`) and internal trips (all others, including `null`).
3. If there is at least one passive journey and all of them are customer trips: `TRAVEL_ALLOWANCE_WRONG_PROJECT`, no hours, and stop here.
4. Otherwise `excess = booked − Σ allowanceFor(internal trip)`. If `excess > 0`: `TRAVEL_ALLOWANCE_EXCEEDED` with the excess hours, rounded like `InsufficientBreakCalculator` does.

A journey belongs to the date it starts on (`booking.date()`), consistent with all other rules.

### 6. Process id and cutoff as the calculator's own constants

`REISEPAUSCHALE_PROCESS = "873"` and `RULE_START = LocalDate.of(2026, 10, 1)` are constants of the calculator. The cutoff has the same value as the other convention rules but stays separate on purpose, as in #800.

- *Alternative:* the process id in `application.yaml`. The id is the same in every environment, so a config entry would add an injection path into a pure domain class without any benefit.

## Risks / Trade-offs

- [ZEP re-creates the "Reisepauschale" process with a new id.] → The rule then silently finds no travel-allowance bookings. It produces no false warnings, only missed ones. The fix is a one-line constant change; the constant's comment names the process.
- [Free text that isn't recognised gives the 30-minute default.] → Too much travel allowance on a short route can go unreported. False warnings are worse than missed ones for adoption; aliases are added when production shows edge cases (see Non-Goals).
- [Travel allowance on customer projects is not checked at all.] → Agreed scope. Those bookings are usually non-billable and already surface through `NON_BILLABLE_ON_BILLABLE_PROJECT`.
- [Adding components to the `JourneyBooking` record breaks every `new JourneyBooking(...)` call.] → The change is mechanical, and tests mostly go through `WarningTestBookingBuilder` (which gets `start`/`destination` setters with `null` defaults) or Instancio.
- [Adding components to the `ZepAttendance` record touches the legacy package.] → The record is only constructed through its builder. Legacy mappers ignore the new accessors.
- [Billability is read as of now.] → Same behaviour as #800; warnings aren't persisted.

## Migration Plan

- No data migration; warnings are recalculated on every request.
- Roll out the frontend translation keys for `TRAVEL_ALLOWANCE_EXCEEDED` and `TRAVEL_ALLOWANCE_WRONG_PROJECT` together with or before the backend.
- Rollback: revert the backend release; no persisted data is created.
