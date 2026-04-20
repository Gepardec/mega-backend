## 1. Shared Foundation

- [x] 1.1 Relocate `OfficeCalendarUtil` to `hexagon/shared/domain/util/` and update all existing usages
- [x] 1.2 Add `findByRole(Role role)` to `UserRepository` port and implement in `UserRepositoryAdapter`
- [x] 1.3 Define `ZepClarificationMailReceived` integration event record in `hexagon/shared/domain/event/`

## 2. Notification Domain

- [x] 2.1 Define `MailScheduleType` enum (`WORKING_DAY_BASED`, `DAY_OF_MONTH_BASED`) in `notification/domain/model/`
- [x] 2.2 Define `ReminderType` enum (with day offset + `MailScheduleType`) in `notification/domain/model/`, migrating scheduling fields from the legacy `Mail` enum
- [x] 2.3 Define `ZepProjektzeitEntry` record in `notification/domain/model/` (migrated from `ZepProjektzeitDetailsMail`)
- [x] 2.4 Implement `ReminderSchedulePolicy` domain service in `notification/domain/service/` (migrated from `BusinessDayCalculator`, using shared `OfficeCalendarUtil`)
- [x] 2.5 Define `NotificationMailPort` outbound port in `notification/domain/port/outbound/`
- [x] 2.6 Define `ZepMailboxPort` outbound port in `notification/domain/port/outbound/`

## 3. Notification Use Cases

- [x] 3.1 Define `SendScheduledRemindersUseCase` driver port in `notification/application/port/inbound/`
- [x] 3.2 Implement `SendScheduledRemindersService` (migrated from `ReminderEmailSender.sendReminder()`), injecting `ReminderSchedulePolicy`, `UserRepository`, and `NotificationMailPort`
- [x] 3.3 Define `ProcessZepMailUseCase` driver port in `notification/application/port/inbound/`
- [x] 3.4 Implement `ProcessZepMailService` (migrated from `MailReceiver` + `ZepMailToCommentService` orchestration), injecting `ZepMailboxPort`, `ZepMailMessageParser`, `UserRepository`, and `NotificationMailPort`; fires `ZepClarificationMailReceived` via CDI `Event<>`

## 4. Notification Outbound Adapters

- [x] 4.1 Implement `QuarkusMailNotificationAdapter` implementing `NotificationMailPort`, delegating to existing `MailSender` and `NotificationHelper`
- [x] 4.2 Implement `ImapZepMailboxAdapter` implementing `ZepMailboxPort` (migrated from `MailReceiver` IMAP logic)
- [x] 4.3 Implement `ZepMailMessageParser` adapter (migrated from `ZepProjektzeitDetailsMailMapper`), converting `jakarta.mail.Message` → `Optional<ZepProjektzeitEntry>`

## 5. Notification Inbound Adapters

- [x] 5.1 Implement `ReminderEmailSchedulerAdapter` with Quartz `@Scheduled` cron, invoking `SendScheduledRemindersUseCase`
- [x] 5.2 Implement `ZepMailWebhookResource` REST adapter at `/pubsub/message-received`, replacing `PubSubResourceImpl`, invoking `ProcessZepMailUseCase`
- [x] 5.3 Implement `ClarificationLifecycleNotificationAdapter` with CDI `@Observes` for `ClarificationCreatedEvent`, `ClarificationCompletedEvent`, and `ClarificationDeletedEvent`; suppress mail for ZEP-sourced `ClarificationCreatedEvent`

## 6. MonthEnd Domain Events

- [x] 6.1 Define `ClarificationCreatedEvent` record in `hexagon/monthend/domain/event/` (fields: clarification id, sourceSystem, creator, subject employee, month, project)
- [x] 6.2 Define `ClarificationCompletedEvent` record in `hexagon/monthend/domain/event/` (fields: clarification id, resolver)
- [x] 6.3 Define `ClarificationDeletedEvent` record in `hexagon/monthend/domain/event/` (fields: clarification id, original creator)
- [x] 6.4 Fire `ClarificationCreatedEvent` from `CreateMonthEndClarificationService` after successful persist
- [x] 6.5 Fire `ClarificationCompletedEvent` from `CompleteMonthEndClarificationService` after successful persist
- [x] 6.6 Fire `ClarificationDeletedEvent` from `DeleteMonthEndClarificationService` after successful delete

## 7. MonthEnd ZEP Integration Adapter

- [x] 7.1 Add `findByFullName(FullName fullName)` to `UserRepository` port and implement in `UserRepositoryAdapter`
- [x] 7.2 Implement `ZepClarificationMailAdapter` inbound adapter with CDI `@Observes ZepClarificationMailReceived`; resolves subject employee via `UserRepository.findByFullName()`, invokes `CreateMonthEndClarificationUseCase` with `sourceSystem = ZEP` treating the mail sender as a project-lead-style creator

## 8. Tests

- [x] 8.1 Unit test `ReminderSchedulePolicy` for all schedule types, positive/negative offsets, and edge cases (weekend, holiday boundaries)
- [x] 8.2 Unit test `SendScheduledRemindersService` — verify correct role lookups and mail dispatch per reminder type; verify no-op when policy returns empty
- [x] 8.3 Unit test `ProcessZepMailService` — verify event firing on valid parse, error notification on exception, independent processing of multiple messages
- [x] 8.4 Unit test `ZepMailMessageParser` — verify valid message parsing, empty return for invalid subject/body
- [x] 8.5 Unit test `ClarificationLifecycleNotificationAdapter` — verify correct mail type per event; verify ZEP-source suppression
- [x] 8.6 Unit test `ZepClarificationMailAdapter` — verify billable/non-billable project routing; verify exception on unknown employee

## 9. Cleanup and Wiring Verification

- [x] 9.1 Verify `PubSubResourceImpl` is removed and `ZepMailWebhookResource` serves the same endpoint contract
- [x] 9.2 Verify no hexagon class imports `MailSender` or `NotificationHelper` directly (only `QuarkusMailNotificationAdapter` wraps them)
- [x] 9.3 Run `ArchitectureTest` and fix any new layer violations introduced by this change
