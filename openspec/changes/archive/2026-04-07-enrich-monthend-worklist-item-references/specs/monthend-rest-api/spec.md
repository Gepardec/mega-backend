## MODIFIED Requirements

### Requirement: Employee monthend endpoints expose employee worklist and self-service flows
The system SHALL provide employee-scoped monthend REST endpoints that allow authenticated employees to retrieve their monthend worklist, prepare their own project monthend context, and create clarifications for their own monthend project context. Responses SHALL expose the generated task and clarification models needed by the employee client. Each worklist task entry SHALL include a nested project object containing the project identifier and project name, and a nullable nested subject employee object containing the employee identifier and full name.

#### Scenario: Employee retrieves their monthend worklist
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** the API returns the employee's open monthend tasks for that month
- **THEN** the API returns the employee-visible open clarifications for that month

#### Scenario: Employee worklist task includes nested project and subject employee
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** each task entry includes a nested project object containing the project identifier and project name
- **THEN** each task entry includes a nullable nested subject employee object containing the employee identifier and full name

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a clarification creation request for their own monthend project context
- **THEN** the API creates an employee-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model

### Requirement: Project-lead monthend endpoints expose lead worklist and clarification creation
The system SHALL provide project-lead-scoped monthend REST endpoints that allow authenticated project leads to retrieve their lead worklist and create clarifications for a subject employee in the same monthend project context. Responses SHALL expose the generated task and clarification models needed by the lead client. Each worklist task entry SHALL include a nested project object containing the project identifier and project name, and a nullable nested subject employee object containing the employee identifier and full name.

#### Scenario: Project lead retrieves the lead monthend worklist
- **WHEN** an authenticated project lead requests the project-lead monthend worklist for a month
- **THEN** the API returns the lead-visible open monthend tasks for that month
- **THEN** the API returns the lead-visible open clarifications for that month

#### Scenario: Lead worklist task includes nested project and subject employee
- **WHEN** an authenticated project lead requests the project-lead monthend worklist for a month
- **THEN** each task entry includes a nested project object containing the project identifier and project name
- **THEN** each task entry includes a nullable nested subject employee object containing the employee identifier and full name

#### Scenario: Project lead creates a clarification for a subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request for a subject employee in a monthend project context
- **THEN** the API creates a project-lead-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model
