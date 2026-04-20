## Context

The notification BC business logic is complete. The remaining gap is structural: `QuarkusMailNotificationAdapter` imports `MailSender`, `NotificationHelper`, and `Mail` from the legacy `notification.mail` package, keeping a compile-time dependency that should flow the other way. Additionally, `NotificationMailPort.send()` accepts a raw `String mailTypeId`, making the contract invisible to the compiler. Separately, `UpdateMonthEndClarificationService.updateText()` does not fire a domain event, so the notification BC never hears about clarification text changes.

Legacy classes (`MailSender`, `NotificationHelper`, `MailParameter`, `Mail`) are retained in the codebase for use by legacy code until a future removal pass — they must not be deleted in this change.

## Goals / Non-Goals

**Goals:**
- Sever all hexagon → `legacy.notification.mail` compile-time dependencies
- Make `NotificationMailPort` type-safe: callers pass enum values, not strings
- Close the `ClarificationUpdatedEvent` gap so clarification-updated notifications are sent
- Rename email templates and properties keys to reflect hexagon domain vocabulary

**Non-Goals:**
- Deleting legacy classes (`MailSender`, `NotificationHelper`, `Mail`, `MailParameter`)
- Changing email template content or visual design
- Adding new notification types beyond what is described here
- Introducing localized template variants (the locale fallback mechanism is preserved)

## Decisions

### 1. Sealed interface `MailNotificationId` with enum implementations

`MailNotificationId` is a sealed interface in `notification/domain/model/` that permits exactly two implementations: `ReminderType` and `ClarificationNotificationType`. Both are enums that implement it.

**Why sealed over alternatives:**
- A plain marker interface would allow any class to implement it, defeating type safety
- A single unified enum would conflate scheduling concerns (day offset, target role) with notification type concerns
- The sealed interface gives the adapter exhaustive pattern matching: `instanceof ReminderType` vs `instanceof ClarificationNotificationType` determines the render strategy at compile time, with no unchecked branches

`MailNotificationId` declares `String name()` — implemented for free by both enums — which drives the convention-based template and subject resolution.

### 2. Convention-based template resolution

Template path: `emails/{id.name()}.html`
Subject key: `mail.{id.name()}.subject`

No enum fields, no mapping tables, no registry. The template filename IS the type identity. This is already how the legacy `Mail` enum worked implicitly (every enum value had a matching file by name) — we just make it explicit and drop the `getTemplate()` infrastructure field.

### 3. Two-pass render retained for reminders

Reminder templates (e.g. `EMPLOYEE_CHECK_PROJECTTIME.html`) are HTML snippets — just a bullet list. They render inside `reminder-template.html` which supplies the greeting, MEGA-Dash link, and logo.

**Why not flatten all reminder templates to be self-contained?**
Flattening would require duplicating ~8 lines of wrapper HTML into each of 6 reminder templates. The two-pass render preserves a single source of truth for the shared boilerplate. The dispatch rule inside the adapter is one `instanceof` check — not complex enough to justify the template duplication.

The adapter determines render strategy by type:
- `instanceof ReminderType` → load snippet, inject as `$mailText$` into `reminder-template.html`, render result
- `instanceof ClarificationNotificationType` → load template directly, render in one pass

### 4. `QuarkusMailNotificationAdapter` absorbs MailSender / NotificationHelper

Rather than moving the legacy classes into the hexagon under new names, their logic is absorbed directly into `QuarkusMailNotificationAdapter`. Both classes are small (~50-60 lines of logic each) and their separation was an artifact of the legacy layering — `NotificationHelper` even uses `MailSender.class.getClassLoader()` to load resources, an accidental coupling that has no reason to survive.

The adapter becomes self-contained: it loads templates, resolves subjects, performs parameter substitution, and dispatches via Quarkus `Mailer`. No other hexagon class needs this logic.

### 5. Template and properties key renames

Mail type identifiers in the hexagon domain are `CLARIFICATION_*` (not `COMMENT_*`). Since template filenames and properties keys are derived by convention from `id.name()`, the resource files must be renamed to match. These are internal classpath resources — they are not API, not URLs, not externally visible identifiers.

| Old name | New name |
|---|---|
| `COMMENT_CREATED.html` | `CLARIFICATION_CREATED.html` |
| `COMMENT_MODIFIED.html` | `CLARIFICATION_UPDATED.html` |
| `COMMENT_CLOSED.html` | `CLARIFICATION_COMPLETED.html` |
| `COMMENT_DELETED.html` | `CLARIFICATION_DELETED.html` |
| `ZEP_CLARIFICATION_PROCESSING_ERROR.html` | `ZEP_PROCESSING_ERROR.html` |

Properties keys follow the same pattern (`mail.COMMENT_CREATED.subject` → `mail.CLARIFICATION_CREATED.subject`).

### 6. `ClarificationUpdatedEvent` fired from `UpdateMonthEndClarificationService`

The event carries `clarificationId`, `actorId` (the editor — enforced by `canEditText()` to always be the original creator), `subjectEmployeeId`, and `text`. The `ClarificationLifecycleNotificationAdapter` observes it and sends a `CLARIFICATION_UPDATED` mail to `subjectEmployeeId`.

This mirrors the shape of the other clarification events and closes the only remaining notification gap.

## Risks / Trade-offs

| Risk | Mitigation |
|---|---|
| Template renames cause runtime failures if a filename is misspelled | Existing `@QuarkusTest` integration tests exercise mail sending; a missing classpath resource throws `IllegalStateException` immediately |
| Breaking `NotificationMailPort` signature requires touching all callers atomically | Compiler enforces this — build fails until all callers are updated; no runtime surprise possible |
| Absorbing `MailSender` logic introduces rendering regression | The rendering logic is straightforward string replacement; behavior is covered by existing notification integration tests |
| `ClarificationUpdatedEvent` fires in the same transaction as `save()` — CDI synchronous observer could roll back the transaction on error | Matches the pattern of all other clarification events; notification failures should not roll back business operations — observers should catch and log exceptions rather than propagate |

## Migration Plan

1. Rename template files and properties keys
2. Introduce `MailNotificationId`, `ClarificationNotificationType`, `ClarificationUpdatedEvent`
3. Update `NotificationMailPort` signature and all callers in one commit
4. Rewrite `QuarkusMailNotificationAdapter` — no legacy imports
5. Wire `ClarificationUpdatedEvent` in `UpdateMonthEndClarificationService`
6. Handle `ClarificationUpdatedEvent` in `ClarificationLifecycleNotificationAdapter`

No data migration required. Legacy classes remain compilable and in use by legacy code throughout.

**Rollback**: revert the commit set. Legacy classes are untouched, so rolling back restores the prior state cleanly.
