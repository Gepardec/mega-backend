# Proposal

## Why

A non-billable booking on a billable project without an "Intern" marker is currently flagged with `INTERNAL_NOTE_MISSING`. This is a false positive: such bookings are acceptable for the internal-note convention, and the problem they may represent (time that should be charged to the customer) is already reported by `NON_BILLABLE_ON_BILLABLE_PROJECT`. Reporting both doubles the noise and points the reviewer at the wrong fix (adding a marker instead of checking billability).

## What Changes

- `INTERNAL_NOTE_MISSING` is only produced for non-billable bookings whose project is not billable. A non-billable booking on a billable project no longer triggers it, whatever its note says.
- A booking whose project is unknown to the system, or that carries no project reference, counts as being on a non-billable project and stays checked as before.
- The month gate ("the payroll month contains billable work") is unchanged. A non-billable booking on a billable project still counts as billable work and opens the gate for the month's other bookings.
- `INTERNAL_NOTE_BILLABLE` and `NON_BILLABLE_ON_BILLABLE_PROJECT` are unchanged. The billable-project capability no longer describes `INTERNAL_NOTE_MISSING`: its scenario stating that an unmarked non-billable booking on a billable project triggers both warnings is removed, so each rule only specifies its own warnings.

## Capabilities

### New Capabilities

_None._

### Modified Capabilities

- `worktime-warning-internal-note`: the requirement for `INTERNAL_NOTE_MISSING` gains the condition that the booking's project is not billable; scenarios that expected the warning on bookings on billable projects are rewritten.
- `worktime-warning-billable-project`: the requirement "Non-billable bookings on billable projects are reported per day" is replaced by "Non-billable bookings on billable projects produce one warning per day" with the same behaviour, without the scenario that described `INTERNAL_NOTE_MISSING`.

## Impact

- Code: the internal-note convention calculator in the worktime warning domain services, and its unit tests.
- API: the worktime warnings endpoint returns fewer `INTERNAL_NOTE_MISSING` entries; the response shape is unchanged.
- No data, configuration or frontend changes.
