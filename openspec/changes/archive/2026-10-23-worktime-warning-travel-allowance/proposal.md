# Proposal

## Why

Employees who travel passively on a business trip (train, bus, passenger in a car) may book a travel allowance ("Reisepauschale") for time that cannot be used productively, such as changing trains, walking and subway rides. The allowance is 30 minutes per journey, or 15 minutes on short routes such as Wien or Linz to St. Pölten. Nobody checks this today, so allowances that are too high, booked without a passive journey, or booked on the wrong project go unnoticed until the manual review. MEGA should report them as worktime warnings before the month is closed.

## What Changes

- Journey bookings additionally carry the start and destination of the journey as ZEP supplies them (free text).
- Every passive journey on a day contributes a travel allowance to that day: 15 minutes on a short route (St. Pölten to or from Wien or Linz, in either direction), 30 minutes otherwise. Active journeys contribute nothing, because active travel time is already working time.
- Start and destination are recognised as known places despite spelling variants (e.g. "Wien Hbf", "St.Pölten", "Sankt Pölten"). A place that is not recognised counts as an ordinary route (30 minutes).
- A booking on the ZEP process "Reisepauschale" (process id 873, project "Intern") is a travel-allowance booking.
- New warning type `TRAVEL_ALLOWANCE_EXCEEDED`: the travel-allowance bookings of a day exceed the allowance of that day's passive journeys on internal trips. The warning carries the excess hours. A day without a passive internal journey has an allowance of 0.
- New warning type `TRAVEL_ALLOWANCE_WRONG_PROJECT`: travel allowance was booked on "Intern" although all passive journeys of that day belong to a billable customer project, where the allowance should have been booked instead.
- A journey counts as a customer trip when its project is billable; journeys on non-billable or unknown projects, or without project, count as internal trips.
- The amount of travel allowance booked on customer projects is not checked: it cannot be told apart from the other bookings on that project. Such non-billable bookings are already reported by `NON_BILLABLE_ON_BILLABLE_PROJECT`.
- The rules only check days from 2026-10-01 onward, the same cutoff date as the other booking-convention rules.

## Capabilities

### New Capabilities
- `worktime-warning-travel-allowance`: Derives the travel allowance per day from passive journeys and their route, and flags travel-allowance bookings that exceed it or are booked on the wrong project (place recognition, short routes, internal vs. customer trips, cutoff date).

### Modified Capabilities
- `worktime-bookings`: Journey bookings carry the start and destination of their attendance record.
- `worktime-warnings`: The warning-type vocabulary is extended with `TRAVEL_ALLOWANCE_EXCEEDED` and `TRAVEL_ALLOWANCE_WRONG_PROJECT`.

## Impact

- **Backend (worktime BC)**: journey booking model and ZEP mapper, warning assembler, new calculator and route/place recognition, warning-type enum. The warnings service already passes the billable projects of all bookings (including journeys) to the assembler; no new port is needed.
- **ZEP integration**: the attendance REST DTO reads the existing fields `start` and `destination`. No new ZEP call.
- **REST API**: no structural change. `GET /worktime/warnings/{payrollMonth}` can return the new `type` values; `TRAVEL_ALLOWANCE_EXCEEDED` carries `hours`.
- **Frontend**: needs translation keys for both new types. Adding them is not part of this change.
