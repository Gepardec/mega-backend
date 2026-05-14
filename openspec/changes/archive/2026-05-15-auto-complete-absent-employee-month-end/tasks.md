## 1. System Actor Foundation

- [x] 1.1 Add `SYSTEM` value to `com.gepardec.mega.hexagon.shared.domain.model.Role` enum
- [x] 1.2 Create `com.gepardec.mega.hexagon.shared.domain.model.SystemActor` with `USER_ID` constant (well-known UUID)
- [x] 1.3 Update `User` domain record: make `zepUsername` and `email` conditionally required (null allowed when `roles.contains(Role.SYSTEM)`)
- [x] 1.4 Add Liquibase changelog seeding the system actor row in `hexagon_users` and `hexagon_user_roles`

## 2. MonthEndTask Domain Changes

- [x] 2.1 Add `completeBySystem()` method to `MonthEndTask` — sets `status=DONE`, `completedBy=SystemActor.USER_ID`, no eligibility check
- [x] 2.2 Update `MonthEndTask.validateCompletionState` to allow `completedBy == SystemActor.USER_ID` as a valid bypass

## 3. MonthEndClarification Domain Changes

- [x] 3.1 Add `MonthEndClarification.createBySystem()` factory method — bypasses `validateCreator`, sets `createdBy=SystemActor.USER_ID`, `sourceSystem=MEGA`
- [x] 3.2 Update `MonthEndClarification.canDelete` to return false when `createdBy.equals(SystemActor.USER_ID)`
- [x] 3.3 Update `MonthEndClarification.canResolve` to return true only for `eligibleActorIds` members when `createdBy.equals(SystemActor.USER_ID)` (subject employee excluded)

## 4. Worktime BC — Absence Use Case

- [x] 4.1 Create `Absence` record in `worktime/domain/model` (`LocalDate date`, `AbsenceType type`)
- [x] 4.2 Create `AbsenceType` enum in `worktime/domain/model` with all ZEP absence categories
- [x] 4.3 Create `WorkTimeAbsenceZepPort` outbound port in `worktime/domain/port/outbound`
- [x] 4.4 Implement `WorkTimeAbsenceZepAdapter` in `worktime/adapter/outbound` mapping ZEP absence records to `Absence` domain objects (unknown codes → `AbsenceType.OTHER`)
- [x] 4.5 Create `GetEmployeeAbsencesUseCase` inbound port in `worktime/application/port/inbound`
- [x] 4.6 Implement `GetEmployeeAbsencesService` — resolves `UserId` → `ZepUsername` via `WorkTimeUserSnapshotPort`, delegates to `WorkTimeAbsenceZepPort`

## 5. MonthEnd BC — Absence Port and ACL Adapter

- [x] 5.1 Create `MonthEndEmployeeAbsencePort` outbound port in `monthend/domain/port/outbound` (`List<LocalDate> findQualifyingAbsentDays(UserId employeeId, YearMonth month)`)
- [x] 5.2 Implement `MonthEndWorkTimeAbsenceAdapter` (ACL) in `monthend/adapter/outbound` — calls `GetEmployeeAbsencesUseCase`, filters out `HOME_OFFICE` and `EXTERNAL_TRAINING`, returns qualifying dates

## 6. MonthEnd BC — Auto-Completion Use Case

- [x] 6.1 Create `AbsentEmployeeAutoCompletion` result record in `monthend/application/port/inbound`
- [x] 6.2 Create `CompleteTasksForAbsentEmployeeUseCase` inbound port in `monthend/application/port/inbound`
- [x] 6.3 Implement `CompleteTasksForAbsentEmployeeService` — checks full-month absence via `OfficeCalendarUtil.isWorkingDay`, completes open subject tasks via `completeBySystem()`, creates one clarification per distinct `projectId` via `createBySystem()`

## 7. Scheduler

- [x] 7.1 Implement `AbsentEmployeeMonthEndScheduler` in `monthend/adapter/inbound` — cron `0 0 17 L * ?`, fan-out over `MonthEndUserSnapshotPort.findActiveIn(currentMonth)`, log each result

## 8. REST Adapter — System Actor Display

- [x] 8.1 Update REST adapters that resolve `completedBy`/`createdBy` actor display names to handle `SystemActor.USER_ID` as a special case returning "MEGA System" without a snapshot lookup

## 9. Tests

- [x] 9.1 Unit test `MonthEndTask.completeBySystem()` — task transitions to DONE with SystemActor.USER_ID as completedBy
- [x] 9.2 Unit test `MonthEndClarification.createBySystem()` — bypasses validateCreator, canDelete returns false, canResolve returns false for subject employee and true for eligible lead
- [x] 9.3 Unit test `CompleteTasksForAbsentEmployeeService` — fully absent employee (tasks completed, clarifications created), partially absent (no-op), no open tasks (clarifications not created)
- [x] 9.4 Unit test `MonthEndWorkTimeAbsenceAdapter` — HOME_OFFICE and EXTERNAL_TRAINING filtered, VACATION included
- [x] 9.5 Unit test `GetEmployeeAbsencesService` — unknown employee throws exception, happy path returns mapped absences
- [x] 9.6 Unit test `User` invariant — system actor allows null zepUsername/email; regular user rejects null
- [x] 9.7 Integration test `AbsentEmployeeMonthEndScheduler` — verify guard skips on non-last-working-day
