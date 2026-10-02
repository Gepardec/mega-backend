# Design

## Context

The warning rules live in the worktime domain as one pure calculator per rule, assembled into a flat list. Two #827 fixes are already implemented on `feature/827` (midnight end mapping in the ZEP booking mapper, exact 22:00 boundary in the core-working-time check); the remaining behaviour changes are listed in proposal.md. Each new `worktime-warning-*` spec was derived from the current calculator code and its tests, then reviewed rule by rule; deviations from the current code were decided explicitly (see proposal.md - What Changes).

## Goals / Non-Goals

**Goals:**
- Make every warning rule verifiable against its own spec instead of against deleted legacy code.
- Bring the code in line with the decided rules with minimal, local changes.

**Non-Goals:**
- Cross-month journey pairing (stays a specified known limitation).
- Changing thresholds, warning types, the REST contract, or the frontend.
- Unifying the two ZEP attendance fetch paths.

## Decisions

### One capability per rule, named after the business rule
Capabilities are named `worktime-warning-<rule>` (e.g. `worktime-warning-core-working-time`), not after the calculator class, so the specs survive an implementation restructuring. `worktime-warnings` stays the overall spec for the use case, warning shape, vocabulary, assembly and cross-rule constraints.
*Alternative:* one `worktime-warning-rules` capability with a requirement per rule — rejected because rules evolve independently and per-rule deltas stay small and focused.

### De-duplicate per type and date once, in the assembly
The "at most one warning per type and date" rule is enforced when assembling calculator outputs, keeping the first occurrence and the existing order. Rules that already emit one warning per day remain correct; per-booking rules (working location, doctor appointment, journey pairing) no longer produce duplicates. Quantitative warnings are already unique per type and date, so no hours values are lost; the month-level `EMPTY_ENTRY_LIST` (no date) is unaffected.
*Alternative:* de-duplicate inside each affected calculator — rejected as three copies of the same concern that a future rule could forget.

### Doctor-appointment windows compared on full timestamps
A booking is permitted iff it lies within [date 08:30, date 12:00] or [date 12:30, date 17:00], comparing the booking's start and end timestamps (not times of day). This closes the lunch-window gaps and, because bookings ending at 24:00 now end on the next day, flags them without a special case.
*Alternative:* patch the existing time-of-day conditions — rejected; the "outside" formulation is what produced the gaps.

### Core working time ignores zero-duration bookings
Zero-duration bookings are filtered out before determining the day's first and last working-time booking, replacing the whole-day exemption. A day consisting only of zero-duration bookings produces no warning.

### Working location is checked in every month
The working-location check no longer skips months without journeys; outside a trip the expected location is the main one, so any other location requires an active trip.

## Risks / Trade-offs

- [Employees see new `WRONG_DOCTOR_APPOINTMENT` / `OUTSIDE_CORE_WORKING_TIME` / `INVALID_WORKING_LOCATION` / `TO_MISSING` warnings for bookings that were silently accepted before] → Intended; the specs document the rules, and the #827 ticket explains the background.
- [Fewer warnings in responses due to de-duplication] → The frontend already groups warnings by type and date, so displayed chips do not change.

## Observations (not changed, recorded for review)

These behaviours are specified as they are today; they look debatable but were not decided as part of this change:
- **Break time:** every gap between working-time bookings counts as break, regardless of its length (e.g. six 5-minute gaps make 30 minutes), and inactive travel counts as break.
- **Location project-relevance:** the warning fires for any booking with the flag set; it does not check whether travel time is billed, although the user-facing message refers to billing.
- **Rest time and core working time:** "last booking of the day" is the last booking by start time, which differs from the latest end only for overlapping bookings (themselves flagged by `TIME_OVERLAP`).
