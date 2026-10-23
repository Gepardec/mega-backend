# Tasks

## 1. ZEP meal entries

- [x] 1.1 Add the `ZepMeal` DTO in `zep/rest/dto` per design.md decision 4 (builder/`@JsonCreator` style of `ZepAbsence`, `date` as `OffsetDateTime`, unknown fields ignored); write `ZepMealTest` that deserializes the sample response (`"date": "2026-10-22T00:00:00.000000Z"`, all three flags) and an entry with an unknown field, and verify with `mvn test -Dtest=ZepMealTest`
- [x] 1.2 Add `getMealsByUsername(username, page)` for `GET /{username}/meals` to `ZepEmployeeRestClient` and implement `MealService` in `zep/rest/service` that pages until `links.next` is empty, keeps entries within the given month and logs the outgoing call with `io.quarkus.logging.Log`; add a two-page fixture under `src/test/resources/zep/rest/testresponses/meals/` and write `MealServiceTest` (`@QuarkusTest`, `@InjectMock` rest client, like `EmploymentPeriodServiceTest`) covering both pages being read and entries outside the month being dropped; verify with `mvn test -Dtest=MealServiceTest`
- [x] 1.3 Add the outbound port `WorkTimeMealEntryZepPort.fetchMealEntryDates(ZepUsername, YearMonth)` and implement `WorkTimeMealEntryZepAdapter` mapping each entry to `date.toLocalDate()` and skipping entries without a date (design.md decisions 3 and 4); write `WorkTimeMealEntryZepAdapterTest` (plain Mockito, like `WorkTimeAbsenceZepAdapterTest`) covering the date mapping, an entry with all flags false still yielding its date, and a missing date; verify with `mvn test -Dtest=WorkTimeMealEntryZepAdapterTest`

## 2. Trip days

- [x] 2.1 Implement `TripDays.of(List<JourneyBooking>, YearMonth)` per design.md decision 1; write `TripDaysTest` covering every scenario of the requirements "A closed trip makes every day from outbound to return a trip day", "Every day with a journey is a trip day", "A trip still open at the end of the month runs until the end of the month", "A trip continued from the previous month starts on the first day of the month" and "A trip interrupted by a new outbound journey only counts its journey days" in `specs/worktime-warning-meal-entry/spec.md`, plus an empty journey list; verify with `mvn test -Dtest=TripDaysTest`

## 3. Warning rule

- [x] 3.1 Add `MEAL_ENTRY_MISSING` to `WorkTimeWarningType`; verify that `mvn compile` succeeds
- [x] 3.2 Implement `MealEntryCalculator.calculate(WorkTimeBookings, YearMonth, Set<LocalDate> mealEntryDates, LocalDate today)` per design.md decision 2 (cutoff 2026-10-01, only dates before `today`, no hours); write `MealEntryCalculatorTest` covering every scenario of the requirements "A trip day without a meal entry is reported" and "Only past trip days from 2026-10-01 onward are checked"; verify with `mvn test -Dtest=MealEntryCalculatorTest`
- [x] 3.3 Wire the calculator into `WorkTimeWarningAssembler` as a separate field next to `TravelAllowanceCalculator`, add the `YearMonth month` and `Set<LocalDate> mealEntryDates` parameters to `assemble(...)`, extend the package-private constructor, update all call sites in `WorkTimeWarningAssemblerTest`, and add a case in which an October 2026 day trip without a meal entry before `today` yields `MEAL_ENTRY_MISSING` for that date; verify with `mvn test -Dtest=WorkTimeWarningAssemblerTest`

## 4. Use case

- [x] 4.1 Inject `WorkTimeMealEntryZepPort` into `GetEmployeeWarningsService`, fetch meal-entry dates only when the month has journeys, and pass them together with the month to the assembler (design.md decision 5); extend `GetEmployeeWarningsServiceTest` with: a month with a trip day without a meal entry yielding `MEAL_ENTRY_MISSING`, a month with a meal entry for that day yielding none, and a month without journeys not calling the meal port; verify with `mvn test -Dtest=GetEmployeeWarningsServiceTest`

## 5. Integration

- [x] 5.1 Run the full build with `mvn clean package` and verify that all tests including `ArchitectureTest` pass
- [x] 5.2 Run `openspec validate add-worktime-meal-entry-warning --strict` and verify that the change validates without errors

## Workflow follow-up

- Have the frontend translation key for `MEAL_ENTRY_MISSING` created in the frontend repository and agree on the release timing.
