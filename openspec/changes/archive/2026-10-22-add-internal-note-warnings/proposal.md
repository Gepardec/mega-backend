# Proposal

## Why

Since 2026-10-01 a booking convention applies: internal bookings start their note with "Intern - ". This makes it easier to tell internal and billable bookings apart during manual review. The convention only helps if note and billability match. MEGA should therefore report deviations in both directions as worktime warnings before the month is closed.

## What Changes

- Detailed bookings additionally carry their billability and their note from ZEP. Both apply to project and journey bookings.
- New warning type `INTERNAL_NOTE_BILLABLE`: a billable booking has a note that is marked as internal.
- New warning type `INTERNAL_NOTE_MISSING`: a non-billable booking has no note marked as internal. This also applies when the note is empty.
- Detection of the marker is tolerant: leading spaces and upper/lower case do not matter. "Intern" is followed by a hyphen or an en dash, with optional spaces before and after it. The marker only counts at the start of the note.
- Both rules only check bookings from 2026-10-01 onward, the date the convention was introduced.
- The warning-type vocabulary is extended beyond the legacy types. The existing legacy identifiers remain unchanged.

## Capabilities

### New Capabilities
- `worktime-warning-internal-note`: Checks the "Intern - " booking convention against a booking's billability (both directions, marker detection, cutoff date).

### Modified Capabilities
- `worktime-bookings`: Detailed bookings carry billability and note. The ZEP mapping gets defaults for missing values.
- `worktime-warnings`: The warning-type vocabulary is extended with `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING`. Legacy identifiers are preserved; new types do not need a legacy counterpart.
- `worktime-rest-api`: The `type` field of the warnings endpoint returns an identifier from the warning-type vocabulary, no longer exclusively a legacy identifier.

## Impact

- **Backend (worktime BC)**: ZEP attendance DTO (read the note), booking model and ZEP mapper, new warning calculator, registration in the warning assembler, warning-type enum.
- **REST API**: No structural change. `GET /worktime/warnings/{payrollMonth}` can return two new `type` values.
- **Frontend**: Needs translation keys for `INTERNAL_NOTE_BILLABLE` and `INTERNAL_NOTE_MISSING`. The warning texts live in the frontend; implementing them there is not part of this change.
- **Business**: From October 2026, every non-billable booking without the marker produces a warning, including journeys and bookings without a note.
