## Context

The hexagon codebase already hosts several bounded contexts (`user`, `project`, `worktime`, `monthend`, `notification`, `shared`). This change adds a new `recognition` context for a company-wide "Briefkasten": employees submit short notes about praiseworthy or brave deeds of colleagues, and the leadership circle (Tipi-JFX Teilnehmerkreis) receives a weekly digest to take entries over into the JFX.

Two existing patterns are directly relevant and are followed here:

- **Cross-BC user access** — `project`'s `UserIdentityLookupPort` / `UserIdentityLookupAdapter`: the consuming BC declares its own outbound port, and an adapter in that BC reaches into the `user` BC's persistence. The recognition domain never depends on the `user` aggregate.
- **Scheduled mail** — `notification`'s `ReminderEmailScheduler` (`@Scheduled` cron) → `SendScheduledRemindersService` (use case) → `QuarkusMailNotificationAdapter` (Quarkus `Mailer`, HTML templates under `emails/`, inline logo, `mega.mail.subject-prefix`, ResourceBundle subjects).

## Goals / Non-Goals

**Goals:**
- Let any authenticated `EMPLOYEE` submit a free-text recognition entry tagged as praise (Lob/Wertschätzung) or courage (Mut).
- Send a weekly digest of not-yet-included entries to every internal project lead active on the run date.
- Keep the recognition domain and application layers isolated from the `user` aggregate.
- Reuse existing mail infrastructure without modifying the `notification` BC.

**Non-Goals:**
- No editing, deletion, archiving, or moderation of entries.
- No UI work (backend only; a REST submit endpoint is provided).
- No changes to the `user`, `notification`, or `shared` spec-level behavior.
- No structured reference to the praised colleague or the submitter — the entry is free text.

## Decisions

### 1. Separate `recognition` bounded context (not part of `user`)
Recognition has its own ubiquitous language (entry, Briefkasten, digest, JFX, praise/courage) and its own reason to change (the JFX culture process), distinct from the `user` BC's identity/employment/role/sync concerns. "Every user is an employee" is data gravity, not domain cohesion — by that logic `worktime`, `monthend`, and `notification` would all collapse into `user`, which the codebase deliberately avoids. Recognition is a downstream **customer** of `user` (supplier). *Alternative considered:* placing the feature in `user` — rejected because it would entangle an unrelated aggregate, table, endpoint, and cron into the identity context.

### 2. `RecognitionEntry` aggregate with a lifecycle flag
Fields: identity, free-text `message`, `RecognitionCategory` (`APPRECIATION` | `COURAGE`), submission timestamp, and `status` (`NEW` → `INCLUDED_IN_DIGEST`). The status flag (rather than a "last digest sent" watermark) makes the weekly run idempotent and missed-run-safe: a skipped week simply leaves more `NEW` entries for the next run. Entries are immutable after creation and are never deleted. *Alternative considered:* time-window watermark — rejected for weaker guarantees on missed/partial runs.

### 3. Recipient resolution via a recognition-owned outbound port (clean isolation)
Recognition declares `ProjectLeadDirectoryPort` (outbound) returning the mail recipients that are internal project leads active on a given `LocalDate`. The adapter lives in `recognition/adapter/outbound`, reuses the `user` BC's `UserRepository.findByRole(PROJECT_LEAD)`, and filters `isActiveOn(date) && !isExternal()`, mapping each match to a recognition mail-recipient value. This mirrors `project`'s `UserIdentityLookupPort` and keeps recognition's domain/application layers free of `User`. *Alternative considered:* injecting `UserRepository` directly into the digest application service (as `notification` does) — rejected in favor of the stricter, more isolated port pattern.

### 4. Recipient type is a mail-recipient value, not a user projection
The digest needs an email address and a salutation name. The shared-kernel `UserRef` (`{id, fullName, zepUsername}`) carries no email, and the shared-kernel rule forbids non-`user` modules from declaring their own **identity** projection. The recognition recipient carries only mail-oriented data (email + first name) — a contact concern, not user identity — so it does not conflict with `shared-user-project-refs`, and no `UserRef` is threaded through the recognition domain.

### 5. Reuse mail infrastructure behind a recognition-owned mail port
`notification`'s `NotificationMailPort` is typed to a **sealed** `MailNotificationId permits ReminderType, ClarificationNotificationType`, with `instanceof` dispatch that throws for unknown types. Routing recognition mail through it would force recognition's mail concept into the `notification` domain (wrong-way coupling) and doesn't fit the digest's list-of-entries shape. Instead, recognition declares its own `RecognitionMailPort` (its own language: send a digest to a recipient) and its adapter reuses the underlying Quarkus `Mailer`, subject-prefix config, inline logo, and an HTML template under `emails/`. If real duplication emerges, a small shared mail helper can be extracted later — deferred, not committed. *Alternative considered:* extend `MailNotificationId` and reuse `NotificationMailPort` — rejected as wrong-way coupling and scope creep.

### 6. Weekly cron inside the recognition BC
An inbound `@Scheduled` adapter in `recognition/adapter/inbound` fires every Monday at 17:00 — the "Redaktionsschluss" (editorial deadline) — and triggers the digest use case, mirroring `ReminderEmailScheduler`. The cron expression is `0 0 17 ? * MON`. The scheduler stays thin: it does not compute the current date itself; the application service derives the reference date from the injected `Clock` (see decision 9). The recipient-resolution outbound port still takes an explicit `LocalDate`, so adapter filtering is testable with arbitrary dates. The schedule lives in the BC that owns the behavior rather than adding a new `MailScheduleType` to `notification`.

### 7. Always send the digest, including an empty state
The weekly mail is sent to all resolved recipients even when there are no new entries, with an explicit empty-state body. A silent week could be read as a broken system; an "no new entries this week" mail is a reliable liveness signal. When entries exist, they are dispatched and then transitioned to `INCLUDED_IN_DIGEST`; when none exist, no state changes.

### 8. Submit endpoint requires an internal `EMPLOYEE`
The submission REST endpoint is protected: the caller must be authenticated and hold the `EMPLOYEE` role, consistent with existing employee-facing endpoints in the codebase. Beyond the role check, external employees are forbidden — only internal employees may submit — so the resource additionally rejects callers where `isExternal()` is true.

### 9. Time is sourced from an injected `Clock`
Both time-dependent operations SHALL obtain the current time from an injected `java.time.Clock` rather than calling the no-argument `now()` methods, matching the established application-service pattern in the codebase (`monthend`, `user` services use `YearMonth.now(clock)`). Specifically: the digest application service derives its reference date via `LocalDate.now(clock)`, and the submit application service stamps `submittedAt` via the clock (e.g. `Instant.now(clock)` / `LocalDateTime.now(clock)`). This keeps clock handling consistent across the codebase and makes both flows deterministic under test by injecting a fixed `Clock` — superior to the bare `LocalDate.now()` still used in the existing scheduler adapters. *Alternative considered:* computing `LocalDate.now()` in the scheduler adapter (as `ReminderEmailScheduler` does) — rejected as non-deterministic and inconsistent with the service-layer clock convention.

## Risks / Trade-offs

- **Adapter reads `User` domain logic** → recognition's outbound adapter depends on the `user` BC's repository and aggregate. This is intentional and confined to the adapter layer; the recognition domain/application stays isolated (same trade-off `project` already accepts).
- **Filter logic (`role + active + internal`) is BC-local** → mild duplication with `notification`'s `role + active` filtering. Accepted: each BC owns its own recipient rule; extracting a shared query would over-couple the contexts for little gain.
- **Digest sent per recipient with the same content** → if recipient count is large, many near-identical mails are sent. Acceptable at the JFX circle's scale; mirrors the existing reminder dispatch.
- **Partial send failure mid-digest** → if a mail send throws after some recipients are served, entries may or may not be marked included depending on transaction boundary. Mitigation: mark entries `INCLUDED_IN_DIGEST` only after the send loop completes within the use case's transaction, so a failed run leaves entries `NEW` for retry next week.
- **Free-text content is unmoderated** → no filtering of inappropriate content. Accepted per scope; leadership reviews entries in the JFX.
