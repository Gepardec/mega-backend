## Why

When a clarification is created, updated, or deleted, the notification always goes to the subject employee — but the subject employee can also be the creator of the clarification. In that case they receive a notification about their own action, which is nonsensical. The notification should reach the parties who need to act on the event, not the party who triggered it.

## What Changes

- The routing logic in `ClarificationLifecycleNotificationAdapter` for `CREATED`, `UPDATED`, and `DELETED` events is corrected: when the creator is the subject employee, the notification is fanned out to all eligible project leads instead; when the creator is a lead, the subject employee is notified (existing behaviour preserved).
- `ClarificationCreatedEvent`, `ClarificationUpdatedEvent`, and `ClarificationDeletedEvent` are enriched with `eligibleProjectLeadIds` so the notification adapter can route without additional I/O.
- `CreateMonthEndClarificationService`, `UpdateMonthEndClarificationService`, and `DeleteMonthEndClarificationService` are updated to include `eligibleProjectLeadIds` when firing their respective events.
- A guard is added to the `COMPLETED` handler to skip notification when `creator` is the system actor (`SystemActor.USER_ID`), preventing a `loadUser` failure if a system-created clarification is ever completed.

## Capabilities

### New Capabilities
- none

### Modified Capabilities
- `notification-clarification-lifecycle`: recipient routing rules change for `CREATED`, `UPDATED`, and `DELETED` scenarios — the recipient is now determined by who the creator is, not always the subject employee; fan-out to all eligible leads is introduced.

## Impact

- `monthend/domain/event/ClarificationCreatedEvent.java` — new field
- `monthend/domain/event/ClarificationUpdatedEvent.java` — new field
- `monthend/domain/event/ClarificationDeletedEvent.java` — new field
- `monthend/application/CreateMonthEndClarificationService.java` — pass `eligibleProjectLeadIds` when firing event
- `monthend/application/UpdateMonthEndClarificationService.java` — pass `eligibleProjectLeadIds` when firing event
- `monthend/application/DeleteMonthEndClarificationService.java` — pass `eligibleProjectLeadIds` when firing event
- `notification/adapter/inbound/ClarificationLifecycleNotificationAdapter.java` — routing logic + system-actor guard
- Tests for all changed classes
