# Proposal

## Why

A project whose billability preset in ZEP is billable is meant to be charged to the customer. A non-billable booking on such a project is therefore almost always a mistake, or at least something the project lead should confirm. Today it goes unnoticed until the manual review. MEGA should report it as a worktime warning before the month is closed.

## What Changes

- New warning type `NON_BILLABLE_ON_BILLABLE_PROJECT`: a non-billable booking was made on a project whose billability preset is billable (ZEP presets "billable" and "billable fixed").
- The rule applies to project and journey bookings, regardless of the note. A booking marked "Intern - " is flagged too, so a non-billable booking without a marker on a billable project triggers both `INTERNAL_NOTE_MISSING` and the new warning.
- The rule only checks bookings from 2026-10-01 onward, the same cutoff date as the internal-note convention.
- Bookings on projects that MEGA does not know, and bookings without a project reference, are not checked.
- Detailed bookings additionally carry a reference to their ZEP project.
- The project's billability is read as it is when the warnings are requested, not as it was at booking time.

## Capabilities

### New Capabilities
- `worktime-warning-billable-project`: Flags non-billable bookings on projects whose billability preset is billable (booking kinds, cutoff date, unknown projects).

### Modified Capabilities
- `worktime-bookings`: Detailed bookings carry the ZEP project reference of their attendance record.
- `worktime-warnings`: Warnings are derived additionally from the billability of the booked projects. The warning-type vocabulary is extended with `NON_BILLABLE_ON_BILLABLE_PROJECT`.

## Impact

- **Backend (worktime BC)**: booking model and ZEP mapper, project snapshot outbound port and adapter, warnings service, warning assembler, new calculator, warning-type enum.
- **Backend (project BC)**: none. The worktime adapter reads the existing project repository; the project's `billable` flag already covers both billable presets.
- **REST API**: no structural change. `GET /worktime/warnings/{payrollMonth}` can return the new `type` value.
- **Frontend**: needs a translation key for `NON_BILLABLE_ON_BILLABLE_PROJECT`. The warning texts live in the frontend; adding it there is not part of this change.
