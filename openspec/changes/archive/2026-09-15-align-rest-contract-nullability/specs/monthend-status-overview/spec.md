## MODIFIED Requirements

### Requirement: Completed overview entries retain completion details
The system SHALL expose the completing actor for `DONE` month-end tasks in the status overview. Every overview entry SHALL carry the completing actor field regardless of task status; for an `OPEN` task the field SHALL be present with the value `null`.

#### Scenario: Shared task shows who completed it
- **WHEN** a shared lead-eligible month-end task appears as `DONE` in the status overview
- **THEN** the overview entry includes the eligible actor who completed that task

#### Scenario: Open task has no completer information
- **WHEN** an `OPEN` month-end task appears in the status overview
- **THEN** the overview entry includes the completing actor field with the value `null`
