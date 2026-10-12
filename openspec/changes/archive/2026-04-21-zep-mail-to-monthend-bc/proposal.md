## Why

The ZEP mail processing pipeline currently lives in the notification BC solely because emails are involved, but its true purpose is to create month-end clarifications from ZEP-generated messages — a monthend concern. The boundary mismatch is exposed by the fact that the entire pipeline terminates in `CreateMonthEndClarificationUseCase.create(..., SourceSystem.ZEP)`, and by an unnecessary integration event in `shared/` that exists only to bridge an incorrect BC split.

## What Changes

- Move `ZepMailWebhookResource`, `ProcessZepMailUseCase/Service`, `ImapZepMailboxAdapter`, `ZepMailboxPort`, and all ZEP mail domain models (`ZepRawMail`, `ZepProjektzeitEntry`, `ZepMailParseResult`, `ZepMailMessageParser`) from the notification BC to the monthend BC
- Merge `ProcessZepMailService` and `ZepClarificationMailAdapter` into a single `CreateClarificationFromZepMailService` that calls domain/repositories directly — eliminating the use-case-calling-use-case smell
- Delete the `ZepClarificationMailReceived` integration event from `shared/domain/event/` — no longer needed once both sides live in the same BC
- Delete `ZepClarificationMailAdapter` from `monthend/adapter/inbound/` — merged into the new service
- Replace the direct `NotificationMailPort` call on the error path with a new `ZepMailProcessingFailedEvent` domain event observed by the notification BC — consistent with `ClarificationCreated/Updated/Completed/Deleted` events
- **BREAKING**: `ProcessZepMailUseCase` port moves package from `notification` to `monthend`; any injection points referencing the old package must be updated

## Capabilities

### New Capabilities
- `monthend-zep-mail-processing`: ZEP mail processing relocated to the monthend BC as an inbound driver for clarification creation; covers the webhook adapter, the merged use case, IMAP outbound adapter, ZEP mail domain models, and the error event

### Modified Capabilities
- `notification-zep-mail-processing`: All requirements in this spec are superseded and removed; ZEP mail processing no longer belongs to the notification BC
- `monthend-clarifications`: Add `ZepMailProcessingFailedEvent` to the domain event catalogue; document ZEP email as one of the inbound drivers for `CreateMonthEndClarificationUseCase`

## Impact

- **Deleted files**: `ZepClarificationMailReceived.java` (shared event), `ZepClarificationMailAdapter.java`, `ProcessZepMailService.java`, `ProcessZepMailUseCase.java`, `ZepMailWebhookResource.java`, `ImapZepMailboxAdapter.java`, `ZepMailboxPort.java`, `ZepRawMail.java`, `ZepMailParseResult.java`, `ZepProjektzeitEntry.java`, `ZepMailMessageParser.java` — all removed from their current locations in the notification or shared packages
- **New files**: All of the above re-created under `hexagon/monthend/` with updated package declarations and adapted logic
- **notification BC**: Loses ZEP mail processing entirely; gains an observer for `ZepMailProcessingFailedEvent` in `ClarificationLifecycleNotificationAdapter` (or a dedicated adapter)
- **shared domain**: `ZepClarificationMailReceived` removed — reduces shared kernel coupling
- **No REST contract change**: `/pubsub/message-received` endpoint path and behaviour are preserved
