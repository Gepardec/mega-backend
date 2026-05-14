## ADDED Requirements

### Requirement: Open monthend tasks are auto-completed for employees absent an entire month
The system SHALL provide `CompleteTasksForAbsentEmployeeUseCase` in the `monthend` BC. When called for a given `UserId` and `YearMonth`, the use case SHALL:
1. Retrieve qualifying absent days via `MonthEndEmployeeAbsencePort` (excludes `HOME_OFFICE` and `EXTERNAL_TRAINING` absence types)
2. Check whether every working day of the month (per `OfficeCalendarUtil.isWorkingDay`) is covered by a qualifying absence
3. If not fully absent: return `Optional.empty()` without modifying any state
4. If fully absent: complete all open subject tasks via `task.completeBySystem()` and create one `MonthEndClarification` per distinct `projectId` via `MonthEndClarification.createBySystem()` with `SystemActor.USER_ID` as creator and the text "Aufgrund von Abwesenheiten wurde der Monat automatisch bestätigt."
5. Return `Optional.of(new AbsentEmployeeAutoCompletion(employeeId, month))`

#### Scenario: Fully absent employee has their open tasks completed
- **WHEN** `CompleteTasksForAbsentEmployeeUseCase.complete(employeeId, month)` is called and the employee was absent every working day
- **THEN** all open `MonthEndTask` records where `subjectEmployeeId` equals that employee are set to `DONE`
- **THEN** `completedBy` on each task is `SystemActor.USER_ID`
- **THEN** the use case returns a present `Optional<AbsentEmployeeAutoCompletion>`

#### Scenario: One clarification is created per distinct project
- **WHEN** the absent employee had open tasks across two distinct projects
- **THEN** exactly two `MonthEndClarification` records are created, one per project
- **THEN** each clarification has `createdBy` equal to `SystemActor.USER_ID`
- **THEN** the clarification text is "Aufgrund von Abwesenheiten wurde der Monat automatisch bestätigt."

#### Scenario: Non-absent employee is skipped
- **WHEN** `CompleteTasksForAbsentEmployeeUseCase.complete(employeeId, month)` is called and the employee was present on at least one working day
- **THEN** no tasks are modified
- **THEN** no clarifications are created
- **THEN** the use case returns `Optional.empty()`

#### Scenario: Already-done tasks are not re-completed
- **WHEN** the use case is called for a fully absent employee whose tasks are already `DONE`
- **THEN** no tasks change state
- **THEN** the use case returns `Optional.empty()`

#### Scenario: No open tasks results in no clarifications and no completion
- **WHEN** the use case is called for a fully absent employee who has no open subject tasks
- **THEN** no clarifications are created
- **THEN** the use case returns `Optional.empty()`

### Requirement: The absent-employee auto-completion runs as a scheduled job on the last working day of the month at end of business
The system SHALL provide `AbsentEmployeeMonthEndScheduler` in `monthend/adapter/inbound`. It SHALL run at **17:00 on the last day of each month** (cron day-of-month `L`), with no additional working-day guard. When triggered, it SHALL call `CompleteTasksForAbsentEmployeeUseCase` once per active user in the current month (via `MonthEndUserSnapshotPort.findActiveIn`). Each auto-completion result SHALL be logged at `INFO` level.

#### Scenario: Scheduler fans out over all active users on the last working day
- **WHEN** the scheduler fires on the last working day of a month
- **THEN** `CompleteTasksForAbsentEmployeeUseCase.complete` is called once per active user
- **THEN** auto-completions are logged at INFO level

#### Scenario: Scheduler skips execution on non-last working days
- **WHEN** the scheduler fires on a day that is not the last working day of the month
- **THEN** no use case calls are made

### Requirement: monthend BC resolves qualifying absent days through its own outbound port
The system SHALL define `MonthEndEmployeeAbsencePort` in `monthend/domain/port/outbound` returning `List<LocalDate>` of days on which the employee had a qualifying absence (i.e., excluding `HOME_OFFICE` and `EXTERNAL_TRAINING`). The port SHALL be implemented by `MonthEndWorkTimeAbsenceAdapter` (ACL), which calls `GetEmployeeAbsencesUseCase` from the `worktime` BC and filters the result. The `monthend` application service SHALL NOT import any type from the `worktime` BC.

#### Scenario: HOME_OFFICE absences are excluded from qualifying days
- **WHEN** `MonthEndEmployeeAbsencePort.findQualifyingAbsentDays` is called for an employee whose only ZEP absences are `HOME_OFFICE`
- **THEN** the result is an empty list

#### Scenario: VACATION absences are included as qualifying days
- **WHEN** `MonthEndEmployeeAbsencePort.findQualifyingAbsentDays` is called for an employee on vacation for the whole month
- **THEN** the result contains one `LocalDate` per vacation day
