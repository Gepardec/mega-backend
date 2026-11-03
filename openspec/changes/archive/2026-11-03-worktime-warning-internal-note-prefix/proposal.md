# Proposal

## Why

Bookings on internal projects must start their note with "Intern -" (Gepardec/mega#799). Today, project leads have to search ZEP manually for bookings that violate this convention — an "Intern -" prefix on a booking for a billable project points to a misbooking, and so does a missing prefix on a booking for an internal project. MEGA's booking check already derives time-booking warnings, so it should detect both cases automatically.

## What Changes

- The booking check detects two new violations of the "Intern -" convention, each reported as its own new warning type:
  - `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` — a booking on a billable project whose note carries the "Intern -" prefix.
  - `INTERNAL_PREFIX_MISSING` — a booking on an internal (non-billable) project whose note lacks the prefix, including bookings with an empty note.
- Both rules apply to project bookings and journey bookings alike.
- Whether a booking is internal is decided at **project level**: a project is internal when MEGA's project data marks it as non-billable. The per-booking billable flag is not used.
- The prefix match is tolerant: leading whitespace is ignored, "intern" is matched case-insensitively, and it may be followed by optional whitespace and a hyphen, en dash, or em dash (e.g. `Intern -`, `intern-`, `Intern –`).
- Bookings on projects that MEGA does not know cannot be classified and produce neither warning.
- Like the existing day-based warnings, each new warning carries the booking date and no hours, and appears at most once per type and day.
- Detailed bookings additionally carry the booking note and a reference to the booked project, both taken from the ZEP attendance record.

## Capabilities

### New Capabilities
- `worktime-warning-internal-note-prefix`: when bookings are flagged for violating the "Intern -" note convention, distinguishing a prefix on a billable-project booking from a missing prefix on an internal-project booking.

### Modified Capabilities
- `worktime-warnings`: the preserved warning-type vocabulary is extended by `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and `INTERNAL_PREFIX_MISSING`.
- `worktime-bookings`: detailed bookings additionally carry the booking note and the booked project's reference, mapped from the ZEP attendance record.

## Impact

- **worktime BC**: booking domain model and ZEP booking mapping (note, project reference); a new warning calculator registered in the warning assembly; the warnings use case now also resolves project billability for the booked projects.
- **project BC**: read-only — project billability is read through worktime's existing outbound project snapshot port. The shared `ProjectRef` type is not changed.
- **ZEP integration**: the attendance DTO maps the additional `note` field already returned by the ZEP REST API.
- **REST API**: `GET /worktime/warnings/{payrollMonth}` may return the two new `type` values; the response shape is unchanged.
- **Frontend** (separate repository): needs translation keys for the two new warning types.
- **Out of scope**: a dedicated project-lead view of these warnings; the warnings surface wherever employee warnings are shown today.
