# Spec Delta

## MODIFIED Requirements

### Requirement: Non-billable bookings without an internal marker are reported per day
The system SHALL produce an `INTERNAL_NOTE_MISSING` warning for every date on which at least one non-billable booking carries no internally marked note, provided the payroll month contains billable work. A payroll month contains billable work when at least one of the employee's bookings in that month is billable or refers to a billable project. In a payroll month without billable work, the system SHALL NOT produce `INTERNAL_NOTE_MISSING`. The rule also applies to bookings without a note. The rule SHALL apply to project and journey bookings. The warning SHALL carry the date and no hours.

#### Scenario: A non-billable booking without a marker is reported
- **WHEN** a non-billable project booking on 2026-10-06 carries the note "JFX" and the month contains a billable booking on another day
- **THEN** an `INTERNAL_NOTE_MISSING` warning for 2026-10-06 without hours is produced

#### Scenario: A non-billable booking without a note is reported
- **WHEN** a non-billable booking on 2026-10-06 carries no note and the month contains a billable booking
- **THEN** an `INTERNAL_NOTE_MISSING` warning for 2026-10-06 is produced

#### Scenario: A non-billable journey booking without a marker is reported
- **WHEN** a non-billable journey booking on 2026-10-06 carries the note "Nach Linz" and the month contains a billable booking
- **THEN** an `INTERNAL_NOTE_MISSING` warning for 2026-10-06 is produced

#### Scenario: A non-billable booking with a marker is not reported
- **WHEN** a non-billable booking carries the note "Intern - JFX" and the month contains a billable booking
- **THEN** no `INTERNAL_NOTE_MISSING` warning is produced for that booking

#### Scenario: Several affected bookings on one day yield one warning
- **WHEN** two non-billable bookings on the same day carry no internally marked note and the month contains a billable booking
- **THEN** exactly one `INTERNAL_NOTE_MISSING` warning is produced for that day

#### Scenario: A month with only non-billable bookings on non-billable projects is not checked
- **WHEN** all bookings of the employee in October 2026 are non-billable, refer to non-billable projects, and carry no internally marked note
- **THEN** no `INTERNAL_NOTE_MISSING` warning is produced for October 2026

#### Scenario: A single billable booking makes the whole month checked
- **WHEN** the employee's only billable booking in October 2026 is on 2026-10-20, and non-billable bookings without a marker exist on 2026-10-06 and 2026-10-07
- **THEN** `INTERNAL_NOTE_MISSING` warnings are produced for 2026-10-06 and 2026-10-07

#### Scenario: A non-billable booking on a billable project counts as billable work
- **WHEN** the employee's only booking in October 2026 is a non-billable booking on 2026-10-06 that refers to a billable project and carries the note "JFX"
- **THEN** an `INTERNAL_NOTE_MISSING` warning for 2026-10-06 is produced

#### Scenario: Billable work in another month does not count
- **WHEN** the employee has billable bookings in October 2026 and only non-billable bookings without a marker on non-billable projects in November 2026
- **THEN** no `INTERNAL_NOTE_MISSING` warning is produced for November 2026

#### Scenario: A project that became billable makes an earlier month checked
- **WHEN** a project's preset changes from "not billable" to "billable" and the warnings for an earlier month whose only billable-related bookings are non-billable bookings on that project are requested afterwards
- **THEN** that month counts as containing billable work and `INTERNAL_NOTE_MISSING` warnings are produced for its unmarked non-billable bookings
