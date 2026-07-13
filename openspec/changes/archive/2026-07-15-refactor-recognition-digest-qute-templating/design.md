## Context

The recognition bounded context sends a weekly digest ("Briefkasten") email to internal project leads. The outbound adapter `QuarkusRecognitionMailAdapter` (`src/main/java/com/gepardec/mega/hexagon/recognition/adapter/outbound/`) currently builds the mail body imperatively:

- It reads a raw HTML resource (`emails/recognition-digest.html`) via `getResourceAsStream` and substitutes `$firstName$` and `$entries$` with `String.replace`.
- It hand-rolls the entries HTML through `renderEntries` / `renderCategoryEntries`, concatenating `<li>` elements and grouping by `RecognitionCategory` (APPRECIATION → "Lob & Wertschätzung", COURAGE → "Mut"), or emitting a fixed empty-state paragraph.
- It manually HTML-escapes every user-supplied value via a helper (`escapeHtml`, delegating to Guava `HtmlEscapers`).
- It resolves the subject through `ResourceBundle` (`messages.properties`, key `mail.RECOGNITION_DIGEST.subject`), prepends the configured `mega.mail.subject-prefix`, and sends via the imperative `Mailer` / `Mail.withHtml(...)` API, adding the inline logo as a CID attachment (`<LogoMEGAdash@gepardec.com>`).

Recognition entries now retain an optional `submittedBy` user ID. A null value means the submitter chose to remain anonymous; the digest must display `Anonym` in that case and the submitter's display name otherwise.

Constraints:

- Quarkus 3 / JDK 21. `quarkus-mailer` is already a dependency; there is no explicit `quarkus-qute` dependency in `pom.xml` (Qute ships transitively with the mailer, but the mailer's typed template integration must be confirmed at build time).
- Submitter-name resolution belongs in the application layer. Digest assembly creates display-ready entries containing `message`, `category`, and `submitterName`; the mail adapter receives these values and does not query user persistence.
- Project convention: use the framework-native templating and default auto-escaping rather than bespoke helpers.

Current behavior that MUST be preserved (already covered by `recognition-weekly-digest` spec, unchanged here): always-send (including empty-state), appreciation/courage grouping, recipient resolution, weekly trigger, and post-send status transitions.

## Goals / Non-Goals

**Goals:**

- Render the digest body from a Qute mail template under `src/main/resources/templates/` instead of raw-resource reading plus manual placeholder substitution.
- Replace the manual entry loops and category grouping with template-level `{#if}` / `{#for}` constructs, including the empty-state message and the two category sections.
- Delegate HTML escaping to Qute's default auto-escaping for `{expression}`, deleting the `escapeHtml` helper; opt out with `.raw` only where markup is intentional (e.g. the logo `<img>` fragment if it lives in the template).
- Preserve the inline CID logo attachment and the subject-prefix behavior.
- Keep the observable output equivalent (same sections, same headings, same empty-state text) so existing tests and recipients see no behavior change.
- Show every entry's submitter display name when `submittedBy` is present, or the literal `Anonym` when it is absent.

**Non-Goals:**

- No change to recognition submission, `submittedBy` persistence semantics, the scheduler, recipient resolution, or entry status transitions.
- No change to the visible content/wording of the email beyond the required submitter attribution.
- No spec-level (behavioral) changes — this is an internal rendering refactor. No delta spec is produced.
- No redesign of the email's HTML/CSS styling.

## Decisions

### Decision 1: Typed mail template via the mailer's Qute integration

Use the `quarkus-mailer` + Qute integration to render the body. Two concrete shapes are available:

- A typed `@CheckedTemplate` declaring a static method returning a `MailTemplate.MailTemplateInstance` (or `TemplateInstance`), giving compile-time checking of the template name and parameters.
- A field-injected `MailTemplate` (`@Inject @Location("recognition-digest") MailTemplate template`).

**Choose the typed `@CheckedTemplate`** for compile-time safety of template name and parameter names, consistent with the DDD/hexagonal preference for explicit, checked boundaries. The template file lives at `src/main/resources/templates/<name>.html` matching the checked-template method.

*Alternative considered:* keep imperative `Mail.withHtml` and only move the HTML fragment into Qute rendered via `Template#data(...)`. Rejected in favor of the typed template for parameter safety, but see Decision 3 for why the send is still partly imperative.

### Decision 2: Template-driven grouping and escaping

Pass the recipient's first name and the entry list (or two pre-filtered lists) to the template. The template:

- Uses `{#if entries.isEmpty()}` (or an equivalent empty flag) to emit the empty-state paragraph ("Diese Woche wurden keine neuen Anerkennungen eingereicht.").
- Uses `{#for}` over entries filtered by category to render each `<li>{entry.message}</li>`, wrapped in the `<h2>`/`<ul>` section, and only renders a section when it has entries.
- Relies on Qute's default HTML escaping for `{recipient.firstName}` and `{entry.message}`, so the `escapeHtml` helper and the Guava `HtmlEscapers` usage in this adapter are removed.

Category filtering may be done either in the template (via `{#if entry.category ...}`) or by passing already-partitioned lists from the adapter. **Prefer passing partitioned data** (two lists, plus a boolean/empty indicator) from the adapter so the template stays simple and the category-to-heading mapping ("Lob & Wertschätzung", "Mut") is explicit; the template only iterates and renders headings. This keeps enum-comparison logic out of the template.

*Note:* the category headings themselves are static literal HTML text in the template and are safe; only user-supplied `message` values require escaping, which Qute does by default.

### Decision 3: Inline logo stays on the imperative Mail API (mixed approach)

Qute renders the HTML body; the inline logo is attached through the imperative `Mail` API as today (`addInlineAttachment(... "<LogoMEGAdash@gepardec.com>")`), and the template references it via `src="cid:LogoMEGAdash@gepardec.com"`. The typed template produces the body string / `MailTemplateInstance`, and the CID attachment is added on the resulting `Mail`. This deliberately mixes typed-template rendering with imperative attachment, which is the supported pattern for inline attachments and avoids re-implementing attachment handling. The `cid:` reference in the template is emitted with `.raw` if it would otherwise be escaped, or simply written as static markup.

### Decision 4: Subject source — keep ResourceBundle

The subject is currently `subject-prefix` + `ResourceBundle` lookup of `mail.RECOGNITION_DIGEST.subject` from `messages.properties`.

**Decided: keep the existing `ResourceBundle` subject lookup.** It works, is shared with the legacy notification stack, and is orthogonal to body rendering — keeping it minimizes blast radius. Only the body moves to Qute in this change.

*Alternative considered:* moving the subject to Qute's message bundle / i18n (`{msg:...}` / `@MessageBundle`). Rejected for this change — it would introduce a parallel message-bundle mechanism alongside the legacy `messages.properties` used elsewhere, risking duplication/drift for a single key. If pursued later, it only affects the subject line, not the body template; left as a possible follow-up.

### Decision 5: Resolve submitter names before rendering

`RecognitionEntry` retains a user ID rather than a display name, so the digest service derives a `RecognitionDigestEntry` rendering projection before mail dispatch. It collects all non-null `submittedBy` IDs, resolves them in one batch through a recognition-owned submitter-directory port, then supplies `message`, `category`, and `submitterName` to the mail port.

Entries with a null `submittedBy` use the literal `Anonym`. A non-null ID that cannot be resolved is treated as an integrity failure rather than silently rendered as anonymous; this prevents accidental misattribution. The Qute template only interpolates the already-resolved `submitterName` alongside each message and continues to auto-escape it.

*Alternative considered:* resolve user names inside `QuarkusRecognitionMailAdapter`. Rejected because the outbound mail adapter would then depend on user persistence and mix application-level identity lookup with rendering.

## Risks / Trade-offs

- **Qute mailer template integration not on the classpath** → Build/inject failure. Mitigation: verify at build time whether the mailer's typed-template support needs an explicit dependency (e.g. `quarkus-qute`) or is transitive via `quarkus-mailer`; add the dependency in the first task if missing (captured in tasks.md).
- **Auto-escaping changes rendered output** (e.g. an entry message that previously round-tripped through Guava escaping now escaped slightly differently, or the `cid:` reference/logo markup getting escaped) → Broken image or subtly different HTML. Mitigation: use `.raw` only for the intentional static logo markup; assert rendered output in tests, including an entry containing HTML special characters to confirm escaping.
- **Behavioral drift from "equivalent output"** (missing a section, wrong empty-state text, different heading) → Recipients see a changed email. Mitigation: keep/extend existing adapter tests asserting empty-state, single-category, and both-category rendering; compare against current output semantics.
- **Mixed typed-template + imperative attachment is easy to get subtly wrong** (body not attached to the same `Mail` that carries the inline attachment) → Missing logo or unstyled body. Mitigation: build the `Mail` from the rendered template instance and add the inline attachment to that same instance; cover with a test asserting the inline attachment CID is present.
- **Template file location/name mismatch with `@CheckedTemplate`** → Runtime template-not-found. Mitigation: place the template at the exact `templates/<name>.html` path matching the checked-template method; a rendering test catches this early.

## Migration Plan

1. Confirm/add the Qute mailer template dependency in `pom.xml`.
2. Add the Qute template under `src/main/resources/templates/` reproducing the current body (greeting, intro line, category sections or empty-state, logo `img` referencing the CID).
3. Rewrite `QuarkusRecognitionMailAdapter` to render via the typed template and attach the inline logo imperatively; delete `escapeHtml`, `renderEntries`, `renderCategoryEntries`, raw-template reading, and the Guava `HtmlEscapers` import.
4. Retire the raw `emails/recognition-digest.html` resource.
5. Add the digest display projection and a recognition-owned submitter-directory port; batch-resolve recorded submitter IDs in `RecognitionDigestService`, using `Anonym` for null IDs.
6. Render `submitterName` in each Qute entry and cover named and anonymous attribution in service and mail-adapter tests.
7. Run the digest adapter/behavior tests (mailer mock) and adjust assertions to the equivalent rendered output; add an escaping test.

Rollback: revert the adapter and template changes; the raw HTML resource and imperative rendering are restored from version control. No data or schema migration is involved, so rollback is code-only.

## Resolved Questions

- Subject i18n (Decision 4): **keep `ResourceBundle`** for the subject in this change; the Qute `{msg:...}` migration is a possible separate follow-up.
- Category partitioning: **partition adapter-side** (Decision 2) and pass pre-filtered appreciation/courage lists to the template; the template only iterates and renders headings.
- Submitter attribution (Decision 5): **resolve names application-side in one batch** and render `Anonym` only for entries with no recorded submitter.
