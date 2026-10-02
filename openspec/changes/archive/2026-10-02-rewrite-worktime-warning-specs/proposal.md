# Proposal

## Why

The worktime warning rules have no specification of their own: apart from the no-time-entry rule, `worktime-warnings` only requires that the calculators "produce the same warnings as the legacy calculators". The legacy calculators were deleted by the migration, so the rules are now defined only by code — and legacy bugs became requirements by reference. The first production month surfaced exactly such bugs (#827: bookings ending at 24:00 hid excess-working-time, missing-rest-time and core-working-time violations; bookings ending 22:01–22:59 were not flagged), and reviewing the rules revealed further gaps in the doctor-appointment and core-working-time checks.

## What Changes

- Replace the "calculators preserve legacy behaviour" requirement with **one capability per warning rule**, each stating the rule as domain behaviour with scenarios. Rules are named after the business rule, not the implementing class.
- Specify the #827 fixes already implemented: a booking ending at 24:00 ends at the start of the following day and still belongs to its start date; core working time ends at exactly 22:00.
- **Behaviour changes** (code follows the spec in this change):
  - **Doctor appointments**: a booking on the doctor's-appointment process must lie *entirely* within 08:30–12:00 or 12:30–17:00. Today bookings covering the lunch window (e.g. 12:00–12:30, 11:00–12:30) and bookings ending at 24:00 are not flagged.
  - **Core working time**: zero-duration bookings are ignored when determining a day's start and end. Today a zero-duration booking at either end exempts the whole day, hiding a violation at the other end.
  - **Working location**: a working location other than the main one requires an active trip, in every month. Today months without any journey are not checked at all.
  - **At most one warning per type and date**: today the working-location, doctor-appointment and journey-pairing rules can emit identical duplicate warnings for the same day.
  - **Journey pairing**: an onward journey never opens a trip. Today an onward journey after a return journey reopens the trip, so a following return journey is not flagged with `TO_MISSING` and a following outbound journey triggers a spurious `BACK_MISSING`.
  - **Break time**: the missing break is rounded once, after summing the breaks. Today each break is rounded before summing (two 10-minute breaks yield 0.16 instead of 0.17 hours).
- Move the no-time-entry rule out of `worktime-warnings` into its own capability; its behaviour is unchanged.
- State the month-scoped journey pairing (trips crossing a month boundary produce `BACK_MISSING` / `TO_MISSING`) explicitly as a known limitation.
- Drop the legacy-parity clause from `worktime-booking-collection`.

## Capabilities

### New Capabilities
- `worktime-warning-core-working-time`: work outside 06:00–22:00 (`OUTSIDE_CORE_WORKING_TIME`)
- `worktime-warning-time-overlap`: overlapping bookings on a day (`TIME_OVERLAP`)
- `worktime-warning-holiday`: bookings on public holidays (`HOLIDAY`)
- `worktime-warning-weekend`: bookings on Saturdays and Sundays (`WEEKEND`)
- `worktime-warning-doctor-appointment`: doctor's appointments outside the permitted windows (`WRONG_DOCTOR_APPOINTMENT`)
- `worktime-warning-max-daily-working-time`: more than 10 hours of working time per day (`EXCESS_WORKING_TIME_PRESENT`)
- `worktime-warning-rest-time`: less than 11 hours of rest between consecutive days (`MISSING_REST_TIME`)
- `worktime-warning-break-time`: less than 30 minutes of break once working time exceeds 6 hours (`MISSING_BREAK_TIME`)
- `worktime-warning-journey-pairing`: outbound/return journey consistency (`TO_MISSING`, `BACK_MISSING`)
- `worktime-warning-journey-working-location`: working location during and outside trips (`INVALID_WORKING_LOCATION`)
- `worktime-warning-location-project-relevance`: the "location project-relevant" flag (`LOCATION_RELEVANT_SET`)
- `worktime-warning-no-time-entry`: expected working days without bookings (`NO_TIME_ENTRY`), moved from `worktime-warnings`

### Modified Capabilities
- `worktime-warnings`: remove the legacy-parity requirement (keep pure functions + injected clock), move the no-time-entry requirement out, add the at-most-one-warning-per-type-and-date rule.
- `worktime-bookings`: bookings ending at 24:00 end at the start of the following day.
- `worktime-booking-collection`: remove the pre-refactoring parity clause from the calculator-input requirement.

## Impact

- **Code (hexagon `worktime` domain):** doctor-appointment window check, core-working-time zero-duration handling, working-location check in months without journeys, journey-pairing state after a return journey, break-time rounding, and per-type-and-date de-duplication of warnings; corresponding tests. Calculators that already match the new specs are unchanged.
- **Already implemented on `feature/827`:** midnight end mapping and the exact 22:00 core-working-time boundary.
- **REST API / OpenAPI:** unchanged shape; responses may contain fewer duplicate warnings, new doctor/core-time/journey warnings where the old checks had gaps, and slightly different missing-break hours.
- **Frontend:** none.
