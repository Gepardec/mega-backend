## REMOVED Requirements

### Requirement: ZEP mail webhook triggers the ZEP mail processing use case
**Reason**: ZEP mail processing is not a notification concern. The webhook and use case have been relocated to the monthend BC as `ZepMailWebhookResource` and `CreateClarificationFromZepMailUseCase`.
**Migration**: See `monthend-zep-mail-processing` spec. The REST contract (`/pubsub/message-received`) is unchanged.

### Requirement: ZEP mail processing use case fetches, parses, and publishes a domain event per valid message
**Reason**: The `ProcessZepMailUseCase` and `ProcessZepMailService` are replaced by `CreateClarificationFromZepMailUseCase` in the monthend BC. The `ZepClarificationMailReceived` integration event is deleted; the new service calls the monthend domain directly. Error notifications are now triggered via `ZepMailProcessingFailedEvent`.
**Migration**: See `monthend-zep-mail-processing` spec.

### Requirement: ZepClarificationMailReceived is an integration event in the shared domain
**Reason**: The event existed solely to bridge the incorrect BC split. With both producer and consumer in the monthend BC the event is unnecessary. Removing it reduces shared kernel coupling.
**Migration**: Delete `ZepClarificationMailReceived` from `shared/domain/event/` and delete `ZepClarificationMailAdapter` from `monthend/adapter/inbound/`. No replacement needed — the new use case calls domain directly.

### Requirement: ZepMailboxPort returns domain-typed raw mails; ZepMailMessageParser is a domain service
**Reason**: These artefacts are relocated to the monthend BC. The requirements are unchanged in substance; see `monthend-zep-mail-processing` spec.
**Migration**: Move all types to `monthend/domain/` with updated package declarations.

### Requirement: ZepMailboxPort abstracts IMAP inbox access
**Reason**: Relocated to monthend BC. See `monthend-zep-mail-processing` spec.
**Migration**: Move `ImapZepMailboxAdapter` to `monthend/adapter/outbound/` and `ZepMailboxPort` to `monthend/domain/port/outbound/`.
