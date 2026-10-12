## MODIFIED Requirements

### Requirement: Status overview entries expose the context needed for matrix or dashboard rendering
The system SHALL include the month-end task identity, task type, task status, a project reference object containing the project identifier and project name, and the subject employee reference when present in each overview entry.

#### Scenario: Review task entry identifies the employee and named project
- **WHEN** the overview contains a `PROJECT_LEAD_REVIEW` task
- **THEN** that overview entry includes both the reviewed employee reference and a project reference object
- **THEN** the project reference object includes the associated project identifier and project name

#### Scenario: Abrechnung entry identifies the named project without a subject employee
- **WHEN** the overview contains an `ABRECHNUNG` task
- **THEN** that overview entry includes a project reference object with the associated project identifier and project name
- **THEN** that overview entry includes no subject employee reference
