## MODIFIED Requirements

### Requirement: Worklists expose the business context needed to act on a task or clarification
The system SHALL include the task type, project reference (id and name), and subject employee reference (id and full name) when present for generated tasks, and SHALL include the project reference, subject employee reference, clarification text, creator metadata, and clarification status metadata for visible clarifications.

#### Scenario: Lead sees which employee must be reviewed
- **WHEN** a lead receives a `PROJECT_LEAD_REVIEW` task in their worklist
- **THEN** the task includes the reviewed employee reference with both the employee id and full name

#### Scenario: Employee sees which project requires action
- **WHEN** an employee receives an employee-owned month-end task in their worklist
- **THEN** the task includes the associated project reference with both the project id and name

#### Scenario: Lead sees which employee a visible clarification belongs to
- **WHEN** a lead receives a clarification in their worklist
- **THEN** the clarification includes the associated project reference with both the project id and name
- **THEN** the clarification includes the associated subject employee reference with both the employee id and full name

#### Scenario: Employee sees clarification details needed to act
- **WHEN** an employee receives a clarification in their worklist
- **THEN** the clarification includes its text and creator metadata
- **THEN** the clarification exposes whether it is still open or already done
