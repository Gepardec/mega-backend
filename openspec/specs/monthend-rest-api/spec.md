# monthend-rest-api Specification

## Purpose
TBD - created by archiving change add-spec-first-monthend-api. Update Purpose after archive.
## Requirements
### Requirement: Monthend REST API contract is defined spec-first
The system SHALL define the monthend REST API in a single canonical OpenAPI document. Java API interfaces and HTTP models for monthend endpoints SHALL be generated from that contract and used by the REST adapter implementation.

#### Scenario: Generated API types follow the canonical contract
- **WHEN** the monthend REST contract is updated
- **THEN** the OpenAPI document is updated as the canonical source
- **THEN** the generated Java API interfaces and models reflect that contract for the monthend REST adapter layer

#### Scenario: Monthend REST adapters implement generated interfaces
- **WHEN** the monthend REST API is implemented
- **THEN** handwritten monthend REST adapters implement the generated Java API interfaces instead of defining separate handwritten endpoint signatures

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
- **THEN** each overview clarification entry includes a `canEditText` field set to `true` if the lead is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes a `canDelete` field set to `true` if the lead is the creator of that clarification and it is open, `false` otherwise
- **THEN** each overview clarification entry includes resolution fields when the clarification is resolved

#### Scenario: Project lead creates a clarification with an optional subject employee
- **WHEN** an authenticated eligible project lead submits a clarification creation request with an optional subject employee in a monthend project context
- **THEN** the API creates a monthend clarification with the authenticated lead as creator
- **THEN** when a subject employee is provided the clarification is scoped to that employee; when absent the clarification is project-level
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for all involved user references and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated lead

### Requirement: Shared monthend endpoints expose monthend actions
The system SHALL provide shared monthend REST endpoints that allow authenticated employee or project-lead actors to complete monthend tasks, edit open clarification text when they are the creator, resolve clarifications when they are an involved party other than the creator, and delete their own open clarifications.

#### Scenario: Eligible actor completes a monthend task
- **WHEN** an authenticated eligible actor submits a task completion request
- **THEN** the API completes the monthend task through the existing completion flow
- **THEN** the API returns the resulting task state including completion metadata when present

#### Scenario: Creator edits their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification text update
- **THEN** the API updates the clarification text through the existing clarification update flow
- **THEN** the API returns the updated clarification as a fully enriched clarification response including UserRefs and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated actor

#### Scenario: Eligible actor resolves a clarification
- **WHEN** an authenticated involved party who is not the creator submits a clarification resolution request with an optional resolution note
- **THEN** the API resolves the clarification through the existing clarification completion flow
- **THEN** the API returns the resolved clarification as a fully enriched clarification response including UserRefs and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated actor

#### Scenario: Creator deletes their open clarification
- **WHEN** the authenticated creator of an open clarification submits a clarification deletion request
- **THEN** the API permanently removes the clarification
- **THEN** the API returns a 204 No Content response

#### Scenario: Creator cannot delete a done clarification
- **WHEN** the authenticated creator of a done clarification submits a clarification deletion request
- **THEN** the API rejects the request

### Requirement: Clarification responses are enriched by the REST adapter using the authenticated actor
The REST adapter SHALL be responsible for all clarification response enrichment. For every endpoint that returns a clarification (create, update, complete, and overview), the REST adapter SHALL resolve user display information by calling `MonthEndUserSnapshotPort.findByIds()` with the set of UserIds referenced in the clarification, and SHALL evaluate capability flags (`canResolve`, `canEditText`, `canDelete`) by invoking the corresponding domain methods on the `MonthEndClarification` aggregate with the `actorId` derived from the authentication context. No application service SHALL perform this enrichment.

#### Scenario: Mutation response carries enriched clarification without re-fetch
- **WHEN** an authenticated actor creates, updates, or completes a clarification
- **THEN** the response contains the same fully enriched clarification shape as an overview entry
- **THEN** the client can update its local overview state directly from the mutation response without issuing a separate overview request

#### Scenario: Capability flags reflect the authenticated actor's permissions
- **WHEN** the REST adapter maps a clarification to its response DTO
- **THEN** `canEditText` is `true` only when the authenticated actor is the creator and the clarification is open
- **THEN** `canResolve` is `true` only when the authenticated actor is an involved party, is not the creator, and the clarification is open
- **THEN** `canDelete` is `true` only when the authenticated actor is the creator and the clarification is open

### Requirement: Work time data is not embedded in the monthend status overview response
The monthend status overview endpoint SHALL NOT include work time aggregations in its response. Work time per payroll month SHALL be fetched via the dedicated `/worktime` endpoints. The frontend SHALL call the work time endpoints independently to obtain billable/non-billable hour breakdowns alongside the monthend overview.

#### Scenario: Monthend overview response contains no work time fields
- **WHEN** an authenticated actor requests the monthend status overview
- **THEN** the response does NOT contain billable hours, non-billable hours, or work time entries
- **THEN** work time data is available exclusively through the `/worktime/employee/{month}` and `/worktime/projects/{month}` endpoints

### Requirement: Monthend endpoint access follows employee, project-lead, and ops roles
The system SHALL secure monthend REST endpoints by endpoint group. Employee-scoped endpoints MUST require the employee role, project-lead-scoped endpoints MUST require the project-lead role, shared actor-scoped endpoints MUST require either employee or project-lead role, and internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: User without employee role cannot access employee-scoped endpoint
- **WHEN** an authenticated caller without the employee role requests an employee-scoped monthend endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: User without project-lead role cannot access lead-scoped endpoint
- **WHEN** an authenticated caller without the project-lead role requests a project-lead-scoped monthend endpoint
- **THEN** the API rejects the request as forbidden

#### Scenario: Employee can access shared monthend endpoint
- **WHEN** an authenticated employee requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Project lead can access shared monthend endpoint
- **WHEN** an authenticated project lead requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Non-ops caller cannot access generation endpoint
- **WHEN** an authenticated caller without the internal sync or cron role requests the monthend generation endpoint
- **THEN** the API rejects the request as forbidden

### Requirement: Internal monthend generation is available through the same API contract
The system SHALL provide an internal monthend generation endpoint in the same OpenAPI contract for operational callers. The endpoint SHALL trigger monthend task generation for the requested month and return the generation result using generated response models.

#### Scenario: Ops caller triggers generation for a month
- **WHEN** an authenticated internal ops caller submits a monthend generation request for a month
- **THEN** the API triggers monthend task generation for that month
- **THEN** the API returns the generation result including created and skipped counts
