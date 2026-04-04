## MODIFIED Requirements

### Requirement: Status overview entries expose the context needed for matrix or dashboard rendering
The system SHALL include the month-end task identity, task type, task status, project reference, project name, and subject employee reference when present in each overview entry.

#### Scenario: Review task entry identifies the employee and named project
- **WHEN** the overview contains a `PROJECT_LEAD_REVIEW` task
- **THEN** that overview entry includes both the reviewed employee reference and the associated project reference
- **THEN** that overview entry includes the associated project name

#### Scenario: Abrechnung entry identifies the named project without a subject employee
- **WHEN** the overview contains an `ABRECHNUNG` task
- **THEN** that overview entry includes the associated project reference and project name
- **THEN** that overview entry includes no subject employee reference
