## REMOVED Requirements

### Requirement: Non-billable bookings on billable projects are reported per day
**Reason**: The requirement carried a scenario that described the `INTERNAL_NOTE_MISSING` warning, which belongs to the internal-note convention rule. It is replaced by "Non-billable bookings on billable projects produce one warning per day", which has the same behaviour and only the scenarios of this rule.
**Migration**: None. The behaviour of `NON_BILLABLE_ON_BILLABLE_PROJECT` is unchanged. How `INTERNAL_NOTE_MISSING` treats bookings on billable projects is specified in `worktime-warning-internal-note`.

## ADDED Requirements

### Requirement: Non-billable bookings on billable projects produce one warning per day
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

#### Scenario: A booking without a marker is reported as well
- **WHEN** a non-billable booking on 2026-10-05 refers to a billable project and carries the note "JFX"
- **THEN** a `NON_BILLABLE_ON_BILLABLE_PROJECT` warning for 2026-10-05 is produced

#### Scenario: A billable booking on a billable project is not reported
- **WHEN** a billable booking refers to a billable project
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that booking

#### Scenario: A non-billable booking on a non-billable project is not reported
- **WHEN** a non-billable booking refers to a project with the preset "not billable" or "not billable fixed"
- **THEN** no `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that booking

#### Scenario: Several affected bookings on one day yield one warning
- **WHEN** two non-billable bookings on the same day refer to billable projects
- **THEN** exactly one `NON_BILLABLE_ON_BILLABLE_PROJECT` warning is produced for that day
