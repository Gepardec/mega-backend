## MODIFIED Requirements

### Requirement: Employee monthend endpoints expose employee worklist, employee overview, and self-service flows
The system SHALL provide employee-scoped monthend REST endpoints that allow authenticated employees to retrieve their monthend worklist, retrieve their employee status overview, prepare their own project monthend context, and create clarifications for their own monthend project context. Responses SHALL expose the generated task and clarification models needed by the employee client. Each worklist task entry SHALL include a nested project object containing the project identifier and project name, and a nullable nested subject employee object containing the employee identifier and full name.

#### Scenario: Employee retrieves their monthend worklist
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** the API returns the employee's open monthend tasks for that month
- **THEN** the API returns the employee-visible open clarifications for that month

#### Scenario: Employee worklist task includes nested project and subject employee
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** each task entry includes a nested project object containing the project identifier and project name
- **THEN** each task entry includes a nullable nested subject employee object containing the employee identifier and full name

#### Scenario: Employee retrieves their monthend status overview
- **WHEN** an authenticated employee requests the employee monthend status overview for a month
- **THEN** the API returns tasks where the employee is the subject, including both open and completed tasks
- **THEN** the API returns all clarifications where the employee is the subject for that month, including both open and resolved clarifications
- **THEN** each overview task entry includes a nested project object containing the project identifier and project name
- **THEN** each overview task entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview task entry includes a `canComplete` field set to `true` if the employee is eligible to complete that task and `false` otherwise
- **THEN** each overview clarification entry includes a nullable nested employee reference object for `subjectEmployee` and a nested employee reference object for `createdBy`
- **THEN** each resolved overview clarification entry includes a nested employee reference object for `resolvedBy`
- **THEN** each overview clarification entry includes a `canResolve` field set to `true` if the employee is eligible to resolve that clarification and `false` otherwise
- **THEN** each overview clarification entry includes resolution fields when the clarification is resolved

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a clarification creation request for their own monthend project context
- **THEN** the API creates a monthend clarification with the authenticated employee as creator and subject
- **THEN** the API returns the created clarification in the generated response model

### Requirement: Project-lead monthend endpoints expose lead worklist, lead overview, and clarification creation
The system SHALL provide project-lead-scoped monthend REST endpoints that allow authenticated project leads to retrieve their lead worklist, retrieve their lead status overview, and create clarifications with an optional subject employee in the same monthend project context. Responses SHALL expose the generated task and clarification models needed by the lead client. Each worklist task entry SHALL include a nested project object containing the project identifier and project name, and a nullable nested subject employee object containing the employee identifier and full name.

#### Scenario: Project lead retrieves the lead monthend worklist
- **WHEN** an authenticated project lead requests the project-lead monthend worklist for a month
- **THEN** the API returns the lead-visible open monthend tasks for that month
- **THEN** the API returns the lead-visible open clarifications for that month

#### Scenario: Lead worklist task includes nested project and subject employee
- **WHEN** an authenticated project lead requests the project-lead monthend worklist for a month
- **THEN** each task entry includes a nested project object containing the project identifier and project name
- **THEN** each task entry includes a nullable nested subject employee object containing the employee identifier and full name

#### Scenario: Project lead retrieves their monthend status overview
- **WHEN** an authenticated project lead requests the project-lead monthend status overview for a month
- **THEN** the API returns all tasks for projects the lead leads, including both open and completed tasks
- **THEN** the API returns all clarifications for projects the lead leads for that month, including both open and resolved clarifications
- **THEN** each overview task entry includes a nested project object containing the project identifier and project name
- **THEN** each overview task entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview task entry includes a `canComplete` field set to `true` if the lead is eligible to complete that task and `false` otherwise
- **THEN** each overview clarification entry includes a nullable nested employee reference object for `subjectEmployee` and a nested employee reference object for `createdBy`
- **THEN** each resolved overview clarification entry includes a nested employee reference object for `resolvedBy`
- **THEN** each overview clarification entry includes a `canResolve` field set to `true` if the lead is eligible to resolve that clarification and `false` otherwise
- **THEN** each overview clarification entry includes resolution fields when the clarification is resolved

#### Scenario: Project lead creates a clarification with an optional subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request with an optional subject employee in a monthend project context
- **THEN** the API creates a monthend clarification with the authenticated lead as creator
- **THEN** when a subject employee is provided the clarification is scoped to that employee; when absent the clarification is project-level
- **THEN** the API returns the created clarification in the generated response model

### Requirement: Shared monthend endpoints expose monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to complete monthend tasks, edit open clarification text when they are the creator, resolve clarifications when they are an involved party other than the creator, and delete their own open clarifications.

#### Scenario: Eligible actor completes a monthend task
- **WHEN** an authenticated eligible actor submits a task completion request
- **THEN** the API completes the monthend task through the existing completion flow
- **THEN** the API returns the resulting task state including completion metadata when present

#### Scenario: Creator edits their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification text update
- **THEN** the API updates the clarification text through the existing clarification update flow
- **THEN** the API returns the updated clarification state

#### Scenario: Eligible actor resolves a clarification
- **WHEN** an authenticated involved party who is not the creator submits a clarification resolution request with an optional resolution note
- **THEN** the API resolves the clarification through the existing clarification completion flow
- **THEN** the API returns the resolved clarification state

#### Scenario: Creator deletes their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification deletion request
- **THEN** the API permanently removes the clarification
- **THEN** the API returns a 204 No Content response

#### Scenario: Creator cannot delete a done clarification
- **WHEN** the authenticated creator of a done clarification submits a clarification deletion request
- **THEN** the API rejects the request
