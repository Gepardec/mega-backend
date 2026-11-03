# worktime-warning-internal-note-prefix Specification

## Purpose
Defines when bookings violate the "Intern -" note convention, flagging an "Intern -" prefix on a booking for a billable project with `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` and a missing prefix on a booking for an internal project with `INTERNAL_PREFIX_MISSING`.

## Requirements

### Requirement: A booking's project classifies it as internal or billable
The system SHALL classify every booking, project and journey alike, by the project it is booked on: a booking on a project that MEGA's project data marks as non-billable is internal, a booking on a project marked as billable is billable. The booking's own billable flag SHALL NOT influence the classification. A booking whose project is not known to MEGA SHALL be left unclassified and SHALL produce neither convention warning.

#### Scenario: A non-billable booking on a billable project counts as billable
- **WHEN** a booking that ZEP marks as non-billable is booked on a project MEGA marks as billable
- **THEN** the booking is evaluated as a billable-project booking

#### Scenario: A journey booking is classified by its project
- **WHEN** a journey booking is booked on a project MEGA marks as non-billable
- **THEN** the journey booking is evaluated as an internal-project booking

#### Scenario: A booking on an unknown project produces no convention warning
- **WHEN** a booking is booked on a project that MEGA's project data does not contain
- **THEN** neither an `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` nor an `INTERNAL_PREFIX_MISSING` warning is produced for that booking

### Requirement: The "Intern -" prefix is recognised tolerantly
A booking note SHALL count as carrying the "Intern -" prefix when, after optional leading whitespace, it starts with the word "intern" in any letter case, followed by optional whitespace and a hyphen, an en dash, or an em dash. Any other note, including an absent or empty note, SHALL count as not carrying the prefix.

#### Scenario: Spelling variants of the prefix are recognised
- **WHEN** a booking note starts with `Intern -`, `intern-`, `INTERN -`, `  Intern - `, `Intern –`, or `Intern—`
- **THEN** the note counts as carrying the prefix

#### Scenario: Words merely starting with "intern" are not a prefix
- **WHEN** a booking note starts with `Interne Schulung` or `Internet-Recherche`
- **THEN** the note counts as not carrying the prefix

#### Scenario: An empty note carries no prefix
- **WHEN** a booking has no note or an empty note
- **THEN** the note counts as not carrying the prefix

### Requirement: A prefix on a billable-project booking is flagged per day
The system SHALL produce an `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` warning for every date that carries at least one billable-project booking whose note carries the "Intern -" prefix. The warning SHALL carry the date and no hours.

#### Scenario: A prefixed booking on a billable project is flagged
- **WHEN** a booking on a billable project has the note `Intern - Teammeeting`
- **THEN** an `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` warning is produced for that booking's date

#### Scenario: An unprefixed booking on a billable project is not flagged
- **WHEN** a booking on a billable project has the note `Kickoff`
- **THEN** no `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` warning is produced for that booking's date

#### Scenario: Several prefixed billable-project bookings on one day produce one warning
- **WHEN** two bookings on billable projects on the same day both carry the prefix
- **THEN** exactly one `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` warning is produced for that day

### Requirement: A missing prefix on an internal-project booking is flagged per day
The system SHALL produce an `INTERNAL_PREFIX_MISSING` warning for every date that carries at least one internal-project booking whose note does not carry the "Intern -" prefix. The warning SHALL carry the date and no hours.

#### Scenario: An unprefixed booking on an internal project is flagged
- **WHEN** a booking on an internal project has the note `Teammeeting`
- **THEN** an `INTERNAL_PREFIX_MISSING` warning is produced for that booking's date

#### Scenario: An internal-project booking without a note is flagged
- **WHEN** a booking on an internal project has an empty note
- **THEN** an `INTERNAL_PREFIX_MISSING` warning is produced for that booking's date

#### Scenario: A prefixed booking on an internal project is not flagged
- **WHEN** a booking on an internal project has the note `Intern - Teammeeting`
- **THEN** no `INTERNAL_PREFIX_MISSING` warning is produced for that booking's date

#### Scenario: Both violations on one day produce one warning each
- **WHEN** one day carries an unprefixed internal-project booking and a prefixed billable-project booking
- **THEN** that day has exactly one `INTERNAL_PREFIX_MISSING` warning and exactly one `INTERNAL_PREFIX_ON_BILLABLE_PROJECT` warning
