## Context

The `notification.mail` package is the last major slice of business logic outside the hexagon. It contains three distinct workflows:

- **Workflow A** — Quartz-scheduled reminder emails dispatched to users by role on computed business days
- **Workflow B** — ZEP clarification emails received via a Google Cloud Pub/Sub webhook, parsed and published as `ZepClarificationMailReceived` events, converted into month-end clarifications by the monthend BC
- **Workflow C** — Transactional lifecycle emails sent when month-end clarifications are created, completed, or deleted (currently in `CommentServiceImpl`)

All three workflows share one infrastructure concern: sending HTML template emails via Quarkus Mailer. The current `MailSender` + `NotificationHelper` pair handles this and is reused across all three. `PubSubResourceImpl` in the REST layer directly invokes `MailReceiver`, bypassing the hexagon entirely.

Constraints from existing specs:
- `hexagon-layer-constraints`: domain must not import infrastructure; inbound and outbound adapters must not depend on each other
- `hexagon-boundary-conventions`: shared kernel limited to stable cross-cutting concepts; no domain-specific copies of shared types
- `monthend-clarifications`: `MonthEndClarification` is a hexagon aggregate; new clarifications must go through its creation use case

## Goals / Non-Goals

**Goals:**
- Introduce `hexagon/notification/` as a bounded context owning Workflows A and B
- Express Workflow C as domain events fired by the monthend BC, observed by a notification inbound adapter — no mail dependency in `monthend`
- Preserve the `/pubsub/message-received` endpoint contract while replacing the legacy implementation
- Keep `MailSender` and `NotificationHelper` alive as adapter internals during this migration (no re-implementation)
- Move `OfficeCalendarUtil` into `hexagon/shared/domain/util/` for future reuse across BCs
- Add `findByRole(Role)` to the existing `UserRepository` port

**Non-Goals:**
- Deleting `MailSender`, `NotificationHelper`, or the legacy `notification.mail` package (deferred to cleanup)
- Migrating email template files or i18n bundles
- Introducing asynchronous event delivery (CDI `@Observes` synchronous events are sufficient)
- Adding new mail types or changing notification business rules

## Decisions

### D1: notification as its own bounded context

**Decision:** Introduce `hexagon/notification/` as a first-class BC.

**Rationale:** `ReminderSchedulePolicy` (from `BusinessDayCalculator`) encodes genuine business rules — which reminder fires on which business day — that belong in a domain layer, not in infrastructure. Workflow B also has meaningful orchestration logic. This is not generic infrastructure; it is a supporting subdomain.

**Alternative considered:** Fold notification sending into each consuming BC as a side-effect port. Rejected because reminder scheduling logic has no natural home in any other BC, and the ZEP webhook processing spans multiple BCs.

---

### D2: Workflow C via domain events, not a shared port

**Decision:** `monthend` use cases fire domain events (`ClarificationCreatedEvent`, `ClarificationCompletedEvent`, `ClarificationDeletedEvent`). A notification inbound adapter observes them via CDI `@Observes` and sends the appropriate mail.

**Rationale:** This keeps `monthend` completely free of any notification dependency. The monthend BC models what happened; the notification BC decides what to communicate. Consistent with hexagonal principle that outbound ports are defined by the consumer — and here the consumer (notification) is a passive observer, so no port in monthend is needed at all.

**Alternative considered:** A `ClarificationNotificationPort` defined in `monthend`, implemented by the notification adapter. Rejected because it introduces an explicit outbound dependency in `monthend` that the domain event approach avoids entirely.

---

### D3: Workflow B via integration event in shared

**Decision:** `ProcessZepMailService` fires `ZepClarificationMailReceived` (defined in `hexagon/shared/domain/event/`). A monthend inbound adapter observes it and calls `CreateMonthEndClarificationUseCase`.

**Rationale:** Workflow B's event crosses BC boundaries in both directions: notification fires it, monthend consumes it. If the event class lived in `notification/domain/event/`, monthend would depend on notification. Combined with notification depending on monthend (via Workflow C's domain events), the result would be a circular package dependency. Placing the event in `shared/` — following the same pattern as other shared kernel types (`UserId`, `Email`) — breaks the cycle. The event is also genuinely co-owned: neither BC has exclusive claim to the concept "a ZEP clarification mail was received."

**Alternative considered:** Direct outbound port `CreateClarificationFromZepMailPort` in the notification BC, implemented by a monthend adapter. Rejected because it couples the notification BC's application layer to monthend's domain model (clarification types), which the event approach avoids.

---

### D4: NotificationMailPort is internal to the notification BC

**Decision:** `NotificationMailPort` is defined in `notification/domain/port/outbound/` and is not placed in `shared/`.

**Rationale:** Only the notification BC's own use cases call it directly. Workflow C is driven by events — the notification adapter that observes those events is inside the notification BC and has natural access to its own port. A shared port would be a shared kernel anti-pattern: coupling without a genuine shared owner.

**Alternative considered:** `shared/domain/port/outbound/NotificationMailPort` consumed by all BCs. Rejected per hexagonal principle — outbound ports are owned by the consumer, and the only direct consumer is the notification BC itself.

---

### D5: Mail enum split

**Decision:** Extract `ReminderType` (domain enum with day offset and schedule type) from the existing `Mail` enum. Template path resolution moves entirely to the adapter.

**Rationale:** The `Mail` enum currently mixes business rules (when does this reminder fire?) with infrastructure hints (which HTML template file to use?). Domain must not encode file paths. The adapter maps `ReminderType → template path` at send time.

The manual/event-driven mail types (`COMMENT_CREATED`, `COMMENT_CLOSED`, etc.) are not needed as domain types in the hexagon — the notification adapter for Workflow C selects the correct template based on which domain event it observes.

---

### D6: UserRepository used directly, extended with findByRole

**Decision:** `SendScheduledRemindersService` injects `UserRepository` directly. `findByRole(Role)` is added to the `UserRepository` port.

**Rationale:** `AuthenticatedActorContext` already sets the precedent for cross-BC direct use of `UserRepository`. A dedicated `UserRoleLookupPort` for notification would be indirection without value. The `user` BC is a foundational upstream BC; downstream BCs depending on its repository port is an intentional Customer-Supplier relationship.

## Risks / Trade-offs

**Synchronous CDI events mean observer failures propagate to the publisher**
→ `ProcessZepMailService` already wraps processing in a try/catch for error email reporting. The monthend observer throwing will be caught there and trigger the error notification to the ZEP mail creator. This is acceptable and mirrors the current behaviour of `ZepMailToCommentService`.

**ClarificationCreatedEvent fired on ZEP-sourced creations would trigger a mail**
→ Currently `CommentServiceImpl` does NOT send `COMMENT_CREATED` mail for ZEP-source comments. The Workflow C notification adapter must replicate this: observe `ClarificationCreatedEvent` but suppress the mail if the clarification originates from ZEP. The event should carry a `sourceSystem` field for this purpose.

**MailSender and NotificationHelper are retained as legacy internals**
→ `QuarkusMailNotificationAdapter` wraps them directly. This is intentional for this migration; the cleanup of the legacy `notification.mail` package is deferred.

**`ZepClarificationMailReceived` in shared/ grows the shared kernel**
→ Acceptable given it is a genuine integration event co-owned by two BCs. The shared kernel boundary convention (spec `hexagon-boundary-conventions`) should be updated to acknowledge integration events as a valid shared kernel resident.

## Open Questions

_All open questions resolved._

- ~~Should `ClarificationDeletedEvent` trigger a notification?~~ **Resolved:** Yes, `COMMENT_DELETED` notification is preserved in the hexagon model.
- ~~Confirm `jakarta.mail` is on the classpath.~~ **Resolved:** Guaranteed — already used by `MailReceiver`.
