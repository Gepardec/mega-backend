## MODIFIED Requirements

### Requirement: Generation is idempotent for an existing month
The system SHALL avoid duplicate month-end tasks when generation is rerun for the same month and business obligation, regardless of whether the existing obligation was created by scheduled generation or by explicit employee self-service preparation.

#### Scenario: Regeneration keeps existing task instances
- **WHEN** month-end generation runs again for a month where the same project obligation already exists
- **THEN** the system does not create a duplicate `MonthEndTask` for that obligation

#### Scenario: Scheduled generation skips an employee task prepared through self-service
- **WHEN** scheduled month-end generation runs for a month where an employee-owned obligation with the same business key was already prepared explicitly by the subject employee
- **THEN** the system does not create a duplicate `MonthEndTask` for that obligation
