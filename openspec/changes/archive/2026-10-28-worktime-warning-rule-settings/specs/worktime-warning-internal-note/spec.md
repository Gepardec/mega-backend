# Spec Delta

## ADDED Requirements

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

## REMOVED Requirements

### Requirement: The convention is only checked from 2026-10-01 onward
**Reason**: The fixed cutoff date is replaced by the rule's configurable start date.
**Migration**: The rule's initial setting is `activeFrom` 2026-10-01, so behaviour is unchanged until an administrator changes it.
