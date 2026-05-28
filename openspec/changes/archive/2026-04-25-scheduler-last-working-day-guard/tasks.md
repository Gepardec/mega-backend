## 1. Extend OfficeCalendarUtil

- [x] 1.1 Add static method `isLastWorkingDayOfMonth(LocalDate date)` to `com.gepardec.mega.hexagon.shared.domain.util.OfficeCalendarUtil` — derive the last working day via `getWorkingDaysForYearMonth(YearMonth.from(date)).getLast()` and compare with `date`
- [x] 1.2 Add unit tests for `isLastWorkingDayOfMonth` covering: last working day is last calendar day, last working day is before last calendar day (weekend), last working day is before last calendar day (public holiday), a mid-month date returns false

## 2. Update the Scheduler

- [x] 2.1 Change the cron expression in `MonthEndTaskGenerationScheduler` from `0 0 0 L * ? *` to `0 0 0 25-31 * ? *`
- [x] 2.2 Add a guard at the top of `generateMonthEndTasks()`: call `OfficeCalendarUtil.isLastWorkingDayOfMonth(LocalDate.now())`; if false, log at DEBUG and return without calling the use case

## 3. Update Tests

- [x] 3.1 Update the cron assertion in `MonthEndTaskGenerationSchedulerTest` to expect `0 0 0 25-31 * ? *`
- [x] 3.2 Add test: when today is not the last working day, `generateMonthEndTasks()` does not invoke `GenerateMonthEndTasksUseCase`
- [x] 3.3 Add test: when today is the last working day, `generateMonthEndTasks()` invokes `GenerateMonthEndTasksUseCase` with `YearMonth.now()`
