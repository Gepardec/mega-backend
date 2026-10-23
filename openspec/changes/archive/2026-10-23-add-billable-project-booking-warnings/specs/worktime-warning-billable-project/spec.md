# Spec Delta

## Purpose

Flags non-billable bookings on projects whose billability preset is billable, so that time that should be charged to the customer stands out before the manual review.

## ADDED Requirements

### Requirement: Non-billable bookings on billable projects are reported per day
The system SHALL produce a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for every date on which at least one non-billable booking refers to a billable project. A project counts as billable when its ZEP billability preset is "billable" or "billable fixed". The rule SHALL apply to project and journey bookings, regardless of the booking's note. The warning SHALL carry the date and no hours.

#### Scenario: A non-billable project booking on a billable project is reported
- **WHEN** a non-billable project booking on 2026-10-05 refers to a project with the preset "billable"
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 without hours is produced

#### Scenario: A project with the preset "billable fixed" counts as billable
- **WHEN** a non-billable booking on 2026-10-05 refers to a project with the preset "billable fixed"
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 is produced

#### Scenario: A non-billable journey booking on a billable project is reported
- **WHEN** a non-billable journey booking on 2026-10-05 refers to a billable project
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 is produced

#### Scenario: An internal marker does not excuse the booking
- **WHEN** a non-billable booking on 2026-10-05 refers to a billable project and carries the note "Intern - JFX"
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 is produced

#### Scenario: A booking without marker on a billable project triggers both convention warnings
- **WHEN** a non-billable booking on 2026-10-05 refers to a billable project and carries the note "JFX"
- **THEN** both an `INTERNAL_NOTE_MISSING` and a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 are produced

#### Scenario: A billable booking on a billable project is not reported
- **WHEN** a billable booking refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that booking

#### Scenario: A non-billable booking on a non-billable project is not reported
- **WHEN** a non-billable booking refers to a project with the preset "not billable" or "not billable fixed"
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that booking

#### Scenario: Several affected bookings on one day yield one warning
- **WHEN** two non-billable bookings on the same day refer to billable projects
- **THEN** exactly one `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that day

### Requirement: Bookings on unknown projects are not checked
A booking SHALL NOT trigger `NON_BILLABLE_ON_BILLABLE_PROJECT` when it carries no project reference, or when its project is not known to the system. Such bookings SHALL NOT cause the warnings request to fail.

#### Scenario: A booking on a project unknown to the system is not reported
- **WHEN** a non-billable booking on 2026-10-05 refers to a ZEP project that the system does not know
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-10-05 and the other warnings are still returned

#### Scenario: A booking without a project reference is not reported
- **WHEN** a non-billable booking on 2026-10-05 carries no project reference
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-10-05

### Requirement: The project's current billability is decisive
The rule SHALL use the billability the project has at the time the warnings are requested. A change of the project's preset SHALL affect the warnings of all months on the next request, including months before the change.

#### Scenario: A project that became billable affects earlier bookings
- **WHEN** a project's preset changes from "not billable" to "billable" and the warnings for an earlier month with non-billable bookings on that project are requested afterwards
- **THEN** `NON_BILLABLE_ON_BILLABLE_PROJECT` warnings are produced for those bookings' dates

### Requirement: Billable-project bookings are only checked from 2026-10-01 onward
The rule SHALL only consider bookings whose date is on or after 2026-10-01. Earlier bookings SHALL NOT trigger `NON_BILLABLE_ON_BILLABLE_PROJECT`.

#### Scenario: A booking before the cutoff date is not checked
- **WHEN** a non-billable booking on 2026-09-30 refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-09-30

#### Scenario: A booking on the cutoff date is checked
- **WHEN** a non-billable booking on 2026-10-01 refers to a billable project
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for 2026-10-01
