## ADDED Requirements

### Requirement: ReminderType implements MailNotificationId
The system SHALL declare `ReminderType` as implementing the `MailNotificationId` sealed interface. `SendScheduledRemindersService` SHALL pass `ReminderType` values directly to `NotificationMailPort.send()` — no string conversion via `.name()`.

#### Scenario: Reminder use case passes ReminderType directly to the port
- **WHEN** `SendScheduledRemindersService` sends a reminder mail
- **THEN** it passes the `ReminderType` enum value directly as the `MailNotificationId` argument — not a string representation
