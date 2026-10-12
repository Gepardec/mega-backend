## MODIFIED Requirements

### Requirement: Actor-scoped monthend endpoints derive the acting user from authentication
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee status overview, project-lead status overview, task completion, clarification edit, clarification resolve, or employee self-service preparation.

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a separate actor identifier

### Requirement: Employee monthend endpoints expose employee overview and self-service flows
The system SHALL provide employee-scoped monthend REST endpoints that allow authenticated employees to retrieve their employee status overview, prepare their own project monthend context, and create clarifications for their own monthend project context. Responses SHALL expose the generated task and clarification models needed by the employee client.

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
- **THEN** each overview clarification entry includes a `canEditText` field set to `true` if the employee is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes a `canDelete` field set to `true` if the employee is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes resolution fields when the clarification is resolved

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a clarification creation request for their own monthend project context
- **THEN** the API creates a monthend clarification with the authenticated employee as creator and subject
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for `createdBy` and `subjectEmployee`, and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated employee

### Requirement: Project-lead monthend endpoints expose lead overview and clarification creation
The system SHALL provide project-lead-scoped monthend REST endpoints that allow authenticated project leads to retrieve their lead status overview and create clarifications with an optional subject employee in the same monthend project context. Responses SHALL expose the generated task and clarification models needed by the lead client.

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
- **THEN** each overview clarification entry includes a `canEditText` field set to `true` if the lead is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes a `canDelete` field set to `true` if the lead is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes resolution fields when the clarification is resolved

#### Scenario: Project lead creates a clarification with an optional subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request with an optional subject employee in a monthend project context
- **THEN** the API creates a monthend clarification with the authenticated lead as creator
- **THEN** when a subject employee is provided the clarification is scoped to that employee; when absent the clarification is project-level
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for all involved user references and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated lead
