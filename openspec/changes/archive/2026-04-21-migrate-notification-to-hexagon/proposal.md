## Why

The `notification.mail` package contains business logic — reminder scheduling rules, ZEP email processing, and clarification lifecycle notifications — that currently lives outside the hexagon and depends directly on legacy service interfaces. Migrating it brings the last major business workflow into the hexagon, eliminating the remaining cross-cutting legacy dependencies and enabling the notification concern to be tested and evolved in isolation.

## What Changes

- Introduce a new `hexagon/notification/` bounded context owning two use cases: scheduled reminder dispatch and ZEP webhook mail processing
- Migrate `BusinessDayCalculator` into the hexagon as `ReminderSchedulePolicy` — a pure domain service
- Migrate `OfficeCalendarUtil` into `hexagon/shared/domain/util/` for use across BCs
- Split the `Mail` enum: scheduling semantics become `ReminderType` in the notification domain; template resolution moves to the outbound adapter
- Replace `PubSubResourceImpl` with a proper hexagon REST adapter `ZepMailWebhookResource` inside the notification BC
- Introduce domain events on `MonthEndClarification` operations (`ClarificationCreatedEvent`, `ClarificationCompletedEvent`, `ClarificationDeletedEvent`) so that the notification BC can send lifecycle emails without `monthend` depending on mail infrastructure
- Introduce a `ZepClarificationMailReceived` integration event in `hexagon/shared/domain/event/` — fired by the notification BC when a ZEP mail is parsed, observed by the monthend BC to create a clarification
- `NotificationMailPort` lives inside the notification BC — not in shared — and is implemented by a `QuarkusMailNotificationAdapter` wrapping the existing `MailSender` and `NotificationHelper`
- Add `findByRole(Role)` to `UserRepository`

## Capabilities

### New Capabilities

- `notification-reminder-scheduling`: Domain rules for which reminder emails fire on which business days, and the use case that dispatches them to users by role
- `notification-zep-mail-processing`: Webhook-triggered ingestion of ZEP clarification emails, parsed into a `ZepClarificationMailReceived` event that the monthend BC processes into a clarification
- `notification-clarification-lifecycle`: Email notifications sent in response to clarification domain events (created, completed, deleted) — driven by the monthend BC firing domain events, observed by the notification BC

### Modified Capabilities

- `monthend-clarifications`: Gains domain events (`ClarificationCreatedEvent`, `ClarificationCompletedEvent`, `ClarificationDeletedEvent`) fired on state changes, and an inbound adapter that observes `ZepClarificationMailReceived` to create clarifications from ZEP emails

## Impact

- **Deleted / superseded**: `notification/mail/ReminderEmailSender`, `notification/mail/dates/BusinessDayCalculator`, `notification/mail/receiver/MailReceiver`, `notification/mail/receiver/ZepMailToCommentService`, `rest/impl/PubSubResourceImpl` (replaced by hexagon adapter)
- **Retained as adapter internals**: `notification/mail/MailSender`, `notification/mail/NotificationHelper`, `notification/mail/receiver/ZepProjektzeitDetailsMailMapper` — wrapped behind ports, not deleted yet
- **Extended**: `hexagon/user/domain/port/outbound/UserRepository` — gains `findByRole(Role)`
- **No REST API changes**: the `/pubsub/message-received` endpoint path and contract are preserved
- **No breaking changes to existing hexagon BCs**: monthend, worktime, project, user are all consumers of new events/ports, not changed at their public API level
