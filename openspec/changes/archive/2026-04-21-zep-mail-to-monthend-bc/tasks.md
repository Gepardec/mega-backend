## 1. Create new monthend ZEP mail domain artefacts

- [x] 1.1 Create `ZepRawMail` record in `monthend/domain/model/` (copy from notification, update package)
- [x] 1.2 Create `ZepMailParseResult` record in `monthend/domain/model/` (copy from notification, update package)
- [x] 1.3 Create `ZepProjektzeitEntry` record in `monthend/domain/model/` (copy from notification, update package)
- [x] 1.4 Create `ZepMailboxPort` outbound port in `monthend/domain/port/outbound/` (copy from notification, update package and imports)
- [x] 1.5 Create `ZepMailMessageParser` domain service in `monthend/domain/service/` (copy from notification, update package and imports to use new model classes)

## 2. Create new monthend outbound adapter

- [x] 2.1 Create `ImapZepMailboxAdapter` in `monthend/adapter/outbound/` implementing the new `ZepMailboxPort` (copy from notification, update package and imports)

## 3. Create ZepMailProcessingFailedEvent domain event

- [x] 3.1 Create `ZepMailProcessingFailedEvent` record in `monthend/domain/event/` carrying optional creator `UserId`, optional creator email, error message, and raw mail content

## 4. Create the merged application service

- [x] 4.1 Create `CreateClarificationFromZepMailUseCase` port interface in `monthend/application/port/inbound/`
- [x] 4.2 Create `CreateClarificationFromZepMailService` in `monthend/application/` implementing the use case — reads IMAP via `ZepMailboxPort`, parses via `ZepMailMessageParser`, resolves creator via `UserRepository`, and calls clarification domain/repository logic directly (same pattern as `CreateMonthEndClarificationService`)
- [x] 4.3 Fire `ZepMailProcessingFailedEvent` on parse failure and on processing exception in `CreateClarificationFromZepMailService`

## 5. Create new monthend inbound adapter (webhook)

- [x] 5.1 Create `ZepMailWebhookResource` in `monthend/adapter/inbound/rest/` calling `CreateClarificationFromZepMailUseCase` (copy from notification, update package and import)

## 6. Wire error notification in the notification BC

- [x] 6.1 Add `onZepMailProcessingFailed(@Observes ZepMailProcessingFailedEvent)` observer to `ClarificationLifecycleNotificationAdapter` (or a dedicated adapter) that sends `ZEP_CLARIFICATION_PROCESSING_ERROR` via `NotificationMailPort`

## 7. Delete old artefacts (in one commit)

- [x] 7.1 Delete `ZepClarificationMailReceived` from `shared/domain/event/`
- [x] 7.2 Delete `ZepClarificationMailAdapter` from `monthend/adapter/inbound/`
- [x] 7.3 Delete `ProcessZepMailService`, `ProcessZepMailUseCase` from `notification/application/`
- [x] 7.4 Delete `ZepMailWebhookResource` from `notification/adapter/inbound/rest/`
- [x] 7.5 Delete `ImapZepMailboxAdapter` from `notification/adapter/outbound/`
- [x] 7.6 Delete `ZepMailboxPort`, `ZepRawMail`, `ZepMailParseResult`, `ZepProjektzeitEntry` from `notification/domain/`
- [x] 7.7 Delete `ZepMailMessageParser` from `notification/domain/service/`

## 8. Migrate tests

- [x] 8.1 Migrate `ProcessZepMailServiceTest` → `CreateClarificationFromZepMailServiceTest` in monthend test package; update all imports and assertions
- [x] 8.2 Delete `ZepClarificationMailAdapterTest` (adapter no longer exists)
- [x] 8.3 Add unit test for `ZepMailProcessingFailedEvent` observer in the notification BC

## 9. Verify

- [x] 9.1 Run `mvn clean package` — zero compile errors, all tests green
- [x] 9.2 Confirm no remaining imports of old notification ZEP mail packages via `grep -r "hexagon.notification.*[Zz]ep" src/`
