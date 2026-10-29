# worktime-warning-internal-note Specification

## Purpose
Checks the "Intern - " booking convention against a booking's billability. This way, billable bookings with an internal marker and non-billable bookings without a marker stand out before the manual review.

## Requirements

### Requirement: A note counts as internally marked when it starts with "Intern" and a delimiter
A note SHALL count as internally marked if, after optional leading spaces, it starts with the word "Intern", regardless of upper or lower case, followed by optional spaces and a delimiter. Accepted delimiters SHALL be a hyphen (`-`), an en dash (`–`) or a colon (`:`). An empty or missing note SHALL NOT count as internally marked, nor SHALL a note that contains the marker only elsewhere.

#### Scenario: A note following the convention is marked
- **WHEN** a booking carries the note "Intern - Jenkins aufräumen"
- **THEN** the note counts as internally marked

#### Scenario: Deviating spellings are marked
- **WHEN** a booking carries one of the notes "intern - JFX", "Intern -JFX", "Intern– JFX" or "  Intern - JFX"
- **THEN** the note counts as internally marked

#### Scenario: A colon as delimiter is marked
- **WHEN** a booking carries one of the notes "Intern: JFX", "intern : JFX", "INTERN:JFX" or "  Intern: JFX"
- **THEN** the note counts as internally marked

#### Scenario: A marker in the middle of the text does not count
- **WHEN** a booking carries the note "JFX (Intern - Vorbereitung)" or "JFX (Intern: Vorbereitung)"
- **THEN** the note does not count as internally marked

#### Scenario: "Intern" without a dash does not count
- **WHEN** a booking carries the note "Internes Meeting" or "Intern JFX"
- **THEN** the note does not count as internally marked

#### Scenario: An empty note is not marked
- **WHEN** a booking carries no note or an empty note
- **THEN** the note does not count as internally marked

### Requirement: Billable bookings with an internal marker are reported per day
The system SHALL produce an `INTERNAL_NOTE_BILLABLE` warning for every date on which at least one billable booking carries an internally marked note. The rule SHALL apply to project and journey bookings. The warning SHALL carry the date and no hours.

#### Scenario: A billable project booking with a marker is reported
- **WHEN** a billable project booking on 2026-10-05 carries the note "Intern - JFX"
- **THEN** an `INTERNAL_NOTE_BILLABLE` warning for 2026-10-05 without hours is produced

#### Scenario: A billable journey booking with a marker is reported
- **WHEN** a billable journey booking on 2026-10-05 carries the note "Intern - Fahrt zum Office"
- **THEN** an `INTERNAL_NOTE_BILLABLE` warning for 2026-10-05 is produced

#### Scenario: Several affected bookings on one day yield one warning
- **WHEN** two billable bookings on the same day carry internally marked notes
- **THEN** exactly one `INTERNAL_NOTE_BILLABLE` warning is produced for that day

#### Scenario: A billable booking without a marker is not reported
- **WHEN** a billable booking carries the note "Heapdump analysiert"
- **THEN** no `INTERNAL_NOTE_BILLABLE` warning is produced for that booking

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

### Requirement: The convention is only checked from the rule's configured start date
Both convention checks SHALL form the `INTERNAL_NOTE` warning rule and SHALL be governed by its rule setting. When the setting has an `activeFrom` date, bookings dated before it SHALL trigger neither `INTERNAL_NOTE_BILLABLE` nor `INTERNAL_NOTE_MISSING`. The initial `activeFrom` is 2026-10-01.

#### Scenario: A booking before the configured start date is not checked
- **WHEN** the `INTERNAL_NOTE` rule has `activeFrom` 2026-10-01 and a non-billable booking on 2026-09-30 carries no note
- **THEN** no `INTERNAL_NOTE_MISSING` warning is produced for 2026-09-30

#### Scenario: A booking on the configured start date is checked
- **WHEN** the `INTERNAL_NOTE` rule has `activeFrom` 2026-10-01 and a non-billable booking on 2026-10-01 carries no note, while the month contains billable work
- **THEN** an `INTERNAL_NOTE_MISSING` warning is produced for 2026-10-01

#### Scenario: A billable marked booking before the configured start date is not reported
- **WHEN** the `INTERNAL_NOTE` rule has `activeFrom` 2026-10-01 and a billable booking on 2026-09-15 carries the note "Intern - JFX"
- **THEN** no `INTERNAL_NOTE_BILLABLE` warning is produced for 2026-09-15

#### Scenario: A disabled rule checks no bookings
- **WHEN** the `INTERNAL_NOTE` rule is disabled and a billable booking on 2026-10-06 carries the note "Intern - JFX"
- **THEN** no `INTERNAL_NOTE_BILLABLE` warning is produced
