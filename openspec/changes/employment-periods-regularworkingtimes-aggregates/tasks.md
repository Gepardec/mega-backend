## 1. Add EmploymentPeriods aggregate to hexagon domain

- [ ] 1.1 Create `EmploymentPeriods` record in `com.gepardec.mega.hexagon.user.domain.model` with `empty()` factory, single-period constructor, `latest()`, `active(LocalDate)`, and `active(YearMonth)` methods — porting logic from the legacy class, using no external dependencies
- [ ] 1.2 Create `EmploymentPeriodsTest` in `com.gepardec.mega.hexagon.user.domain.model` covering all scenarios from the `employment-periods-aggregate` spec (latest, active with LocalDate, active with YearMonth)

## 2. Add RegularWorkingTimes aggregate to hexagon domain

- [ ] 2.1 Create `RegularWorkingTimes` record in `com.gepardec.mega.hexagon.user.domain.model` with `empty()` factory, single-entry constructor, `latest()`, `active(LocalDate)`, and `active(YearMonth)` methods — using standard Java only (no `commons-collections4`)
- [ ] 2.2 Create `RegularWorkingTimesTest` in `com.gepardec.mega.hexagon.user.domain.model` covering all scenarios from the `regular-working-times-aggregate` spec (latest, active with LocalDate edge cases, active with YearMonth)

## 3. Update ZepProfile to use aggregate types

- [ ] 3.1 Change `ZepProfile` fields from `List<EmploymentPeriod>` to `EmploymentPeriods` and from `List<RegularWorkingTime>` to `RegularWorkingTimes`

## 4. Update ZepEmployeeAdapter

- [ ] 4.1 Update `ZepEmployeeAdapter` to wrap the raw lists into `new EmploymentPeriods(...)` and `new RegularWorkingTimes(...)` when constructing `ZepProfile`

## 5. Verify and test

- [ ] 5.1 Run `mvn test` and confirm all tests pass with no regressions
