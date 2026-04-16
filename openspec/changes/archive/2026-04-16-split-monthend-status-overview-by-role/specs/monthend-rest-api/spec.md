## MODIFIED Requirements

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee worklists, project-lead worklists, employee status overview, project-lead status overview, task completion, clarification edit, clarification resolve, or employee self-service preparation.

#### Scenario: Employee worklist is resolved for the authenticated employee
- **WHEN** an authenticated employee requests the employee monthend worklist for a month
- **THEN** the API resolves the acting employee from the authentication context
- **THEN** the request does not require an employee identifier

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a separate actor identifier

---

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
- **THEN** each overview entry includes a nested project object containing the project identifier and project name
- **THEN** each overview entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview entry includes a `canComplete` field set to `true` if the employee is eligible to complete that task and `false` otherwise

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a clarification creation request for their own monthend project context
- **THEN** the API creates an employee-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model

---

### Requirement: Project-lead monthend endpoints expose lead worklist, lead overview, and clarification creation
The system SHALL provide project-lead-scoped monthend REST endpoints that allow authenticated project leads to retrieve their lead worklist, retrieve their lead status overview, and create clarifications for a subject employee in the same monthend project context. Responses SHALL expose the generated task and clarification models needed by the lead client. Each worklist task entry SHALL include a nested project object containing the project identifier and project name, and a nullable nested subject employee object containing the employee identifier and full name.

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
- **THEN** each overview entry includes a nested project object containing the project identifier and project name
- **THEN** each overview entry includes a nullable nested subject employee object containing the employee identifier and full name
- **THEN** each overview entry includes a `canComplete` field set to `true` if the lead is eligible to complete that task and `false` otherwise

#### Scenario: Project lead creates a clarification for a subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request for a subject employee in a monthend project context
- **THEN** the API creates a project-lead-side monthend clarification
- **THEN** the API returns the created clarification in the generated response model

---

### Requirement: Shared monthend endpoints expose monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to complete monthend tasks, edit open clarification text when they are on the creator side, and resolve clarifications when they are on the resolver side.

#### Scenario: Eligible actor completes a monthend task
- **WHEN** an authenticated eligible actor submits a task completion request
- **THEN** the API completes the monthend task through the existing completion flow
- **THEN** the API returns the resulting task state including completion metadata when present

#### Scenario: Creator side edits an open clarification
- **WHEN** an authenticated actor on the creator side submits a clarification text update for an open clarification
- **THEN** the API updates the clarification text through the existing clarification update flow
- **THEN** the API returns the updated clarification state

#### Scenario: Resolver side resolves a clarification
- **WHEN** an authenticated actor allowed to resolve a clarification submits a clarification resolution request with an optional resolution note
- **THEN** the API resolves the clarification through the existing clarification completion flow
- **THEN** the API returns the resolved clarification state
