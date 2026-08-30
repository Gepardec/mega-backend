## Why

Praiseworthy and brave everyday deeds of colleagues often stay invisible because there is no low-friction way for employees to report them to leadership. We want a company-wide "Briefkasten" (mailbox) that any employee can drop an entry into, and a weekly digest that surfaces the newest entries to the leadership circle (Tipi-JFX Teilnehmerkreis) so they can be taken over into the JFX.

## What Changes

- Introduce a new **`recognition`** bounded context (`com.gepardec.mega.hexagon.recognition`), a downstream consumer of the `user` bounded context.
- Any authenticated employee can **submit a recognition entry**: free-text message plus a category identifying it as praise/appreciation (Lob/Wertschätzung) or courage (Mut). Entries are persisted and never deleted or archived.
- A **weekly digest email** is sent to every internal project lead active on the run date. The digest lists all entries not yet included in a previous digest and is **always sent**, even when there are no new entries, so recipients get a reliable liveness signal.
- Recipient resolution — "all internal project leads active on a given `LocalDate`" — is exposed through a recognition-owned outbound port, keeping the recognition domain isolated from the `user` aggregate.
- The digest reuses the existing mail-sending infrastructure (Quarkus `Mailer`, subject-prefix config, inline logo, HTML templates under `emails/`) behind the recognition BC's own outbound mail port; the `notification` bounded context is left untouched.

## Capabilities

### New Capabilities
- `recognition-entry`: The `RecognitionEntry` aggregate — its fields (free-text message, category, submission timestamp), the `APPRECIATION`/`COURAGE` category, the `NEW → INCLUDED_IN_DIGEST` lifecycle, and the submission behavior. Entries are immutable and never removed.
- `recognition-rest-api`: The employee-facing REST endpoint for submitting a recognition entry, protected so that only an authenticated internal employee holding the `EMPLOYEE` role may submit (external employees are forbidden).
- `recognition-weekly-digest`: The weekly scheduled digest — the cron-triggered use case, resolution of active internal project leads for a given date via the outbound directory port, always-send behavior (including the empty-state mail), transitioning included entries to `INCLUDED_IN_DIGEST`, and dispatch via the recognition mail port.

### Modified Capabilities
<!-- None. The change reuses existing user-BC persistence and mail infrastructure without altering their spec-level behavior: UserRepository.findByRole already exists, the notification BC is not modified, and the digest recipient is a mail-recipient value (email + first name) rather than a user-identity projection, so shared-user-project-refs is unaffected. -->

## Impact

- **New package**: `com.gepardec.mega.hexagon.recognition` (domain, application, adapter layers).
- **Persistence**: new `recognition_entry` table introduced via a Liquibase changelog; new Panache repository + entity.
- **REST**: new employee-facing submit endpoint; secured with the `EMPLOYEE` role and restricted to internal employees (external employees forbidden).
- **Scheduling**: new weekly `@Scheduled` cron adapter in the recognition BC.
- **Cross-BC**: recognition's outbound directory adapter reads user data from the `user` BC's persistence (mirroring `project`'s `UserIdentityLookupAdapter`); no `user` spec behavior changes.
- **Mail**: reuses the Quarkus `Mailer` bean and existing template/logo/subject-prefix conventions; adds a new digest HTML template resource and a subject message key. The `notification` BC is not modified.
