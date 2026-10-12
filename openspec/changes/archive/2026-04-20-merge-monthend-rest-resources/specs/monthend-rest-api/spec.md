## MODIFIED Requirements

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

### Requirement: Internal monthend generation is available through the same API contract
The system SHALL provide an internal monthend generation endpoint in the same OpenAPI contract for operational callers. The endpoint SHALL trigger monthend task generation for the requested month and return the generation result using generated response models. The month SHALL be encoded in the path.

#### Scenario: Ops caller triggers generation for a month
- **WHEN** an authenticated internal ops caller submits `POST /monthend/{month}/generate`
- **THEN** the API triggers monthend task generation for that month
- **THEN** the API returns the generation result including created and skipped counts

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

## REMOVED Requirements

### Requirement: Project-lead monthend endpoints expose lead overview and clarification creation
**Reason:** The overview is replaced by explicit role-suffixed paths (`/status-overview/employee` and `/status-overview/project-lead`). Clarification creation is replaced by the unified `POST /monthend/clarifications` endpoint with optional `subjectEmployeeId`. The generic `/project-lead/` URL prefix is removed.
**Migration:** Clients calling `GET /monthend/project-lead/status-overview` migrate to `GET /monthend/{month}/status-overview/project-lead`. Clients calling `POST /monthend/project-lead/clarifications` migrate to `POST /monthend/clarifications` with the same body fields plus the now-optional `subjectEmployeeId`.
