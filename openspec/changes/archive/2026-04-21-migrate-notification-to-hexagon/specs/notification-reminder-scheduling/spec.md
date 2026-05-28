## ADDED Requirements

### Requirement: Reminder schedule policy computes which reminders fire on a given date
The system SHALL provide a pure domain service `ReminderSchedulePolicy` that, given a `LocalDate`, returns the set of `ReminderType` values whose scheduled send date matches that date. The policy SHALL derive send dates from the first working day of the month and SHALL use `OfficeCalendarUtil` for working-day calculations. The policy SHALL have no dependency on any infrastructure or external system.

`ReminderType` SHALL encode a day offset (positive = nth working day from month start, negative = nth working day back from month end) and a `MailScheduleType` (WORKING_DAY_BASED or DAY_OF_MONTH_BASED). The policy SHALL support both schedule types.

#### Scenario: Reminder with positive working-day offset fires on the correct date
- **WHEN** `ReminderSchedulePolicy.getRemindersForDate(date)` is called for a date that is the 5th working day of the month
- **THEN** the policy returns all `ReminderType` values whose day offset is `5` and schedule type is `WORKING_DAY_BASED`

#### Scenario: Reminder with negative working-day offset fires on the correct date
- **WHEN** `ReminderSchedulePolicy.getRemindersForDate(date)` is called for a date that is the 5th-to-last working day of the month
- **THEN** the policy returns all `ReminderType` values whose day offset is `-5` and schedule type is `WORKING_DAY_BASED`

#### Scenario: Reminder with day-of-month offset fires on the correct date or next working day
- **WHEN** `ReminderSchedulePolicy.getRemindersForDate(date)` is called and the configured day of month falls on a non-working day
- **THEN** the policy schedules the reminder to the next working day

#### Scenario: No reminders fire on a date with no matches
- **WHEN** `ReminderSchedulePolicy.getRemindersForDate(date)` is called for a date that matches no reminder schedule
- **THEN** the policy returns an empty set

### Requirement: Scheduled reminder use case dispatches emails to users by role
The system SHALL provide a `SendScheduledRemindersUseCase` that, when invoked with today's date, calls `ReminderSchedulePolicy` to determine which reminders fire, resolves the target users by role via `UserRepository`, and sends the appropriate mail via `NotificationMailPort` for each user.

`UserRepository` SHALL be extended with `findByRole(Role role)` returning `List<User>`.

The use case SHALL log a summary of sent reminders and SHALL log a warning if no users are found for a given role.

#### Scenario: Reminders are dispatched to all users with the matching role
- **WHEN** the scheduled reminder use case is invoked and the policy returns one or more reminder types
- **THEN** for each reminder type the use case finds all users with the required role and sends each user the corresponding mail via `NotificationMailPort`

#### Scenario: No reminders fired when policy returns empty
- **WHEN** the scheduled reminder use case is invoked and the policy returns no reminder types for today
- **THEN** no mails are sent and the use case logs that no reminders were due

#### Scenario: Warning logged when no users found for a role
- **WHEN** the scheduled reminder use case resolves a reminder type but no users hold the required role
- **THEN** the use case logs a warning and skips that reminder type without error

### Requirement: Reminder scheduler adapter triggers the use case on a daily schedule
The system SHALL provide a `ReminderEmailSchedulerAdapter` that invokes `SendScheduledRemindersUseCase` via a Quartz cron schedule once per day. The adapter SHALL log the start and outcome of each execution.

#### Scenario: Scheduler invokes use case on schedule
- **WHEN** the cron trigger fires
- **THEN** the adapter calls `SendScheduledRemindersUseCase` with today's date

### Requirement: OfficeCalendarUtil is available in the hexagon shared domain
The system SHALL relocate `OfficeCalendarUtil` to `hexagon/shared/domain/util/` so it is accessible to any hexagon BC without depending on the legacy package. The utility SHALL remain a pure static utility with no external dependencies.

#### Scenario: ReminderSchedulePolicy uses the shared OfficeCalendarUtil
- **WHEN** `ReminderSchedulePolicy` performs working-day calculations
- **THEN** it uses `OfficeCalendarUtil` from `hexagon/shared/domain/util/`
