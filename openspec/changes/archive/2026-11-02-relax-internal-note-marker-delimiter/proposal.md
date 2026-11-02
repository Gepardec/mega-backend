# Proposal

## Why

Many employees start a note with the name of the internal project they book on: "Intern", "Intern LF-Projekte", "Intern-Jagdthemen" or "Intern-Weiterbildungen". Today "Intern" may be followed only by spaces and then a delimiter (`-`, `–`, `:`). So "Intern LF-Projekte - MEGA" and "Intern JFX" count as unmarked, and non-billable bookings with these notes get a false `INTERNAL_NOTE_MISSING` warning. A note that starts with the word "Intern" is enough to show the work is internal, so the delimiter requirement goes.

## What Changes

- A note counts as internally marked when, after optional leading spaces, it starts with the word "Intern" (any case), directly followed by whitespace, a hyphen (`-`), an en dash (`–`) or a colon (`:`), with more text after that.
- Newly marked: "Intern JFX", "Intern LF-Projekte - MEGA". Still marked: every form accepted today ("Intern - JFX", "Intern: JFX", "INTERN-JFX", "Intern-Jagdthemen - Workshop", ...).
- Still not marked: "Internes Meeting", "Internal sync" ("Intern" is not a standalone word); "Intern/Extern abstimmen" ("Intern" followed by another character); a marker in the middle of the note; empty notes.
- Newly not marked: a note made up only of "Intern", optionally followed by spaces or delimiters ("Intern", "Intern -", "Intern: "). Today "Intern -" and "Intern:" count as marked.
- Behavior change for existing data: non-billable bookings with notes like "Intern JFX" or "Intern LF-Projekte - MEGA" no longer produce `INTERNAL_NOTE_MISSING`, and billable bookings with such notes now produce `INTERNAL_NOTE_BILLABLE`. Notes that are just "Intern -" or "Intern:" flip the other way. Warnings are computed on request, so this applies to every month shown after deployment.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `worktime-warning-internal-note`: the requirement "A note counts as internally marked when it starts with "Intern" and a delimiter" is replaced. "Intern" must now be a standalone word followed by whitespace or a delimiter, and further text must follow.

Reviewed `worktime-warning-billable-project` and `worktime-bookings`: they only use "Intern - JFX" as sample data, which stays marked, so no change is needed there.

## Impact

- Code: `InternalNoteConventionCalculator` (marker pattern) in the worktime bounded context, plus its unit test.
- No API, persistence, configuration or frontend changes. Warning types and translation keys stay the same.
