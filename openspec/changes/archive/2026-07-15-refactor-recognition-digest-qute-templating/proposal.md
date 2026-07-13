## Why

The recognition weekly-digest email is currently built by reading a raw HTML file and performing manual string-placeholder substitution, hand-rolling the entry list with string concatenation, and manually HTML-escaping every piece of user-supplied text. This is error-prone (a missed escape is an HTML-injection risk), hard to read, and diverges from the framework-native templating the platform already ships. Migrating the digest body to the mailer's Qute templating makes the rendering declarative, delegates HTML escaping to the template engine by default, and removes bespoke helper code. The digest must also disclose the recorded submitter for each recognition while preserving the submitter's anonymous choice.

## What Changes

- Render the digest body from a Qute mail template instead of reading a raw HTML resource and doing manual `$firstName$` / `$entries$` placeholder replacement.
- Replace the hand-rolled entry-rendering loops and category grouping with template-level conditionals and iteration (empty-state message, appreciation/praise vs. courage sections).
- Rely on the template engine's default HTML auto-escaping for interpolated values, removing the manual HTML-escaping helper entirely (opting out only where raw HTML is intentional, e.g. the pre-rendered logo markup).
- Preserve the inline logo attachment: the template renders the body while the imperative mail API still attaches the inline CID logo (`<LogoMEGAdash@gepardec.com>`); the two remain mixed.
- Keep the subject as-is (subject-prefix + `ResourceBundle` lookup); only the body moves to Qute. Migrating the subject to the template engine's message/i18n mechanism is left as a possible follow-up (see design.md, Decision 4).
- Add the mailer-template / Qute integration dependency if it is not already transitively present; remove the now-unused manual-escaping and raw-template-reading dependencies from this adapter.
- Present attribution for every digest entry: resolve the recorded `submittedBy` user ID to a display name, or render `Anonym` when `submittedBy` is absent.

## Capabilities

### New Capabilities
<!-- None. This is an implementation-only refactor; no new capability is introduced. -->

### Modified Capabilities
- `recognition-weekly-digest`: The digest retains its always-send behavior, empty-state message, appreciation/courage grouping, recipient resolution, weekly trigger, and status transitions. It now guarantees that markup-significant characters in entry messages reach recipients as literal text, and that each entry shows its recorded submitter's name or `Anonym` when no submitter was recorded. Captured as a MODIFIED delta on the "digest contains all entries" requirement in `specs/recognition-weekly-digest/spec.md`.

## Impact

- **Code**: `QuarkusRecognitionMailAdapter` (outbound adapter of the recognition bounded context) renders a display-ready digest model via a Qute mail template. `RecognitionDigestService` resolves submitter display names through a recognition-owned directory port before dispatch. The Qute template lives under `src/main/resources/templates/`; the raw `emails/recognition-digest.html` resource is retired.
- **Dependencies**: Requires the `quarkus-mailer` Qute template integration (verify whether an explicit dependency such as `quarkus-qute` is needed or whether it is already transitive via `quarkus-mailer`). The manual-escaping dependency (Guava `HtmlEscapers`) and raw-resource reading in this adapter are no longer used here.
- **Tests**: Digest service tests cover named and anonymous attribution; mail-adapter tests cover the rendered name, `Anonym`, template rendering, and auto-escaping.
- **Behavior / specs**: The `recognition-weekly-digest` delta guarantees literal rendering of markup-significant message characters and submitter attribution. Send behavior is otherwise unchanged.
