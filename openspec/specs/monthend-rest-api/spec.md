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
The system SHALL treat the authenticated caller as the acting monthend actor for all actor-scoped monthend REST endpoints. Actor-scoped requests MUST NOT require a caller-supplied actor identifier for employee status overview, project-lead status overview, task completion, clarification edit, clarification resolve, or employee self-service preparation.

#### Scenario: Task completion is attributed to the authenticated caller
- **WHEN** an authenticated eligible actor completes a monthend task through the REST API
- **THEN** the completion uses the authenticated actor as the acting user
- **THEN** the request does not require a separate actor identifier

#### Scenario: Self-service preparation acts on the authenticated employee context
- **WHEN** an authenticated employee prepares monthend obligations through the REST API
- **THEN** the API uses the authenticated employee as the acting user for preparation
- **THEN** the request does not require a separate actor identifier

### Requirement: Monthend status overview is available via explicit role-suffixed paths
The system SHALL provide two explicit status overview endpoints — one for the employee view and one for the project-lead view — so that actors holding both roles can independently request either view. `GET /monthend/{month}/status-overview/employee` SHALL return tasks and clarifications where the authenticated actor is the subject. `GET /monthend/{month}/status-overview/project-lead` SHALL return all tasks and clarifications for projects the authenticated actor leads.

#### Scenario: Employee retrieves their monthend status overview
- **WHEN** an authenticated actor requests `GET /monthend/{month}/status-overview/employee`
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

#### Scenario: Project lead retrieves their monthend status overview
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview/project-lead`
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

#### Scenario: Project lead requests employee view for their own employee page
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview/employee`
- **THEN** the API returns only tasks and clarifications where the lead is the subject employee
- **THEN** the response is identical in shape to a regular employee overview

#### Scenario: Employee prepares a project context with optional clarification
- **WHEN** an authenticated employee submits a preparation request for one project and month with optional clarification text to `POST /monthend/preparations`
- **THEN** the API ensures the employee-owned monthend obligations for that project context exist
- **THEN** the API includes the ensured tasks and the created clarification when clarification text was provided

#### Scenario: Employee creates a clarification for their own project context
- **WHEN** an authenticated employee submits a `CreateClarificationRequest` to `POST /monthend/clarifications` without a `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated employee as both creator and subject
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for `createdBy` and `subjectEmployee`, and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated employee

### Requirement: Project-lead monthend clarification creation uses a unified endpoint with optional subject
The system SHALL allow project leads to create monthend clarifications via the unified `POST /monthend/clarifications` endpoint. When the authenticated actor holds the project-lead role and a `subjectEmployeeId` is provided in the request, the clarification SHALL be scoped to that employee. When the authenticated actor holds the project-lead role and no `subjectEmployeeId` is provided, the clarification SHALL be project-level with `subjectEmployeeId = null`. When the authenticated actor does not hold the project-lead role, `subjectEmployeeId` SHALL always be set to the authenticated actor's own ID regardless of the request body.

#### Scenario: Project lead creates a clarification with an explicit subject employee
- **WHEN** an authenticated project lead submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with a `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated lead as creator and the specified employee as subject
- **THEN** the API returns the created clarification as a fully enriched clarification response including UserRefs for all involved user references and `canResolve`, `canEditText`, and `canDelete` flags evaluated for the authenticated lead

#### Scenario: Project lead creates a project-level clarification without subject
- **WHEN** an authenticated project lead submits a `CreateClarificationRequest` to `POST /monthend/clarifications` without a `subjectEmployeeId`
- **THEN** the API creates a monthend clarification with the authenticated lead as creator and a null subject employee
- **THEN** the API returns the created clarification as a fully enriched clarification response

#### Scenario: Employee cannot create a clarification scoped to another employee
- **WHEN** an authenticated actor without the project-lead role submits a `CreateClarificationRequest` to `POST /monthend/clarifications` with a `subjectEmployeeId` referencing another user
- **THEN** the API ignores the provided `subjectEmployeeId` and creates the clarification with the authenticated actor as both creator and subject

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
- **WHEN** the REST adapter maps a clarification to its generated `Dto`
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
The system SHALL secure monthend REST endpoints by operation. The status overview endpoint and clarification/preparation creation endpoints MUST require at least the employee role. Internal generation endpoints MUST require the internal sync or cron role defined for operational endpoints.

#### Scenario: Unauthenticated caller cannot access monthend endpoints
- **WHEN** an unauthenticated caller requests any actor-scoped monthend endpoint
- **THEN** the API rejects the request as unauthorized

#### Scenario: Employee can access the employee status overview endpoint
- **WHEN** an authenticated employee requests `GET /monthend/{month}/status-overview/employee`
- **THEN** the API accepts the request and returns the employee view

#### Scenario: Non-project-lead cannot access the project-lead status overview endpoint
- **WHEN** an authenticated actor without the project-lead role requests `GET /monthend/{month}/status-overview/project-lead`
- **THEN** the API rejects the request as forbidden

#### Scenario: Project lead can access both status overview endpoints
- **WHEN** an authenticated project lead requests `GET /monthend/{month}/status-overview/project-lead`
- **THEN** the API accepts the request and returns the project-lead view
- **WHEN** the same project lead requests `GET /monthend/{month}/status-overview/employee`
- **THEN** the API accepts the request and returns the employee view scoped to that lead

#### Scenario: Employee can access shared monthend endpoint
- **WHEN** an authenticated employee requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Project lead can access shared monthend endpoint
- **WHEN** an authenticated project lead requests a shared actor-scoped monthend endpoint
- **THEN** the API accepts the request and evaluates the action against the existing monthend eligibility rules

#### Scenario: Non-ops caller cannot access generation endpoint
- **WHEN** an authenticated caller without the internal sync or cron role requests `POST /monthend/{month}/generate`
- **THEN** the API rejects the request as forbidden

### Requirement: Internal monthend generation is available through the same API contract
The system SHALL provide an internal monthend generation endpoint in the same OpenAPI contract for operational callers. The endpoint SHALL trigger monthend task generation for the requested month and return the generation result using generated `Dto` types. The month SHALL be encoded in the path.

#### Scenario: Ops caller triggers generation for a month
- **WHEN** an authenticated internal ops caller submits `POST /monthend/{month}/generate`
- **THEN** the API triggers monthend task generation for that month
- **THEN** the API returns the generation result including created and skipped counts
