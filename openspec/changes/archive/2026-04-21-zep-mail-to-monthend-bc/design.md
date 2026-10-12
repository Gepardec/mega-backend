## Context

The ZEP mail processing pipeline was placed in the notification BC during initial hexagonalisation because it involves email. On inspection the pipeline has nothing to do with notification: it reads emails that ZEP uses as a transport to push time-entry clarification data, parses them into domain objects, and terminates by calling `CreateMonthEndClarificationUseCase`. The notification BC is purely for outbound communication (MEGA → users); the ZEP mailbox is inbound integration (ZEP → MEGA). The wrong BC assignment introduced an integration event in `shared/` (`ZepClarificationMailReceived`) whose sole purpose is to bridge across the incorrect boundary, and a cross-BC dependency where `ProcessZepMailService` calls `NotificationMailPort` directly for error notifications.

## Goals / Non-Goals

**Goals:**
- Relocate all ZEP mail processing artefacts to `hexagon/monthend/`
- Eliminate the `ZepClarificationMailReceived` shared integration event
- Eliminate the use-case-calling-use-case pattern (`ProcessZepMailService` → `ZepClarificationMailAdapter` → `CreateMonthEndClarificationUseCase`)
- Replace the direct `NotificationMailPort` dependency with a domain event for the error path

**Non-Goals:**
- Changing the `/pubsub/message-received` REST contract or Pub/Sub wiring
- Changing the ZEP mail parsing logic or the HTML table format it understands
- Adding new capabilities (e.g., retry, dead-letter queue, marking mails as read on failure)

## Decisions

### Decision 1: Merge ProcessZepMailService + ZepClarificationMailAdapter into one application service

**Chosen:** Single `CreateClarificationFromZepMailService` in `monthend/application/` implementing `CreateClarificationFromZepMailUseCase`. It reads from IMAP, parses, and calls the monthend domain/repositories directly — the same pattern used by `CreateMonthEndClarificationService`.

**Alternative considered:** Keep both services and preserve the CDI event as an intra-BC mechanism. Rejected: within the same BC the event is ceremony with no benefit; it complicates tracing and makes the transaction boundary harder to reason about.

**Why no use-case-calling-use-case:** Application services are the entry points into a BC. Calling one from another couples their transaction scopes and lifecycle, blurs responsibility, and defeats the purpose of explicit use-case interfaces. Direct domain/repository calls are the correct approach when two application services need to share logic — push the shared logic down to the domain.

### Decision 2: ZepMailMessageParser remains a pure domain service, relocated to monthend domain

**Chosen:** `ZepMailMessageParser` stays a stateless domain service with no infrastructure dependencies. It moves to `monthend/domain/service/`. `CreateClarificationFromZepMailService` injects it directly (no port interface needed — domain services are not hidden behind ports).

**Why:** The parser contains business logic for interpreting ZEP's HTML table format. It belongs in the domain, not the adapter layer. Its purity (no I/O) means no port abstraction is necessary.

### Decision 3: ZEP mail models (ZepRawMail, ZepProjektzeitEntry, ZepMailParseResult) relocate to monthend domain

**Chosen:** Moved to `monthend/domain/model/`. These are domain value types, not adapter DTOs. `ZepRawMail` is the raw input from the IMAP adapter; `ZepProjektzeitEntry` is the parsed ZEP structure; `ZepMailParseResult` is the parser output.

**Note on ACL:** `ZepMailMessageParser` acts as an Anti-Corruption Layer: it translates ZEP's email format (external model) into `ZepProjektzeitEntry` (internal representation). `CreateClarificationFromZepMailService` then maps `ZepProjektzeitEntry` fields onto a `CreateMonthEndClarificationUseCase` call — shielding the rest of the monthend domain from ZEP's naming conventions (e.g. `zepIdErsteller`).

### Decision 4: Error path uses a new ZepMailProcessingFailedEvent domain event

**Chosen:** When mail processing fails (parse failure or downstream exception), `CreateClarificationFromZepMailService` fires a `ZepMailProcessingFailedEvent` carrying the creator `UserId`, creator email, error message, and raw mail content. The notification BC observes this event and sends the `ZEP_CLARIFICATION_PROCESSING_ERROR` mail.

**Alternative considered:** Keep a direct `NotificationMailPort` dependency in the monthend application service. Rejected: it would make monthend depend on the notification BC's port, coupling two BCs. Domain events are the canonical cross-BC communication mechanism here, consistent with all other monthend→notification flows (`ClarificationCreatedEvent`, etc.).

**Where the event is defined:** `monthend/domain/event/` — alongside the other four clarification domain events.

### Decision 5: Delete ZepClarificationMailReceived from shared

`ZepClarificationMailReceived` was placed in `shared/` because it crossed BC boundaries. Once both sides live in monthend, the event has no cross-BC consumers and is replaced by a direct domain call within `CreateClarificationFromZepMailService`. The shared package shrinks, which is always desirable (shared kernels create coupling).

## Risks / Trade-offs

- **CDI bean resolution breaks at startup** if any class still imports the old `notification` package paths for the moved types → Mitigation: compile-fail; a full `mvn clean package` after the move will surface all stale references.
- **Test coverage gap** if `ProcessZepMailServiceTest` is not migrated and renamed alongside the service → Mitigation: treat test migration as part of the same task as service migration; fail CI if coverage drops.
- **ZepClarificationMailAdapter deletion removes an observer** for `ZepClarificationMailReceived` — if `ZepClarificationMailReceived` is deleted first, the CDI event has no observer and calls would silently do nothing during any partial-migration state → Mitigation: delete the old event and its observer in the same commit as the new service is wired up.

## Migration Plan

1. Create new artefacts in `monthend/` (new package, no deletions yet) — system has both old and new in parallel, new is not yet wired.
2. Wire the new `ZepMailWebhookResource` and register the `ZepMailProcessingFailedEvent` observer in notification.
3. Delete old artefacts from notification and shared in one commit — eliminates the old wiring atomically.
4. Run `mvn clean package` to confirm zero compile errors and all tests pass.

No database migration, no REST contract change, no Pub/Sub reconfiguration required.
