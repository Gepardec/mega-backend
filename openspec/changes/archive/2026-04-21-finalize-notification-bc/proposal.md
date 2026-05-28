## Why

The notification BC is functionally complete for business logic but its outbound adapter (`QuarkusMailNotificationAdapter`) still depends on legacy `MailSender`, `NotificationHelper`, and `Mail` enum — keeping a compile-time coupling to the legacy package that must be severed. Additionally, `NotificationMailPort` accepts a raw `String` as the mail type identifier, making the contract invisible to the compiler, and `UpdateMonthEndClarificationService` does not fire a domain event on text update, leaving clarification-updated notifications unimplemented.

## What Changes

- **NEW** `MailNotificationId` sealed interface in the notification domain; `ReminderType` and `ClarificationNotificationType` are the only permitted implementations
- **NEW** `ClarificationNotificationType` enum (`CLARIFICATION_CREATED`, `CLARIFICATION_UPDATED`, `CLARIFICATION_COMPLETED`, `CLARIFICATION_DELETED`, `ZEP_CLARIFICATION_PROCESSING_ERROR`) implementing `MailNotificationId`
- **NEW** `ClarificationUpdatedEvent` domain event in the monthend BC
- `UpdateMonthEndClarificationService.updateText()` fires `ClarificationUpdatedEvent` after saving
- `ClarificationLifecycleNotificationAdapter` gains an `onClarificationUpdated` observer
- **BREAKING** `NotificationMailPort.send()` signature changes from `String mailTypeId` to `MailNotificationId` — all callers updated
- `QuarkusMailNotificationAdapter` rewritten to absorb all template-loading and rendering logic from `MailSender` / `NotificationHelper`; no imports from the legacy `notification.mail` package remain
- Email template files renamed: `COMMENT_*.html` → `CLARIFICATION_*.html`, `ZEP_COMMENT_PROCESSING_ERROR.html` → `ZEP_CLARIFICATION_PROCESSING_ERROR.html`
- `messages.properties` and `messages_en.properties` keys renamed to match

## Capabilities

### New Capabilities

_(none — this change completes and hardens existing capabilities rather than introducing new ones)_

### Modified Capabilities

- `notification-clarification-lifecycle`: adds clarification-updated notification requirement; changes port signature to `MailNotificationId`; renames mail type identifiers from `COMMENT_*` to `CLARIFICATION_*`; removes spec requirement to delegate to legacy `MailSender`/`NotificationHelper`
- `notification-reminder-scheduling`: `ReminderType` implements `MailNotificationId`; port call sites updated to pass `ReminderType` directly instead of `reminderType.name()`
- `notification-zep-mail-processing`: error mail type renamed from `ZEP_COMMENT_PROCESSING_ERROR` to `ZEP_CLARIFICATION_PROCESSING_ERROR`; port call site updated to pass `ClarificationNotificationType.ZEP_CLARIFICATION_PROCESSING_ERROR` directly
- `monthend-clarifications`: `updateText()` now fires `ClarificationUpdatedEvent` — new observable side effect that enables the notification chain

## Impact

- `hexagon/notification/domain/` — new `MailNotificationId` interface, new `ClarificationNotificationType` enum
- `hexagon/notification/domain/port/outbound/NotificationMailPort` — signature change (breaking for all callers)
- `hexagon/notification/adapter/outbound/QuarkusMailNotificationAdapter` — full rewrite
- `hexagon/notification/adapter/inbound/ClarificationLifecycleNotificationAdapter` — new observer method, updated call sites
- `hexagon/notification/application/SendScheduledRemindersService` — updated call site
- `hexagon/notification/application/ProcessZepMailService` — updated call site
- `hexagon/monthend/domain/event/ClarificationUpdatedEvent` — new record
- `hexagon/monthend/application/UpdateMonthEndClarificationService` — fires event
- `src/main/resources/emails/` — 5 template files renamed
- `src/main/resources/messages*.properties` — 5 subject keys renamed
- Legacy classes (`MailSender`, `NotificationHelper`, `Mail`, `MailParameter`) — untouched; hexagon no longer imports them
