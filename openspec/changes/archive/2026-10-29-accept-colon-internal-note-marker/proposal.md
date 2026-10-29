# Proposal

## Why

Employees often mark internal bookings as "Intern: JFX" instead of "Intern - JFX". Today only a hyphen or en dash counts as the delimiter, so these notes are treated as unmarked: non-billable bookings get a false `INTERNAL_NOTE_MISSING` warning, and billable bookings marked this way slip past `INTERNAL_NOTE_BILLABLE`. Accepting the colon as a third delimiter matches how people actually write the marker.

## What Changes

- A note also counts as internally marked when "Intern" is followed by optional spaces and a colon (`:`), e.g. "Intern: JFX", "intern : JFX", "  INTERN:JFX".
- Everything else about the marker stays the same: it must be at the start of the note (after optional leading spaces), is case-insensitive, and "Intern" without any delimiter ("Intern JFX", "Internes Meeting") still does not count.
- Behavior change for existing data: notes like "Intern: JFX" that produced `INTERNAL_NOTE_MISSING` will no longer do so, and billable bookings with such notes will now produce `INTERNAL_NOTE_BILLABLE`. Warnings are computed on request, so this takes effect for every month shown after deployment.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `worktime-warning-internal-note`: the requirement "A note counts as internally marked when it starts with "Intern" and a dash" is widened to also accept a colon as delimiter.

## Impact

- Code: `InternalNoteConventionCalculator` (marker pattern) in the worktime bounded context, plus its unit test.
- No API, persistence, configuration, or frontend changes. Warning types and translation keys stay unchanged.
