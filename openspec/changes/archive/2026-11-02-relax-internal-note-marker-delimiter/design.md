# Design

## Context

`InternalNoteConventionCalculator` decides "internally marked" with a single case-insensitive pattern, `^\s*intern\s*[-–:]`, applied with `find()`. Both warning types (`INTERNAL_NOTE_BILLABLE`, `INTERNAL_NOTE_MISSING`) rely on that one check, so changing the pattern changes both. See proposal.md for motivation and specs/worktime-warning-internal-note for the accepted and rejected notes.

## Goals / Non-Goals

**Goals:**
- Change only the marker pattern. The rule settings, the billable-work and billable-project logic, and the per-day aggregation stay untouched.

**Non-Goals:**
- No list of known internal project names. Matching stays based on the word "Intern", so new "Intern-XYZ" projects work without code changes.
- No check that the note's prefix matches the booked project's actual name.

## Decisions

### Pattern: `^\s*intern[\s\-–:]+[^\s\-–:]` (case-insensitive, `find()`)

- `^\s*` keeps the existing tolerance for leading spaces.
- `intern[\s\-–:]+` means "Intern" must be directly followed by at least one separator. This is what turns it into a standalone word ("Internes", "Internal" fail) and also rejects other followers ("Intern/Extern", "Intern.JFX").
- `[^\s\-–:]` requires one non-separator character after the separators, so a bare "Intern", "Intern -" or "Intern: " does not count.

Alternatives considered:
- `^\s*intern\b` (the colleague's original idea): `\b` also matches before `/`, `.`, `,`, so "Intern/Extern abstimmen" would count. Rejected.
- Whitespace-only follower `^\s*intern\s+\S`: rejects "Intern-Jagdthemen", "Intern-Weiterbildungen", "INTERN-JFX" and "Intern:JFX", which are accepted today or are real project names. Rejected.
- Allowlist of the four project names: brittle, and it still wouldn't cover free forms like "Intern JFX". Rejected.

### Unicode case folding not needed

`CASE_INSENSITIVE` without `UNICODE_CASE` is enough, because "intern" is pure ASCII. The en dash stays a literal in the character class, as it is today.

## Risks / Trade-offs

- [A note that starts with "Intern" used as an adverb (e.g. "Intern abgestimmt mit Kunde") on a billable booking now produces `INTERNAL_NOTE_BILLABLE`] → Acceptable. The word itself signals internal work, and the warning only prompts a manual check.
- [Notes that are only "Intern -" / "Intern:" stop counting as marked, so they may now produce `INTERNAL_NOTE_MISSING`] → Intended. The warning points the employee to the missing description.
- [Warnings for past months change after deployment, because they are computed on request] → Same behavior as earlier marker changes. Nothing is persisted, so nothing needs migrating, and rollback means reverting the pattern.
