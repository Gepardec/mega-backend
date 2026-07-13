## 1. Dependency verification

- [x] 1.1 Check `pom.xml` for whether the mailer's Qute template support (typed `MailTemplate` / `@CheckedTemplate` rendering) is already available transitively via `quarkus-mailer`; run a quick build/inject smoke check.
- [x] 1.2 If the typed mail-template support is not resolvable, add the required dependency (e.g. `quarkus-qute`) to `pom.xml` and confirm it resolves.

## 2. Qute template

- [x] 2.1 Create the digest Qute template under `src/main/resources/templates/` (name matching the checked-template method, e.g. `recognition-digest.html`).
- [x] 2.2 Reproduce the current body: greeting with `{recipient.firstName}` (or an equivalent parameter) and the fixed intro line ("hier ist der aktuelle Briefkasten für den Tipi-JFX Teilnehmerkreis:").
- [x] 2.3 Add the empty-state branch with `{#if}` emitting "Diese Woche wurden keine neuen Anerkennungen eingereicht." when there are no entries.
- [x] 2.4 Add the appreciation/praise section: only render when non-empty, with heading "Lob & Wertschätzung" and a `{#for}` over the appreciation entries rendering the escaped message and submitter attribution inside `<ul>`.
- [x] 2.5 Add the courage section: only render when non-empty, with heading "Mut" and a `{#for}` over the courage entries rendering the escaped message and submitter attribution inside `<ul>`.
- [x] 2.6 Add the logo `<img src="cid:LogoMEGAdash@gepardec.com"/>` markup, using `.raw` only where needed so the CID reference is not escaped.
- [x] 2.7 Confirm interpolated values (`{recipient.firstName}`, `{entry.message}`) use Qute's default HTML auto-escaping (no `.raw` on user-supplied text).

## 3. Adapter rewrite

- [x] 3.1 Declare a typed `@CheckedTemplate` (static method returning the mail template instance) for the digest template, or inject a `MailTemplate` via `@Location`; per design, prefer `@CheckedTemplate`.
- [x] 3.2 In `QuarkusRecognitionMailAdapter`, partition the incoming entries into appreciation and courage lists (adapter-side, per design) plus an empty indicator, and pass the recipient first name and these lists to the template.
- [x] 3.3 Render the body via the typed template and build the `Mail`, adding the inline logo attachment on the same mail instance with CID `<LogoMEGAdash@gepardec.com>` (mixed typed-template + imperative attachment).
- [x] 3.4 Keep the subject as `subject-prefix` + `ResourceBundle` lookup of `mail.RECOGNITION_DIGEST.subject` (Decision 4, Option A); leave the `{msg:...}` i18n migration as a noted follow-up.
- [x] 3.5 Preserve the existing `Log.info` on successful send; update `RecognitionMailPort.sendDigest(...)` to accept the display-ready digest entry projection.

## 4. Cleanup

- [x] 4.1 Delete `escapeHtml`, `renderEntries`, `renderCategoryEntries`, the raw-template `readTemplate` method, and the associated constants (`TEMPLATE_PATH`, `FIRST_NAME_PARAMETER`, `ENTRIES_PARAMETER`, `EMPTY_STATE`) from the adapter.
- [x] 4.2 Remove the now-unused imports in this adapter (Guava `HtmlEscapers`, `IOUtils`/`InputStream` for template reading, `ResourceBundle`-related helpers only if fully unused).
- [x] 4.3 Retire the raw `src/main/resources/emails/recognition-digest.html` resource.
- [x] 4.4 Confirm the logo resource read path is retained (still needed for the inline attachment).

## 5. Tests

- [x] 5.1 Run existing recognition digest adapter/behavior tests (mailer mock in the test profile) and update assertions to the equivalent rendered output.
- [x] 5.2 Add/verify a test for the empty-state body (no entries → empty-state paragraph present).
- [x] 5.3 Add/verify tests for single-category and both-category rendering (correct headings and entry messages).
- [x] 5.4 Add a test asserting that an entry message containing markup-significant characters is auto-escaped in the rendered body (escaping behavior).
- [x] 5.5 Add/verify a test asserting the inline logo attachment (CID `<LogoMEGAdash@gepardec.com>`) is present on the sent mail and the body references `cid:LogoMEGAdash@gepardec.com`.
- [x] 5.6 Run the full build/tests to confirm no regression.

## 6. Submitter attribution

- [x] 6.1 Add a display-ready recognition digest entry projection and a recognition-owned submitter-directory port/adapter that maps user IDs to display names.
- [x] 6.2 Batch-resolve non-null `submittedBy` values in `RecognitionDigestService`; use the literal `Anonym` when a recognition has no recorded submitter.
- [x] 6.3 Pass the projection through the mail port and render `{entry.submitterName}` alongside every Qute entry message.
- [x] 6.4 Add/verify digest-service and mail-adapter tests for named submitters and anonymous entries.
