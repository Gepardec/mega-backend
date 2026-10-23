# Design

## Context

Motivation: see proposal.md, section "Why". Requirements: see `specs/worktime-warning-meal-entry/spec.md`.

Current state:

- `GetEmployeeWarningsService` fetches bookings, absences, expected working days and the billable project ids, then calls `WorkTimeWarningAssembler.assemble(bookings, expectedWorkingDays, excusedDates, billableProjectZepIds, today)`. `today` comes from the injected `Clock`. For a month without bookings it returns `EMPTY_ENTRY_LIST` early and fetches nothing else.
- `NonBillableOnBillableProjectCalculator` and `TravelAllowanceCalculator` are the precedent for calculators with their own signature that the assembler holds as separate fields and calls explicitly. The assembler does not know the payroll month today.
- `JourneyDirectionScanner` walks the journeys as a state machine (`NONE`/`OPEN`/`CLOSED`) but only reports `TO_MISSING`/`BACK_MISSING`. It doesn't tell which dates a trip covers.
- `NoEntryWarningCalculator` excludes the current date and later days with `date.isBefore(today)`.
- ZEP REST access for an employee lives in `ZepEmployeeRestClient` (`/employees/{username}/...`, `limit` set via `@ClientQueryParam`). `AbsenceService` pages through all of an employee's absences with `Multi.createBy().repeating()` until `links.next` is empty, then filters by month. `WorkTimeAbsenceZepAdapter` wraps that service behind a worktime outbound port.
- ZEP's meals endpoint `GET /employees/{username}/meals` has no date filter. Each entry has `id`, `employee_id`, `date` (a UTC midnight timestamp such as `2026-10-22T00:00:00.000000Z`), `breakfast`, `lunch` and `dinner`.

## Goals / Non-Goals

**Goals:**
- Keep the trip-day rule a pure domain function over the month's journeys, the month and the current date.
- Avoid the extra ZEP call for the common case of a month without journeys.

**Non-Goals:**
- No refactoring of `JourneyDirectionScanner` or `TO_MISSING`/`BACK_MISSING`. The two rules share the trip concept but answer different questions; merging them is optional later work.
- No domain model for breakfast, lunch and dinner. The rule only needs to know which dates have an entry.
- No look-ahead into adjacent months. Trips crossing a month boundary are handled by the month-start and month-end rules in the spec.
- No frontend translations in this repository.

## Decisions

### 1. Trip days as a small domain service

`TripDays.of(List<JourneyBooking> journeys, YearMonth month) : Set<LocalDate>` in `worktime/domain/services`. It walks the chronologically ordered journeys once and keeps the start date of the open trip, if there is one:

| journey  | trip open                                                    | no trip open                                                                       |
|----------|--------------------------------------------------------------|------------------------------------------------------------------------------------|
| outbound | interrupted: drop the open span, open a new trip at this date | open a trip at this date                                                           |
| onward   | stays open                                                   | first journey of the month: open a trip at month start. Otherwise: no trip opens   |
| return   | add every date from trip start to this date, close the trip  | first journey of the month: add month start to this date. Otherwise: nothing       |

After the walk, an open trip adds every date from its start to the end of the month. Every journey's own date is always added. The result is the union.

"First journey of the month" decides whether an unpaired onward or return journey continues a trip from the previous month. An unpaired return journey after a closed trip in the middle of the month is a booking mistake, not a month-crossing trip, so it only counts its own day.

- *Alternative:* extend `JourneyDirectionScanner` to also report spans. That couples two rules with different edge-case handling (the scanner flags an interrupted trip; this rule narrows it to journey days). A separate walk of about 20 lines is easier to read and test.

### 2. A standalone calculator with the month and meal dates

`MealEntryCalculator.calculate(WorkTimeBookings bookings, YearMonth month, Set<LocalDate> mealEntryDates, LocalDate today)`:

1. `TripDays.of(bookings.journeys(), month)`
2. keep dates `>= RULE_START` (2026-10-01) and `< today`, the same comparison as `NoEntryWarningCalculator`
3. drop dates contained in `mealEntryDates`
4. one `MEAL_ENTRY_MISSING` warning per remaining date, no hours

`RULE_START` is the calculator's own constant, as in #800 and #801.

The assembler holds the calculator as a separate field next to `TravelAllowanceCalculator`. `assemble(...)` gains the parameters `YearMonth month` and `Set<LocalDate> mealEntryDates`, and the package-private test constructor gets the calculator. Warnings go through the existing deduplication per type and date.

- *Alternative:* now bundle the assembler's inputs into a parameter record (the `WarningContext` idea that #800 and #801 deferred). The assembler grows from five to seven parameters, which is still readable, and the record would touch every existing assembler test. Keep the explicit parameters. Reconsider when the next rule adds another input.
- *Alternative:* derive the month from the bookings. That fails for months whose bookings don't cover the first or last day, which is exactly where the month-start and month-end rules apply.

### 3. Outbound port returns meal-entry dates only

`WorkTimeMealEntryZepPort.fetchMealEntryDates(ZepUsername zepUsername, YearMonth month) : Set<LocalDate>` in `application/port/outbound`. It returns the dates within the month that have a meal entry. The breakfast, lunch and dinner flags don't leave the adapter, because no rule uses them.

- *Alternative:* a `MealEntry` domain record with the three flags. Nothing reads the flags, so this would be unused model. Add it when a rule needs the flags.

### 4. ZEP access mirrors absences

- `ZepMeal` DTO in `zep/rest/dto` with the builder/`@JsonCreator` style of `ZepAbsence` and `@JsonIgnoreProperties(ignoreUnknown = true)`. `date` is read as `OffsetDateTime`; the record also exposes the flags so the DTO matches the response.
- `ZepEmployeeRestClient.getMealsByUsername(username, page)` for `GET /{username}/meals`. It inherits `limit` from the class-level `@ClientQueryParam`.
- `MealService` in `zep/rest/service` pages like `AbsenceService` and keeps entries whose date falls within the month. It logs the outgoing ZEP call with `io.quarkus.logging.Log`, without personal data.
- `WorkTimeMealEntryZepAdapter` in `worktime/adapter/outbound` implements the port and maps each `ZepMeal` to `date.toLocalDate()`, keeping the date in the offset ZEP sends it in. A missing date is skipped.

Taking the date in ZEP's own offset avoids shifting a UTC-midnight date into the previous day in a timezone west of UTC. Converting to Europe/Vienna gives the same date, but it adds a timezone assumption for no benefit.

- *Alternative:* a new `ZepMealRestClient`. The endpoint is a sub-resource of `/employees/{username}` like absences and employment periods, so it belongs in the existing client.

### 5. The service fetches meals only for months with journeys

`GetEmployeeWarningsService` calls the meal port only when `bookings.journeys()` is non-empty and passes `Set.of()` otherwise. It also passes the month to the assembler. A month without journeys has no trip days, so the result is the same. This avoids paging through the employee's full meal history for most requests.

## Risks / Trade-offs

- [The meals endpoint has no date filter, so the full history is paged on every warnings request with journeys.] → At 200 entries per page, a frequent traveller has well under 200 entries per year, so one or two requests. The call is skipped for months without journeys. If it becomes slow, a cache can be added later without changing the port.
- [A forgotten return journey at the end of the month extends the trip to the month's end.] → Accepted in exploration: `BACK_MISSING` already points to the cause, and a month-crossing trip is the more likely case.
- [A forgotten outbound journey before the first journey of the month starts the trip on the 1st.] → Same reasoning, with `TO_MISSING` pointing to the cause.
- [ZEP fails to answer the meals request.] → The warnings request fails, as it does for absences today. Failing loudly is better than silently dropping the meal reminders.
- [ZEP changes the timestamp format of `date`.] → The adapter test pins the current format. Unknown fields are already ignored.

## Migration Plan

- No data migration; warnings are recalculated on every request.
- Roll out the frontend translation key for `MEAL_ENTRY_MISSING` together with or before the backend.
- Rollback: revert the backend release; no persisted data is created.
