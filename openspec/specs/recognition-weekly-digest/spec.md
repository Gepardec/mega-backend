# Recognition Weekly Digest

## Purpose

Defines the weekly recognition digest: how recipients (active internal project leads) are resolved for a reference date, when the digest is triggered (Monday 17:00 editorial deadline / Redaktionsschluss), which entries it contains, and how those entries transition to "included in digest" after a successful send.

## Requirements

### Requirement: Recipients are the internal project leads active on a given date
The digest SHALL determine its recipients as all internal project leads active on a supplied reference date. A recipient qualifies when they hold the project-lead role, are employed (active) on that date, and are internal (not external). Recipient resolution SHALL be parameterized by the reference date so it can be evaluated for any date. External users and users not active on the reference date SHALL be excluded.

#### Scenario: Active internal project lead is included
- **WHEN** recipients are resolved for a reference date
- **AND** a user holds the project-lead role, is active on that date, and is internal
- **THEN** that user is included in the recipient list

#### Scenario: External project lead is excluded
- **WHEN** recipients are resolved for a reference date
- **AND** a user holds the project-lead role and is active on that date but is external
- **THEN** that user is excluded from the recipient list

#### Scenario: Inactive project lead is excluded
- **WHEN** recipients are resolved for a reference date
- **AND** a user holds the project-lead role and is internal but is not active on that date
- **THEN** that user is excluded from the recipient list

#### Scenario: Non-lead user is excluded
- **WHEN** recipients are resolved for a reference date
- **AND** a user is active and internal but does not hold the project-lead role
- **THEN** that user is excluded from the recipient list

### Requirement: The digest is sent weekly at the editorial deadline
The system SHALL trigger the weekly digest automatically every Monday at 17:00 (the "Redaktionsschluss" / editorial deadline). When triggered, it SHALL resolve recipients for the current date and send the digest to each of them.

#### Scenario: Weekly trigger dispatches the digest
- **WHEN** the Monday 17:00 editorial deadline is reached
- **THEN** recipients are resolved for the current date
- **THEN** the digest is sent to each resolved recipient

### Requirement: The digest contains all entries not yet included in a previous digest
Each weekly digest SHALL contain every recognition entry whose status is "new" at the time the digest is assembled, and SHALL NOT contain entries already included in a previous digest. The digest content SHALL present each entry's message and its category (praise/appreciation or courage).

#### Scenario: New entries appear in the digest
- **WHEN** the digest is assembled and there are entries with status "new"
- **THEN** every such entry is included in the digest, showing its message and category

#### Scenario: Previously included entries do not reappear
- **WHEN** the digest is assembled
- **THEN** entries already marked "included in digest" are not part of the digest

### Requirement: Included entries transition to included-in-digest after sending
After a digest has been sent, every entry contained in that digest SHALL transition from "new" to "included in digest" so it is not sent again. The transition SHALL occur only after the send completes, so that a failed run leaves the entries as "new" for the next weekly run.

#### Scenario: Sent entries are marked included
- **WHEN** a digest containing one or more "new" entries has been sent successfully
- **THEN** each of those entries has status "included in digest"

#### Scenario: Failed send leaves entries new
- **WHEN** assembling or sending the digest fails before completion
- **THEN** the affected entries retain the status "new"

### Requirement: The digest is sent even when there are no new entries
When a weekly digest run finds no entries with status "new", the system SHALL still send a digest to all resolved recipients, using an explicit empty-state message indicating there are no new entries this week. This provides recipients a reliable signal that the process is operating.

#### Scenario: Empty-state digest is sent when there are no new entries
- **WHEN** the weekly digest runs and no entries have status "new"
- **THEN** a digest with an empty-state message is still sent to every resolved recipient
- **THEN** no entry status changes occur

#### Scenario: No recipients means nothing is sent
- **WHEN** the weekly digest runs and no internal project leads are active on the current date
- **THEN** no digest mail is sent
